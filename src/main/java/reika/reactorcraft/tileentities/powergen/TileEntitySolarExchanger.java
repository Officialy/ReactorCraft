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

import net.minecraft.world.level.block.state.BlockState;
import reika.reactorcraft.registry.ReactorBlockEntities;

import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.base.TankedReactorPowerReceiver;
import reika.reactorcraft.registry.ReactorTiles;
import reika.rotarycraft.auxiliary.interfaces.sodiumsolarupgrades.SodiumSolarOutput;
import reika.rotarycraft.base.tileentity.tileentitypiping.Flow;
import reika.rotarycraft.registry.MachineRegistry;


public class TileEntitySolarExchanger extends TankedReactorPowerReceiver implements SodiumSolarOutput {
	public TileEntitySolarExchanger(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.SOLAR.get(), pos, state);
	}


	public static final int MINPOWER = 65536;
	public static final int MINSPEED = 2048;

	@Override
	public FluidStack drain(Direction from, FluidStack resource, boolean doDrain) {
		return this.drain(from, resource.amount, doDrain);
	}

	@Override
	public FluidStack drain(Direction from, int maxDrain, boolean doDrain) {
		return tank.drain(maxDrain, doDrain);
	}

	@Override
	public boolean canDrain(Direction from, Fluid fluid) {
		return from.offsetY == 0;
	}

	@Override
	public boolean canConnectToPipe(MachineRegistry m) {
		return m.isStandardPipe();
	}

	@Override
	public boolean canConnectToPipeOnSide(MachineRegistry p, Direction side) {
		return this.canConnectToPipe(p);
	}

	@Override
	public int getCapacity() {
		return 1000;
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
	public Flow getFlowForSide(Direction side) {
		return side.offsetY == 0 ? Flow.OUTPUT : Flow.NONE;
	}

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.SOLAR;
	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		super.updateEntity(world, pos);
	}

	@Override
	public boolean isActive() {
		return this.sufficientPower();
	}

	@Override
	public int receiveSodium(int amt) {
		amt = Math.min(amt, tank.getRemainingSpace());
		tank.addLiquid(amt, ReactorCraft.NA_warm);
		return amt;
	}

	@Override
	public int getMinTorque() {
		return 1;
	}

	@Override
	public int getMinSpeed() {
		return MINSPEED;
	}

	@Override
	public long getMinPower() {
		return MINPOWER;
	}

	@Override
	public boolean canReadFrom(Direction dir) {
		return dir == Direction.DOWN;
	}

	@Override
	public int getMinTorque(int available) {
		return 1;
	}

}
