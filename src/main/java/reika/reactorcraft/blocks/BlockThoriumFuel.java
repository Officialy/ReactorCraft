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

import java.util.Locale;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import reika.dragonapi.libraries.java.ReikaRandomHelper;
import reika.dragonapi.libraries.level.ReikaWorldHelper;
import reika.reactorcraft.auxiliary.RadiationEffects;
import reika.reactorcraft.auxiliary.RadiationEffects.RadiationIntensity;
import reika.reactorcraft.registry.ReactorBlocks;

/**
 * 26.2 port: the legacy {@code BlockFluidFinite} (Forge finite-fluid block, gone) for molten LiFBe
 * thorium fuel. The original's flow/spread logic was already commented out; the live behaviour is the
 * eight quanta levels (now an {@link IntegerProperty} 0-7 instead of metadata) and a radiation
 * randomTick. The {@link reika.reactorcraft.tileentities.fission.thorium.TileEntityFuelDump} fills and
 * drains it below itself.
 */
public class BlockThoriumFuel extends Block {

	public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 7);

	public BlockThoriumFuel(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, 7));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LEVEL);
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource rand) {
		if (ReikaRandomHelper.doWithChance(0.005))
			RadiationEffects.instance.contaminateArea(world, pos.getX(), pos.getY()+rand.nextInt(2), pos.getZ(), 2, 0.25F, 0, false, RadiationIntensity.MODERATE);
	}

	/** Whether a fuel quantum may overwrite the block at the given position. */
	public static boolean canOverwrite(Level world, BlockPos pos) {
		BlockState s = world.getBlockState(pos);
		Block b = s.getBlock();
		if (b == ReactorBlocks.THORIUM_FUEL.get())
			return false;
		// MOD-PORT: legacy BlockRegistry.PIPING explicit allow folded into the duct/pipe name check below.
		if (ReikaWorldHelper.softBlocks(world, pos))
			return true;
		String n = b.getClass().getSimpleName().toLowerCase(Locale.ENGLISH);
		return n.contains("duct") || n.contains("conduit") || n.contains("cable") || n.contains("pipe");
	}

}
