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

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import reika.dragonapi.libraries.java.ReikaRandomHelper;
import reika.reactorcraft.auxiliary.NeutronBlock;
import reika.reactorcraft.auxiliary.RadiationEffects;
import reika.reactorcraft.auxiliary.RadiationEffects.RadiationIntensity;
import reika.reactorcraft.entities.EntityNeutron;
import reika.reactorcraft.registry.MatBlocks;
import reika.reactorcraft.registry.ReactorOptions;

public class BlockReactorMat extends Block implements NeutronBlock {

	private final MatBlocks type;

	public BlockReactorMat(BlockBehaviour.Properties properties, MatBlocks type) {
		super(properties);
		this.type = type;
	}

	public MatBlocks getType() {
		return type;
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource rand) {
		if (type == MatBlocks.SLAG) {
			if (ReikaRandomHelper.doWithChance(7.5)) {
				RadiationEffects.instance.contaminateArea(world, pos.getX(), pos.getY(), pos.getZ(), 4, 0.5F, 0.05, false, RadiationIntensity.HIGHLEVEL);
			}
		}
		else if (type == MatBlocks.LODESTONE) {
			this.doLodestoneTick(world, pos, false);
		}
	}

	@Override
	protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
		if (type == MatBlocks.LODESTONE)
			this.doLodestoneTick(world, pos, true);
	}

	private void doLodestoneTick(Level world, BlockPos pos, boolean forced) {
		if (world.hasNeighborSignal(pos)) {
			// CoFH IEnergyReceiver gone — push to the block above via the NeoForge transfer-API energy cap.
			EnergyHandler eh = world.getCapability(Capabilities.Energy.BLOCK, pos.above(), Direction.DOWN);
			if (eh != null) {
				int amt = Mth.ceil(ReactorOptions.LODESTONERFMULT.getFloat() * (!forced ? 2 : 1));
				try (Transaction tx = Transaction.openRoot()) {
					eh.insert(amt, tx);
					tx.commit();
				}
			}
		}
	}

	@Override
	public boolean onNeutron(EntityNeutron e, Level world, BlockPos pos) {
		if (type == MatBlocks.GRAPHITE)
			e.moderate();
		return false;
	}

	@Override
	public int getFlammability(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
		return type == MatBlocks.GRAPHITE ? 70 : 0;
	}

	@Override
	public int getFireSpreadSpeed(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
		return type == MatBlocks.GRAPHITE ? 7 : 0;
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		// The scrubber is a permeable mesh — no collision (steam passes up through it).
		return type == MatBlocks.SCRUBBER ? Shapes.empty() : Shapes.block();
	}

}
