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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import reika.reactorcraft.base.TankedReactorPowerReceiver;
import reika.reactorcraft.registry.ReactorBlockEntities;
import reika.reactorcraft.registry.ReactorTiles;
import reika.rotarycraft.base.blockentity.BlockEntityPiping;
import reika.rotarycraft.registry.MachineRegistry;

// MOD-PORT: implements RotaryCraft's SodiumSolarOutput once that sodium-solar-upgrade interface is
// ported; until then the receiveSodium() input path (from a solar tower) is gated out.
public class TileEntitySolarExchanger extends TankedReactorPowerReceiver {

	public TileEntitySolarExchanger(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.SOLAR.get(), pos, state);
	}

	public static final int MINPOWER = 65536;
	public static final int MINSPEED = 2048;

	// The exchanger pushes its hot sodium out through the horizontal sides.
	@Override
	public FluidStack drainPipe(Direction from, int maxDrain, IFluidHandler.FluidAction doDrain) {
		return from.getStepY() == 0 ? tank.drain(maxDrain, doDrain) : FluidStack.EMPTY;
	}

	@Override
	public BlockEntityPiping.Flow getFlowForSide(Direction side) {
		return side.getStepY() == 0 ? BlockEntityPiping.Flow.OUTPUT : BlockEntityPiping.Flow.NONE;
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
	public ReactorTiles getTile() {
		return ReactorTiles.SOLAR;
	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		super.updateEntity(world, pos);
	}

	public boolean isActive() {
		return this.sufficientPower();
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
