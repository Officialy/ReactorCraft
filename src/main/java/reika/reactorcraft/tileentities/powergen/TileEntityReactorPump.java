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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import reika.dragonapi.instantiable.HybridTank;
import reika.reactorcraft.base.TankedReactorPowerReceiver;
import reika.reactorcraft.registry.ReactorBlockEntities;
import reika.reactorcraft.registry.ReactorFluids;
import reika.reactorcraft.registry.ReactorTiles;
import reika.rotarycraft.auxiliary.interfaces.PipeConnector;
import reika.rotarycraft.base.blockentity.BlockEntityPiping;
import reika.rotarycraft.registry.MachineRegistry;

public class TileEntityReactorPump extends TankedReactorPowerReceiver {

	public TileEntityReactorPump(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.PUMP.get(), pos, state);
	}

	public static final long MINPOWER = 16384;
	public static final int MINTORQUE = 1024;

	private final HybridTank output = new HybridTank("pumpout", this.getCapacity());

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.PUMP;
	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		super.updateEntity(world, pos);

		if (this.canConvert())
			this.convertFluids();
		if (!output.isEmpty())
			this.dumpFluids(world, pos);
	}

	private boolean canConvert() {
		if (!this.sufficientPower())
			return false;
		if (tank.isEmpty())
			return false;
		if (output.isEmpty())
			return true;
		if (output.isFull())
			return false;
		if (tank.getActualFluid().equals(ReactorFluids.getLegacyFluid("rc lowpwater")))
			return output.getActualFluid().equals(Fluids.WATER);
		if (tank.getActualFluid().equals(ReactorFluids.getLegacyFluid("rc lowpammonia")))
			return output.getActualFluid().equals(ReactorFluids.getLegacyFluid("rc ammonia"));
		return false;
	}

	private void dumpFluids(Level world, BlockPos pos) {
		for (int i = 2; i < 6; i++) {
			Direction dir = dirs[i];
			BlockEntity te = this.getAdjacentBlockEntity(dir);
			if (te instanceof PipeConnector pc) {
				int amt = pc.fillPipe(dir.getOpposite(), output.getFluid(), IFluidHandler.FluidAction.EXECUTE);
				if (amt > 0)
					output.removeLiquid(amt);
			}
			else if (te instanceof IFluidHandler fl) {
				int amt = fl.fill(output.getFluid(), IFluidHandler.FluidAction.EXECUTE);
				if (amt > 0)
					output.removeLiquid(amt);
			}
		}
	}

	private void convertFluids() {
		int amt = Math.min(tank.getFluidLevel(), output.getRemainingSpace());
		if (amt <= 0)
			return;
		if (tank.getActualFluid().equals(ReactorFluids.getLegacyFluid("rc lowpwater"))) {
			output.addLiquid(amt, Fluids.WATER);
		}
		else if (tank.getActualFluid().equals(ReactorFluids.getLegacyFluid("rc lowpammonia"))) {
			output.addLiquid(amt, ReactorFluids.getLegacyFluid("rc ammonia"));
		}
		tank.removeLiquid(amt);
	}

	@Override
	protected void animateWithTick(Level world, BlockPos pos) {
		super.animateWithTick(world, pos);
		if (this.getPower() > 0) {
			phi += 15F;
		}
	}

	@Override
	protected void readSyncTag(CompoundTag NBT) {
		super.readSyncTag(NBT);
		output.readFromNBT(NBT);
	}

	@Override
	protected void writeSyncTag(CompoundTag NBT) {
		super.writeSyncTag(NBT);
		output.writeToNBT(NBT);
	}

	// The pump's output tank is drained by adjacent pipes pulling on the horizontal sides.
	@Override
	public FluidStack drainPipe(Direction from, int maxDrain, IFluidHandler.FluidAction doDrain) {
		return from.getAxis().isHorizontal() ? output.drain(maxDrain, doDrain) : FluidStack.EMPTY;
	}

	@Override
	public BlockEntityPiping.Flow getFlowForSide(Direction side) {
		if (this.canReceiveFrom(side))
			return BlockEntityPiping.Flow.INPUT;
		if (side.getAxis().isHorizontal())
			return BlockEntityPiping.Flow.OUTPUT;
		return BlockEntityPiping.Flow.NONE;
	}

	@Override
	public boolean canConnectToPipe(MachineRegistry m) {
		return m.isStandardPipe();
	}

	@Override
	public boolean canConnectToPipeOnSide(MachineRegistry p, Direction side) {
		return side != Direction.DOWN && this.canConnectToPipe(p);
	}

	@Override
	public int getCapacity() {
		return 12000;
	}

	@Override
	public boolean canReceiveFrom(Direction from) {
		return from == Direction.UP;
	}

	@Override
	public boolean canReadFrom(Direction dir) {
		return dir == Direction.DOWN;
	}

	@Override
	public Fluid getInputFluid() {
		return null;
	}

	@Override
	public boolean isValidFluid(Fluid f) {
		if (f.equals(ReactorFluids.getLegacyFluid("rc lowpwater")))
			return true;
		if (f.equals(ReactorFluids.getLegacyFluid("rc lowpammonia")))
			return true;
		return false;
	}

	@Override
	public int getMinTorque(int available) {
		return MINTORQUE;
	}

	@Override
	public int getMinTorque() {
		return MINTORQUE;
	}

	@Override
	public int getMinSpeed() {
		return 1;
	}

	@Override
	public long getMinPower() {
		return MINPOWER;
	}
}
