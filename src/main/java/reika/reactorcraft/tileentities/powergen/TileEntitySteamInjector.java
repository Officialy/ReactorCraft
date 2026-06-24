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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import reika.dragonapi.base.BlockEntityBase;
import reika.dragonapi.instantiable.HybridTank;
import reika.reactorcraft.registry.ReactorBlockEntities;
import reika.reactorcraft.registry.ReactorBlocks;
import reika.reactorcraft.registry.ReactorFluids;
import reika.rotarycraft.auxiliary.interfaces.PipeConnector;
import reika.rotarycraft.base.blockentity.BlockEntityPiping.Flow;
import reika.rotarycraft.registry.MachineRegistry;

public class TileEntitySteamInjector extends BlockEntityBase implements IFluidHandler, PipeConnector {

	private final HybridTank tank = new HybridTank("injector", 1000);

	public TileEntitySteamInjector(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.STEAMINJECTOR.get(), pos, state);
	}

	@Override
	public Block getBlockEntityBlockID() {
		return ReactorBlocks.TURBINEMULTI.get();
	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {

	}

	@Override
	protected void animateWithTick(Level world, BlockPos pos) {

	}

	@Override
	public int getRedstoneOverride() {
		return 0;
	}

	@Override
	protected String getTEName() {
		return "steam_injector";
	}

	private boolean isLube(FluidStack fs) {
		return !fs.isEmpty() && fs.getFluid().equals(ReactorFluids.getLegacyFluid("rc lubricant"));
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
	public int fillPipe(Direction from, FluidStack resource, IFluidHandler.FluidAction action) {
		return this.isLube(resource) ? tank.fill(resource, action) : 0;
	}

	@Override
	public FluidStack drainPipe(Direction from, int maxDrain, IFluidHandler.FluidAction action) {
		return FluidStack.EMPTY;
	}

	// --- NeoForge IFluidHandler (input-only lubricant tank) ---
	@Override
	public int getTanks() {
		return 1;
	}

	@Override
	public FluidStack getFluidInTank(int t) {
		return tank.getFluid();
	}

	@Override
	public int getTankCapacity(int t) {
		return tank.getCapacity();
	}

	@Override
	public boolean isFluidValid(int t, FluidStack stack) {
		return this.isLube(stack);
	}

	@Override
	public int fill(FluidStack resource, FluidAction action) {
		return this.isLube(resource) ? tank.fill(resource, action) : 0;
	}

	@Override
	public FluidStack drain(FluidStack resource, FluidAction action) {
		return FluidStack.EMPTY;
	}

	@Override
	public FluidStack drain(int maxDrain, FluidAction action) {
		return FluidStack.EMPTY;
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

	public int getLubricant() {
		return tank.getFluidLevel();
	}

	void remove(int amt) {
		tank.removeLiquid(amt);
	}

}
