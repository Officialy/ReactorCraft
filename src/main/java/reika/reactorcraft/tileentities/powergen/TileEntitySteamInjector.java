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
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

import reika.dragonapi.base.BlockEntityBase;
import reika.dragonapi.instantiable.HybridTank;
import reika.dragonapi.instantiable.storage.HybridTankResourceHandler;
import reika.dragonapi.interfaces.blockentity.HasFluidResourceHandler;
import reika.reactorcraft.registry.ReactorBlockEntities;
import reika.reactorcraft.registry.ReactorBlocks;
import reika.reactorcraft.registry.ReactorFluids;
import reika.rotarycraft.auxiliary.interfaces.PipeConnector;
import reika.rotarycraft.base.blockentity.BlockEntityPiping.Flow;
import reika.rotarycraft.registry.MachineRegistry;

public class TileEntitySteamInjector extends BlockEntityBase implements PipeConnector, HasFluidResourceHandler {

	private final HybridTank tank = new HybridTank("injector", 1000);
	private final ResourceHandler<FluidResource> fluidHandler = new HybridTankResourceHandler(
			new HybridTank[] {tank},
			(index, resource) -> resource.getFluid() == ReactorFluids.getLegacyFluid("rc lubricant"),
			(index, resource) -> false, this::setChanged);

	@Override
	public ResourceHandler<FluidResource> getFluidHandler(Direction side) {
		return fluidHandler;
	}

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
