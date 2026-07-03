/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import reika.dragonapi.libraries.java.ReikaArrayHelper;
import reika.dragonapi.libraries.java.ReikaRandomHelper;
import reika.dragonapi.libraries.level.ReikaWorldHelper;
import reika.reactorcraft.blocks.multi.BlockGeneratorMulti;
import reika.reactorcraft.blocks.multi.BlockGeneratorMulti.GeneratorPart;
import reika.reactorcraft.registry.MatBlocks;
import reika.reactorcraft.registry.ReactorBlocks;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.tileentities.powergen.TileEntityTurbineCore;

/**
 * Flowing steam — an air-like block that self-propagates toward turbines/generators each scheduled tick.
 * The legacy metadata bitfield is replaced with named blockstate flags.
 */
public class BlockSteam extends Block {

	/** Do not decay (lose itself by chance) when rising. */
	public static final BooleanProperty NO_DECAY = BooleanProperty.create("no_decay");
	/** May still drive a turbine (cleared once it has been spent). */
	public static final BooleanProperty POWERED = BooleanProperty.create("powered");
	/** Ammonia working fluid (vs water). */
	public static final BooleanProperty AMMONIA = BooleanProperty.create("ammonia");
	/** Has moved horizontally already. */
	public static final BooleanProperty MOVED = BooleanProperty.create("moved");

	private static final int TICK_DELAY = 2;

	public BlockSteam(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(NO_DECAY, false).setValue(POWERED, false).setValue(AMMONIA, false).setValue(MOVED, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(NO_DECAY, POWERED, AMMONIA, MOVED);
	}

	/** The state steam takes after transmitting one step in the given direction. */
	private BlockState getTransmittedState(BlockState state, Direction dir) {
		if (dir == Direction.UP) {
			if (!state.getValue(MOVED))
				return state;
			// Rising after a horizontal move: keep the fluid type, mark no-decay, drop powered + moved.
			return this.defaultBlockState().setValue(NO_DECAY, true).setValue(AMMONIA, state.getValue(AMMONIA));
		}
		return state.setValue(MOVED, true);
	}

	public boolean canMoveInto(Level world, BlockPos pos) {
		BlockState s = world.getBlockState(pos);
		if (s.isAir())
			return true;
		if (s.is(this))
			return false;
		if (!s.getFluidState().isEmpty())
			return false;
		return ReikaWorldHelper.softBlocks(world, pos);
	}

	@Override
	protected void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean movedByPiston) {
		super.onPlace(state, world, pos, oldState, movedByPiston);
		world.scheduleTick(pos, this, TICK_DELAY);
	}

	@Override
	protected void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
		if (pos.getY() > world.getMaxY()) {
			world.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
			return;
		}
		this.move(world, pos, state, random);
	}

	private void move(ServerLevel world, BlockPos pos, BlockState state, RandomSource rand) {
		BlockPos above = pos.above();
		BlockState aboveState = world.getBlockState(above);

		// A scrubber block directly above absorbs the steam.
		if (aboveState.getBlock() == ReactorBlocks.matBlock(MatBlocks.SCRUBBER)) {
			world.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
			return;
		}

		if (ReactorTiles.getTE(world, above) == ReactorTiles.TURBINECORE) {
			TileEntityTurbineCore te = (TileEntityTurbineCore) world.getBlockEntity(above);
			Direction dir = te.getSteamMovement();
			int d = te.getNumberStagesTotal() - te.getStage();
			BlockPos target = pos.offset(dir.getStepX() * d, dir.getStepY(), dir.getStepZ() * d);
			if (this.canMoveInto(world, target)) {
				world.setBlock(target, this.getTransmittedState(state, dir), 2);
			}
			else {
				BlockState ts = world.getBlockState(target);
				if (ts.getBlock() == ReactorBlocks.GENERATORMULTI.get() && ts.getValue(BlockGeneratorMulti.PART) == GeneratorPart.COIL) {
					BlockPos gpos = te.getBlockPos().offset(dir.getStepX() * (1 + d), 3, dir.getStepZ() * (1 + d));
					if (this.canMoveInto(world, gpos))
						world.setBlock(gpos, this.getTransmittedState(state, dir), 2);
				}
				else if (ts.getBlock() == ReactorBlocks.FLYWHEELMULTI.get()) {
					int ddx = ReikaRandomHelper.getRandomPlusMinus(target.getX(), 1 + 3 * Math.abs(dir.getStepZ()));
					int ddz = ReikaRandomHelper.getRandomPlusMinus(target.getZ(), 1 + 3 * Math.abs(dir.getStepX()));
					BlockPos fpos = new BlockPos(ddx, target.getY(), ddz);
					if (this.canMoveInto(world, fpos))
						world.setBlock(fpos, this.getTransmittedState(state, dir), 2);
				}
			}
			world.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
			return;
		}

		if (this.canMoveInto(world, above)) {
			if (state.getValue(NO_DECAY) || ReikaRandomHelper.doWithChance(80))
				world.setBlock(above, this.getTransmittedState(state, Direction.UP), 2);
			world.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
			return;
		}

		Direction[] sides = new Direction[]{Direction.EAST, Direction.WEST, Direction.SOUTH, Direction.NORTH};
		ReikaArrayHelper.shuffleArray(sides);
		for (Direction s : sides) {
			BlockPos sp = pos.relative(s);
			if (this.canMoveInto(world, sp)) {
				world.setBlock(sp, this.getTransmittedState(state, s), 2);
				world.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
				return;
			}
		}
	}

	@Override
	protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
		if (entity instanceof ItemEntity || entity instanceof ExperienceOrb)
			return;
		if (level instanceof ServerLevel sl) {
			entity.hurtServer(sl, sl.damageSources().inFire(), 1);
			if (state.getValue(AMMONIA) && entity instanceof LivingEntity le)
				le.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));
		}
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		// Legacy steam rendered as a standard block model on the translucent pass (getRenderType()==0,
		// getRenderBlockPass()==1). The earlier INVISIBLE setting assumed a BER/particle renderer that was
		// never written, so steam blocks the grate emits were completely invisible ("grate not outputting
		// steam"). Render the baked cube model; ReactorModelProvider gives it render_type translucent.
		return RenderShape.MODEL;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
	}

}
