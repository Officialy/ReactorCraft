/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.base;

import java.util.Locale;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import reika.dragonapi.DragonAPI;
import reika.dragonapi.instantiable.HybridTank;
import reika.dragonapi.instantiable.StepTimer;
import reika.reactorcraft.auxiliary.TemperaturedReactorTyped;
import reika.reactorcraft.registry.ReactorTiles;
import reika.rotarycraft.base.blockentity.BlockEntityPiping;
import reika.rotarycraft.registry.MachineRegistry;

public abstract class TileEntityIntermediateBoiler extends TileEntityNuclearBoiler implements TemperaturedReactorTyped {

	protected StepTimer timer = new StepTimer(20);

	protected final HybridTank output = new HybridTank(this.getName().toLowerCase(Locale.ENGLISH)+"out", this.getCapacity());

	public TileEntityIntermediateBoiler(BlockEntityType<?> t, BlockPos pos, BlockState state) {
		super(t, pos, state);
	}

	public abstract int getLiquidUsage();

	public abstract int getMinimumTemperature();

	@Override
	public int getTanks() {
		return 2;
	}

	@Override
	public FluidStack getFluidInTank(int i) {
		return i == 0 ? tank.getFluid() : output.getFluid();
	}

	@Override
	public int getTankCapacity(int i) {
		return i == 0 ? tank.getCapacity() : output.getCapacity();
	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		super.updateEntity(world, pos);

		timer.update();

		if (timer.checkCap()) {
			if (this.canHeat())
				this.heat();
		}

		if (DragonAPI.debugtest)
			this.addLiquid(1000);

		this.transferFluid(world, pos);
	}

	private void transferFluid(Level world, BlockPos pos) {
		ReactorTiles r = ReactorTiles.getTE(world, pos.above());
		if (r == this.getTile()) {
			TileEntityIntermediateBoiler te = (TileEntityIntermediateBoiler)world.getBlockEntity(pos.above());
			if (te.tank.getFluidLevel() < te.tank.getCapacity() && !tank.isEmpty()) {
				int amt = Math.min(tank.getFluidLevel(), Math.min(100, te.tank.getCapacity()-te.tank.getFluidLevel()));
				te.tank.addLiquid(amt, tank.getActualFluid());
				tank.removeLiquid(amt);
			}

			if (te.output.getFluidLevel() < te.output.getCapacity() && !output.isEmpty()) {
				int amt = Math.min(output.getFluidLevel(), Math.min(100, te.output.getCapacity()-te.output.getFluidLevel()));
				te.output.addLiquid(amt, output.getActualFluid());
				output.removeLiquid(amt);
			}
		}
	}

	protected void heat() {
		int amt = this.getLiquidUsage();
		double c = this.getFluidHeatCapacity();
		temperature -= amt*c;
		tank.removeLiquid(amt);
		output.addLiquid(amt, this.getOutputFluid());
	}

	protected abstract Fluid getOutputFluid();

	protected abstract double getFluidHeatCapacity();

	public boolean canHeat() {
		return temperature >= this.getMinimumTemperature() && tank.getFluidLevel() >= this.getLiquidUsage() && output.getFluidLevel() < output.getCapacity() && tank.getActualFluid().equals(this.getInputFluid());
	}

	@Override
	public final boolean canConnectToPipe(MachineRegistry m) {
		return m.isStandardPipe();
	}

	@Override
	public final void animateWithTick(Level world, BlockPos pos) {

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

	@Override
	public FluidStack drain(FluidStack resource, FluidAction action) {
		return output.drain(resource, action);
	}

	@Override
	public FluidStack drain(int maxDrain, FluidAction action) {
		return output.drain(maxDrain, action);
	}

	@Override
	public FluidStack drainPipe(Direction from, int maxDrain, IFluidHandler.FluidAction doDrain) {
		return from == Direction.UP ? output.drain(maxDrain, doDrain) : FluidStack.EMPTY;
	}

	@Override
	public final BlockEntityPiping.Flow getFlowForSide(Direction side) {
		if (side == Direction.UP)
			return BlockEntityPiping.Flow.OUTPUT;
		return super.getFlowForSide(side);
	}

	@Override
	public final boolean canConnectToPipeOnSide(MachineRegistry p, Direction side) {
		return side.getStepY() != 0 && this.canConnectToPipe(p);
	}

}
