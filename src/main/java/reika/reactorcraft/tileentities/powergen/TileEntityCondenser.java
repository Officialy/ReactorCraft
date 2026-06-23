/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.tileentities.powergen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import reika.reactorcraft.base.TileEntityTankedReactorMachine;
import reika.reactorcraft.blocks.BlockSteam;
import reika.reactorcraft.registry.ReactorBlockEntities;
import reika.reactorcraft.registry.ReactorBlocks;
import reika.reactorcraft.registry.ReactorFluids;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.tileentities.fission.TileEntityReactorBoiler;
import reika.rotarycraft.base.blockentity.BlockEntityPiping;
import reika.rotarycraft.registry.MachineRegistry;

public class TileEntityCondenser extends TileEntityTankedReactorMachine {

	public TileEntityCondenser(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.CONDENSER.get(), pos, state);
	}

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.CONDENSER;
	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		thermalTicker.update();
		BlockPos below = pos.below();
		BlockState bs = world.getBlockState(below);
		if (bs.getBlock() == ReactorBlocks.STEAM.get() && !tank.isFull() && temperature < 100 && !world.isClientSide()) {
			Fluid f = this.getFluidFromSteam(bs);
			if (tank.isEmpty() || tank.getActualFluid().getFluid().equals(f)) {
				world.removeBlock(below, false);
				tank.addLiquid(TileEntityReactorBoiler.WATER_PER_STEAM, f);
			}
		}

		this.balance(world, pos);
	}

	/** The low-pressure fluid the given steam blockstate condenses into. */
	private Fluid getFluidFromSteam(BlockState steam) {
		return steam.getValue(BlockSteam.AMMONIA)
				? ReactorFluids.getLegacyFluid("rc lowpammonia")
				: ReactorFluids.getLegacyFluid("rc lowpwater");
	}

	private void balance(Level world, BlockPos pos) {
		for (Direction dir : dirs) {
			BlockPos npos = pos.relative(dir);
			if (ReactorTiles.getTE(world, npos) == ReactorTiles.CONDENSER) {
				TileEntityCondenser te = (TileEntityCondenser) world.getBlockEntity(npos);
				int dL = te.tank.getFluidLevel() - tank.getFluidLevel();
				if (dL / 4 > 0) {
					tank.addLiquid(dL / 4, te.tank.getActualFluid().getFluid());
					te.tank.removeLiquid(dL / 4);
				}
			}
		}
	}

	@Override
	protected void animateWithTick(Level world, BlockPos pos) {

	}

	// The condensed water is pulled out of the top by an adjacent pipe.
	@Override
	public FluidStack drainPipe(Direction from, int maxDrain, IFluidHandler.FluidAction doDrain) {
		return from == Direction.UP ? tank.drain(maxDrain, doDrain) : FluidStack.EMPTY;
	}

	@Override
	public BlockEntityPiping.Flow getFlowForSide(Direction side) {
		return side == Direction.UP ? BlockEntityPiping.Flow.OUTPUT : BlockEntityPiping.Flow.NONE;
	}

	@Override
	public boolean canConnectToPipe(MachineRegistry m) {
		return m.isStandardPipe();
	}

	@Override
	public boolean canConnectToPipeOnSide(MachineRegistry p, Direction side) {
		return side == Direction.UP && this.canConnectToPipe(p);
	}

	@Override
	public int getCapacity() {
		return 12000;
	}

	@Override
	public boolean canReceiveFrom(Direction from) {
		return false;
	}

	@Override
	public Fluid getInputFluid() {
		return null;
	}

	@Override
	public boolean isValidFluid(Fluid f) {
		return false;
	}

	@Override
	protected void readSyncTag(CompoundTag NBT) {
		super.readSyncTag(NBT);
		tank.readFromNBT(NBT);
	}

	@Override
	protected void writeSyncTag(CompoundTag NBT) {
		super.writeSyncTag(NBT);
		tank.writeToNBT(NBT);
	}

}
