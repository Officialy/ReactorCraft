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
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

import reika.dragonapi.instantiable.HybridTank;
import reika.dragonapi.instantiable.storage.HybridTankResourceHandler;
import reika.dragonapi.interfaces.blockentity.HasFluidResourceHandler;
import reika.dragonapi.libraries.java.ReikaStringParser;
import reika.rotarycraft.auxiliary.interfaces.PipeConnector;
import reika.rotarycraft.base.blockentity.BlockEntityPiping;
import reika.rotarycraft.registry.MachineRegistry;

public abstract class TileEntityTankedReactorMachine extends TileEntityReactorBase implements PipeConnector, HasFluidResourceHandler {

	protected final HybridTank tank = new HybridTank(ReikaStringParser.stripSpaces(this.getTEName().toLowerCase(Locale.ENGLISH)), this.getCapacity());
	private final ResourceHandler<FluidResource> fluidHandler = new HybridTankResourceHandler(
			new HybridTank[]{tank}, (index, resource) -> isValidFluid(resource.getFluid()),
			(index, resource) -> false, this::setChanged);

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
		return tank.getActualFluid().getFluid();
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

	@Override
	public ResourceHandler<FluidResource> getFluidHandler(Direction side) {
		return side == null || canReceiveFrom(side) ? fluidHandler : null;
	}








	// --- RC pipe protocol ---
	@Override
	public boolean canConnectToPipeOnSide(MachineRegistry p, Direction side) {
		return this.canReceiveFrom(side) && this.canConnectToPipe(p);
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
