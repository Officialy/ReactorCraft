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

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

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

	private final HybridTank tank = new HybridTank("co2collector", 1000);

	private ForgeDirection readDir = ForgeDirection.DOWN;

	public int ticks = 512;

	@Override
	public void updateEntity(World world, int x, int y, int z, int meta) {
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
					tank.addLiquid(10, FluidRegistry.getFluid("rc co2"));
			}
		}
		else if (id == MachineRegistry.REFRIGERATOR.getBlock() && this.getAdjacentLocation(readDir).getBlockMetadata(world) == MachineRegistry.REFRIGERATOR.getBlockMetadata()) {
			TileEntityRefrigerator te = (TileEntityRefrigerator)this.getAdjacentTileEntity(readDir);
			te.addAttachment(this, readDir.getOpposite());
		}
		//ReikaJavaLibrary.pConsole(id+":"+tank, Side.SERVER);
	}

	public ForgeDirection getReadDirection() {
		return readDir;
	}

	public boolean hasFurnace() {
		Block id = this.getAdjacentLocation(readDir).getBlock(worldObj);
		return id == Blocks.furnace || id == Blocks.lit_furnace || id == MachineRegistry.REFRIGERATOR.getBlock() && this.getAdjacentLocation(readDir).getBlockMetadata(worldObj) == MachineRegistry.REFRIGERATOR.getBlockMetadata();
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
		return this.canDrain(from, resource.getFluid()) ? tank.drain(resource.amount, doDrain) : null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		return this.canDrain(from, null) ? tank.drain(maxDrain, doDrain) : null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		return false;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		return from == readDir.getOpposite() && ReikaFluidHelper.isFluidDrainableFromTank(fluid, tank);
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		return new FluidTankInfo[]{tank.getInfo()};
	}

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.COLLECTOR;
	}

	@Override
	protected void animateWithTick(World world, int x, int y, int z) {

	}

	@Override
	protected void readSyncTag(NBTTagCompound NBT) {
		super.readSyncTag(NBT);

		tank.readFromNBT(NBT);
	}

	@Override
	protected void writeSyncTag(NBTTagCompound NBT) {
		super.writeSyncTag(NBT);

		tank.writeToNBT(NBT);
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return ReikaAABBHelper.getBlockAABB(this).expand(0.5, 0.5, 0.5);
	}

	@Override
	public void onCompleteCycle(int ln2) {
		tank.addLiquid(ln2*2/7, FluidRegistry.getFluid("rc liquid oxygen"));
	}

	@Override
	public boolean canConnectToPipe(MachineRegistry m) {
		return m.isStandardPipe();
	}

	@Override
	public boolean canConnectToPipeOnSide(MachineRegistry m, ForgeDirection side) {
		return this.canConnectToPipe(m) && this.getFlowForSide(side) != Flow.NONE;
	}

	@Override
	public Flow getFlowForSide(ForgeDirection side) {
		return side == readDir.getOpposite() ? Flow.OUTPUT : Flow.NONE;
	}

}
