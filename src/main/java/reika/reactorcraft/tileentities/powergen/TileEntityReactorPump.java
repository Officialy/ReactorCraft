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

import net.minecraft.world.level.block.state.BlockState;
import reika.reactorcraft.registry.ReactorBlockEntities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidRegistry;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import reika.dragonapi.instantiable.HybridTank;
import reika.dragonapi.libraries.ReikaFluidHelper;
import reika.reactorcraft.base.TankedReactorPowerReceiver;
import reika.reactorcraft.registry.ReactorTiles;
import reika.rotarycraft.registry.MachineRegistry;
import reika.rotarycraft.tileentities.piping.TileEntityPipe;

import buildcraft.api.transport.IPipeTile.PipeType;

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
			this.dumpFluids(world, x, y, z);
		//ReikaJavaLibrary.pConsole(tank+":"+output);
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
			return output.getActualFluid().equals(FluidRegistry.WATER);
		if (tank.getActualFluid().equals(ReactorFluids.getLegacyFluid("rc lowpammonia")))
			return output.getActualFluid().equals(ReactorFluids.getLegacyFluid("rc ammonia"));
		return false;
	}

	private void dumpFluids(Level world, int x, int y, int z) {
		for (int i = 2; i < 6; i++) {
			Direction dir = dirs[i];
			BlockEntity te = this.getAdjacentTileEntity(dir);
			if (te instanceof TileEntityPipe) {
				TileEntityPipe p = (TileEntityPipe)te;
				if (p.canIntakeFluid(output.getActualFluid())) {
					int dL = output.getFluidLevel()-p.getFluidLevel();
					//ReikaJavaLibrary.pConsole(dL);
					if (dL/4 > 0) {
						p.addFluid(dL/4);
						p.setFluid(output.getActualFluid());
						output.removeLiquid(dL/4);
					}
				}
			}
			else if (te instanceof IFluidHandler) {
				IFluidHandler fl = (IFluidHandler)te;
				if (fl.canFill(dir.getOpposite(), output.getActualFluid())) {
					int amt = fl.fill(dir.getOpposite(), output.getFluid(), true);
					if (amt > 0)
						output.removeLiquid(amt);
				}
			}
		}
	}

	private void convertFluids() {
		int amt = Math.min(tank.getFluidLevel(), output.getRemainingSpace());
		if (amt <= 0)
			return;
		if (tank.getActualFluid().equals(ReactorFluids.getLegacyFluid("rc lowpwater"))) {
			output.addLiquid(amt, FluidRegistry.WATER);
		}
		else if (tank.getActualFluid().equals(ReactorFluids.getLegacyFluid("rc lowpammonia"))) {
			output.addLiquid(amt, ReactorFluids.getLegacyFluid("rc ammonia"));
		}
		tank.removeLiquid(amt);
	}

	@Override
	protected void animateWithTick(Level world, BlockPos pos) {
		super.animateWithTick(world, x, y, z);
		if (this.getPower() > 0) {
			phi += 15F;
		}
	}

	@Override
	protected void readSyncTag(CompoundTag NBT)
	{
		super.readSyncTag(NBT);

		output.readFromNBT(NBT);
	}

	@Override
	protected void writeSyncTag(CompoundTag NBT)
	{
		super.writeSyncTag(NBT);

		output.writeToNBT(NBT);
	}

	@Override
	public FluidStack drain(Direction from, FluidStack resource, boolean doDrain) {
		return this.canDrain(from, resource.getFluid()) ? output.drain(resource.amount, doDrain) : null;
	}

	@Override
	public FluidStack drain(Direction from, int maxDrain, boolean doDrain) {
		return this.canDrain(from, null) ? output.drain(maxDrain, doDrain) : null;
	}

	@Override
	public boolean canDrain(Direction from, Fluid fluid) {
		return from.offsetY == 0 && ReikaFluidHelper.isFluidDrainableFromTank(fluid, tank);
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
	public boolean canReadFrom(Direction dir) {
		return dir == Direction.DOWN;
	}

	@Override
	public ConnectOverride overridePipeConnection(PipeType type, Direction side) {
		return type == PipeType.FLUID ? (side != Direction.DOWN ? ConnectOverride.CONNECT : ConnectOverride.DISCONNECT) : ConnectOverride.DEFAULT;
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
