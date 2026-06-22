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

import net.minecraft.world.level.block.state.BlockState;
import reika.reactorcraft.registry.ReactorBlockEntities;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityFurnace;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidRegistry;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.world.level.material.FluidTankInfo;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import reika.dragonapi.auxiliary.trackers.ItemMaterialController;
import reika.dragonapi.instantiable.HybridTank;
import reika.dragonapi.instantiable.ItemMaterial;
import reika.dragonapi.libraries.ReikaAABBHelper;
import reika.dragonapi.libraries.ReikaFluidHelper;
import reika.reactorcraft.base.TileEntityReactorBase;
import reika.reactorcraft.registry.ReactorTiles;
import reika.rotarycraft.api.interfaces.RefrigeratorAttachment;
import reika.rotarycraft.auxiliary.interfaces.PipeConnector;
import reika.rotarycraft.base.tileentity.tileentitypiping.Flow;
import reika.rotarycraft.registry.MachineRegistry;
import reika.rotarycraft.tileentities.production.TileEntityRefrigerator;

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

		readDir = dirs[meta].getOpposite();
		Block id = this.getAdjacentLocation(readDir).getBlock(world);
		if (id == Blocks.lit_furnace) {
			TileEntityFurnace te = (TileEntityFurnace)this.getAdjacentTileEntity(readDir);
			ItemStack fuel = te.getStackInSlot(1);
			if (fuel != null && te.isBurning() && te.currentItemBurnTime > 0) {
				ItemMaterial mat = ItemMaterialController.instance.getMaterial(fuel);
				if (mat == ItemMaterial.COAL || mat == ItemMaterial.WOOD)
					tank.addLiquid(10, ReactorFluids.getLegacyFluid("rc co2"));
			}
		}
		else if (id == MachineRegistry.REFRIGERATOR.getBlock() && this.getAdjacentLocation(readDir).getBlockMetadata(world) == MachineRegistry.REFRIGERATOR) {
			TileEntityRefrigerator te = (TileEntityRefrigerator)this.getAdjacentTileEntity(readDir);
			te.addAttachment(this, readDir.getOpposite());
		}
		//ReikaJavaLibrary.pConsole(id+":"+tank, Dist.DEDICATED_SERVER);
	}

	public Direction getReadDirection() {
		return readDir;
	}

	public boolean hasFurnace() {
		Block id = this.getAdjacentLocation(readDir).getBlock(level);
		return id == Blocks.furnace || id == Blocks.lit_furnace || id == MachineRegistry.REFRIGERATOR.getBlock() && this.getAdjacentLocation(readDir).getBlockMetadata(level) == MachineRegistry.REFRIGERATOR;
	}

	@Override
	public int fill(Direction from, FluidStack resource, boolean doFill) {
		return 0;
	}

	@Override
	public FluidStack drain(Direction from, FluidStack resource, boolean doDrain) {
		return this.canDrain(from, resource.getFluid()) ? tank.drain(resource.amount, doDrain) : null;
	}

	@Override
	public FluidStack drain(Direction from, int maxDrain, boolean doDrain) {
		return this.canDrain(from, null) ? tank.drain(maxDrain, doDrain) : null;
	}

	@Override
	public boolean canFill(Direction from, Fluid fluid) {
		return false;
	}

	@Override
	public boolean canDrain(Direction from, Fluid fluid) {
		return from == readDir.getOpposite() && ReikaFluidHelper.isFluidDrainableFromTank(fluid, tank);
	}

	@Override
	public FluidTankInfo[] getTankInfo(Direction from) {
		return new FluidTankInfo[]{tank.getInfo()};
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

	@Override
	public AABB getRenderBoundingBox() {
		return ReikaAABBHelper.getBlockAABB(this).expand(0.5, 0.5, 0.5);
	}

	@Override
	public void onCompleteCycle(int ln2) {
		tank.addLiquid(ln2*2/7, ReactorFluids.getLegacyFluid("rc liquid oxygen"));
	}

	@Override
	public boolean canConnectToPipe(MachineRegistry m) {
		return m.isStandardPipe();
	}

	@Override
	public boolean canConnectToPipeOnSide(MachineRegistry m, Direction side) {
		return this.canConnectToPipe(m) && this.getFlowForSide(side) != Flow.NONE;
	}

	@Override
	public Flow getFlowForSide(Direction side) {
		return side == readDir.getOpposite() ? Flow.OUTPUT : Flow.NONE;
	}

}
