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
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import reika.dragonapi.libraries.ReikaFluidHelper;
import reika.reactorcraft.base.TileEntityTankedReactorMachine;
import reika.reactorcraft.registry.ReactorBlocks;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.tileentities.fission.TileEntityReactorBoiler;
import reika.rotarycraft.base.tileentity.tileentitypiping.Flow;
import reika.rotarycraft.registry.MachineRegistry;

import buildcraft.api.transport.IPipeTile.PipeType;

public class TileEntityCondenser extends TileEntityTankedReactorMachine {

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.CONDENSER;
	}

	@Override
	public void updateEntity(Level world, int x, int y, int z, int meta) {
		thermalTicker.update();
		//this.getSteam(world, x, y, z);
		if (world.getBlock(x, y-1, z) == ReactorBlocks.STEAM.getBlockInstance() && !tank.isFull() && temperature < 100 && !world.isRemote) {
			int smeta = world.getBlockMetadata(x, y-1, z);
			Fluid f = this.getFluidFromSteamMetadata(smeta);
			//ReikaJavaLibrary.pConsole(f.getName());
			if (tank.isEmpty() || tank.getActualFluid().equals(f)) {
				world.setBlockToAir(x, y-1, z);
				tank.addLiquid(TileEntityReactorBoiler.WATER_PER_STEAM, f);
			}
		}

		this.balance(world, x, y, z);
		//tank.addLiquid(100, ReactorCraft.H2O_lo);
	}

	private Fluid getFluidFromSteamMetadata(int smeta) {
		//ReikaJavaLibrary.pConsole(String.format("%4s", Integer.toBinaryString(smeta)).replace(" ", "0"), Side.SERVER);
		if ((smeta&4) == 4)
			return FluidRegistry.getFluid("rc lowpammonia");
		return FluidRegistry.getFluid("rc lowpwater");
	}

	private void balance(Level world, int x, int y, int z) {
		for (int i = 0; i < 6; i++) {
			ForgeDirection dir = dirs[i];
			int dx = x+dir.offsetX;
			int dy = y+dir.offsetY;
			int dz = z+dir.offsetZ;
			ReactorTiles rt = ReactorTiles.getTE(world, dx, dy, dz);
			if (rt == ReactorTiles.CONDENSER) {
				TileEntityCondenser te = (TileEntityCondenser)world.getTileEntity(dx, dy, dz);
				int dL = te.tank.getLevel() - tank.getLevel();
				if (dL/4 > 0) {
					tank.addLiquid(dL/4, te.tank.getActualFluid());
					te.tank.removeLiquid(dL/4);
				}
			}
		}
	}

	@Override
	protected void animateWithTick(Level world, int x, int y, int z) {

	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
		return this.canDrain(from, resource.getFluid()) ? tank.drain(resource.amount, doDrain) : null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		//ReikaJavaLibrary.pConsole(from, Side.SERVER);
		if (this.canDrain(from, null)) {
			return tank.drain(maxDrain, doDrain);
		}
		return null;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		return from == ForgeDirection.UP && ReikaFluidHelper.isFluidDrainableFromTank(fluid, tank);
	}

	@Override
	public boolean canConnectToPipe(MachineRegistry m) {
		return m.isStandardPipe();
	}

	@Override
	public int getCapacity() {
		return 12000;
	}

	@Override
	public boolean canReceiveFrom(ForgeDirection from) {
		return false;
	}

	@Override
	public Fluid getInputFluid() {
		return null;
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
	public boolean isValidFluid(Fluid f) {
		return false;//WorkingFluid.getWorkingFluid(f) != null;
	}

	@Override
	public ConnectOverride overridePipeConnection(PipeType type, ForgeDirection with) {
		return type == PipeType.FLUID ? (with == ForgeDirection.UP ? ConnectOverride.CONNECT : ConnectOverride.DISCONNECT) : ConnectOverride.DEFAULT;
	}

	@Override
	public boolean canConnectToPipeOnSide(MachineRegistry p, ForgeDirection side) {
		return side == ForgeDirection.UP && this.canConnectToPipe(p);
	}

	@Override
	public Flow getFlowForSide(ForgeDirection side) {
		return side == ForgeDirection.UP ? Flow.OUTPUT : Flow.NONE;
	}

}
