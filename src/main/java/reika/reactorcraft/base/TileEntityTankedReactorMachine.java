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
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import reika.dragonapi.instantiable.HybridTank;
import reika.dragonapi.libraries.java.ReikaStringParser;
import reika.rotarycraft.auxiliary.interfaces.PipeConnector;
import reika.rotarycraft.base.blockentity.BlockEntityPiping;
import reika.rotarycraft.registry.MachineRegistry;

public abstract class TileEntityTankedReactorMachine extends TileEntityReactorBase implements IFluidHandler, PipeConnector {

	protected final HybridTank tank = new HybridTank(ReikaStringParser.stripSpaces(this.getTEName().toLowerCase(Locale.ENGLISH)), this.getCapacity());

	public TileEntityTankedReactorMachine(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public abstract int getCapacity();

	public abstract boolean canReceiveFrom(Direction from);

	public abstract Fluid getInputFluid();

	public int getFluidLevel() {
		return tank.getFluidLevel();
	}

	public Fluid getContainedFluid() {
		return tank.getActualFluid();
	}

	public void addLiquid(int amt) {
		tank.addLiquid(amt, this.getInputFluid());
	}

	public boolean isValidFluid(Fluid f) {
		return f.equals(this.getInputFluid());
	}

	public boolean canFill(Direction from, Fluid f) {
		return this.canReceiveFrom(from) && this.isValidFluid(f);
	}

	// --- NeoForge IFluidHandler (input-only tank; sided filtering is done by the block's
	// capability wrapper / the RC PipeConnector protocol below) ---
	@Override
	public int getTanks() {
		return 1;
	}

	@Override
	public FluidStack getFluidInTank(int tank) {
		return this.tank.getFluid();
	}

	@Override
	public int getTankCapacity(int tank) {
		return this.tank.getCapacity();
	}

	@Override
	public boolean isFluidValid(int tank, FluidStack stack) {
		return this.isValidFluid(stack.getFluid());
	}

	@Override
	public int fill(FluidStack resource, FluidAction action) {
		if (resource.isEmpty() || !this.isValidFluid(resource.getFluid()))
			return 0;
		return tank.fill(resource, action);
	}

	@Override
	public FluidStack drain(FluidStack resource, FluidAction action) {
		return FluidStack.EMPTY;
	}

	@Override
	public FluidStack drain(int maxDrain, FluidAction action) {
		return FluidStack.EMPTY;
	}

	// --- RC pipe protocol ---
	@Override
	public boolean canConnectToPipeOnSide(MachineRegistry p, Direction side) {
		return this.canReceiveFrom(side) && this.canConnectToPipe(p);
	}

	@Override
	public int fillPipe(Direction from, FluidStack resource, IFluidHandler.FluidAction action) {
		if (resource.isEmpty() || !this.canFill(from, resource.getFluid()))
			return 0;
		return tank.fill(resource, action);
	}

	@Override
	public FluidStack drainPipe(Direction from, int maxDrain, IFluidHandler.FluidAction doDrain) {
		return FluidStack.EMPTY;
	}

	@Override
	public BlockEntityPiping.Flow getFlowForSide(Direction side) {
		return this.canReceiveFrom(side) ? BlockEntityPiping.Flow.INPUT : BlockEntityPiping.Flow.NONE;
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
