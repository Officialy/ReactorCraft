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

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidRegistry;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.world.level.material.FluidTankInfo;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import reika.dragonapi.instantiable.HybridTank;
import reika.rotarycraft.auxiliary.interfaces.PipeConnector;
import reika.rotarycraft.base.tileentity.tileentitypiping.Flow;
import reika.rotarycraft.registry.MachineRegistry;

public class TileEntitySteamInjector extends BlockEntity implements IFluidHandler, PipeConnector {

	private final HybridTank tank = new HybridTank("injector", 1000);

	@Override
	public boolean canUpdate() {
		return false;
	}

	@Override
	public boolean canConnectToPipe(MachineRegistry m) {
		return m == MachineRegistry.HOSE || m == MachineRegistry.BEDPIPE;
	}

	@Override
	public boolean canConnectToPipeOnSide(MachineRegistry p, Direction side) {
		return this.canConnectToPipe(p);
	}

	@Override
	public Flow getFlowForSide(Direction side) {
		return Flow.INPUT;
	}

	@Override
	public int fill(Direction from, FluidStack resource, boolean doFill) {
		return this.canFill(from, resource.getFluid()) ? tank.fill(resource, doFill) : 0;
	}

	@Override
	public FluidStack drain(Direction from, FluidStack resource, boolean doDrain) {
		return null;
	}

	@Override
	public FluidStack drain(Direction from, int maxDrain, boolean doDrain) {
		return null;
	}

	@Override
	public boolean canFill(Direction from, Fluid fluid) {
		return fluid.equals(ReactorFluids.getLegacyFluid("rc lubricant"));
	}

	@Override
	public boolean canDrain(Direction from, Fluid fluid) {
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(Direction from) {
		return new FluidTankInfo[]{tank.getInfo()};
	}

	@Override
	public boolean shouldRenderInPass(int pass) {
		return false;
	}

	@Override
	public void loadAdditional(/*PORT*/CompoundTag NBT) {
		super.loadAdditional(/*PORT*/NBT);

		tank.readFromNBT(NBT);
	}

	@Override
	public void saveAdditional(/*PORT*/CompoundTag NBT) {
		super.saveAdditional(/*PORT*/NBT);

		tank.writeToNBT(NBT);
	}

	public int getLubricant() {
		return tank.getFluidLevel();
	}

	void remove(int amt) {
		tank.removeLiquid(amt);
	}

}
