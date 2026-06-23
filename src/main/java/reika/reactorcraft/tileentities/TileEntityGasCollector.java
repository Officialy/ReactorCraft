/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.tileentities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import reika.dragonapi.auxiliary.trackers.ItemMaterialController;
import reika.dragonapi.instantiable.HybridTank;
import reika.dragonapi.instantiable.ItemMaterial;
import reika.reactorcraft.base.TileEntityReactorBase;
import reika.reactorcraft.blocks.BlockReactorMachine;
import reika.reactorcraft.registry.ReactorBlockEntities;
import reika.reactorcraft.registry.ReactorFluids;
import reika.reactorcraft.registry.ReactorTiles;
import reika.rotarycraft.api.interfaces.RefrigeratorAttachment;
import reika.rotarycraft.auxiliary.interfaces.PipeConnector;
import reika.rotarycraft.base.blockentity.BlockEntityPiping.Flow;
import reika.rotarycraft.blockentities.production.BlockEntityRefrigerator;
import reika.rotarycraft.registry.MachineRegistry;

public class TileEntityGasCollector extends TileEntityReactorBase implements IFluidHandler, PipeConnector, RefrigeratorAttachment {

	public TileEntityGasCollector(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.COLLECTOR.get(), pos, state);
	}

	private final HybridTank tank = new HybridTank("co2collector", 1000);

	private Direction readDir = Direction.DOWN;

	public int ticks = 512;

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		if (ticks > 0)
			ticks -= 8;

		readDir = this.getBlockState().getValue(BlockReactorMachine.FACING).getOpposite();
		BlockPos tgt = pos.relative(readDir);
		BlockState bs = world.getBlockState(tgt);
		if (bs.is(Blocks.FURNACE) && bs.getValue(BlockStateProperties.LIT)) {
			BlockEntity be = world.getBlockEntity(tgt);
			if (be instanceof AbstractFurnaceBlockEntity furnace) {
				ItemStack fuel = furnace.getItem(1);
				if (!fuel.isEmpty()) {
					ItemMaterial mat = ItemMaterialController.instance.getMaterial(fuel);
					if (mat == ItemMaterial.COAL || mat == ItemMaterial.WOOD)
						tank.addLiquid(10, ReactorFluids.getLegacyFluid("rc co2"));
				}
			}
		}
		else if (MachineRegistry.getMachine(world, tgt) == MachineRegistry.REFRIGERATOR) {
			BlockEntity be = world.getBlockEntity(tgt);
			if (be instanceof BlockEntityRefrigerator fridge)
				fridge.addAttachment(this, readDir.getOpposite());
		}
	}

	public Direction getReadDirection() {
		return readDir;
	}

	public boolean hasFurnace() {
		BlockPos tgt = this.getBlockPos().relative(readDir);
		BlockState bs = level.getBlockState(tgt);
		return bs.is(Blocks.FURNACE) || MachineRegistry.getMachine(level, tgt) == MachineRegistry.REFRIGERATOR;
	}

	// --- NeoForge IFluidHandler (output-only: filled internally via addLiquid, drained out via pipe) ---
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
		return false;
	}

	@Override
	public int fill(FluidStack resource, FluidAction action) {
		return 0;
	}

	@Override
	public FluidStack drain(FluidStack resource, FluidAction action) {
		if (resource.isEmpty())
			return FluidStack.EMPTY;
		FluidStack in = tank.getFluid();
		if (in.isEmpty() || !FluidStack.isSameFluidSameComponents(resource, in))
			return FluidStack.EMPTY;
		return tank.drain(resource.getAmount(), action);
	}

	@Override
	public FluidStack drain(int maxDrain, FluidAction action) {
		return tank.drain(maxDrain, action);
	}

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.COLLECTOR;
	}

	@Override
	protected void animateWithTick(Level world, BlockPos pos) {

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

	// 1.21.5: BlockEntity.getRenderBoundingBox was removed; kept as a helper for the renderer's bounds.
	public AABB getRenderBoundingBox() {
		return new AABB(this.getBlockPos()).inflate(0.5, 0.5, 0.5);
	}

	@Override
	public void onCompleteCycle(int ln2) {
		tank.addLiquid(ln2 * 2 / 7, ReactorFluids.getLegacyFluid("rc liquid oxygen"));
	}

	// --- RC pipe protocol ---
	@Override
	public boolean canConnectToPipe(MachineRegistry m) {
		return m.isStandardPipe();
	}

	@Override
	public boolean canConnectToPipeOnSide(MachineRegistry m, Direction side) {
		return this.canConnectToPipe(m) && this.getFlowForSide(side) != Flow.NONE;
	}

	@Override
	public int fillPipe(Direction from, FluidStack resource, IFluidHandler.FluidAction action) {
		return 0;
	}

	@Override
	public FluidStack drainPipe(Direction from, int maxDrain, IFluidHandler.FluidAction doDrain) {
		return from == readDir.getOpposite() ? tank.drain(maxDrain, doDrain) : FluidStack.EMPTY;
	}

	@Override
	public Flow getFlowForSide(Direction side) {
		return side == readDir.getOpposite() ? Flow.OUTPUT : Flow.NONE;
	}

}
