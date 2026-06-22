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

import net.minecraft.world.level.block.state.BlockState;
import reika.reactorcraft.registry.ReactorBlockEntities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidRegistry;
import net.neoforged.neoforge.fluids.FluidStack;

import reika.dragonapi.libraries.ReikaFluidHelper;
import reika.reactorcraft.base.TileEntityTankedReactorMachine;
import reika.reactorcraft.registry.ReactorBlocks;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.tileentities.fission.TileEntityReactorBoiler;
import reika.rotarycraft.base.tileentity.tileentitypiping.Flow;
import reika.rotarycraft.registry.MachineRegistry;

import buildcraft.api.transport.IPipeTile.PipeType;

public class TileEntityCondenser extends TileEntityTankedReactorMachine {
	public TileEntityCondenser(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.CONDENSER.get(), pos, state);
	}


	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.CONDENSER;
	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		thermalTicker.update();
		//this.getSteam(world, x, y, z);
		if (world.getBlock(x, y-1, z) == ReactorBlocks.STEAM.getBlockInstance() && !tank.isFull() && temperature < 100 && !world.isClientSide()) {
			int smeta = world.getBlockMetadata(x, y-1, z);
			Fluid f = this.getFluidFromSteamMetadata(smeta);
			//ReikaJavaLibrary.pConsole(f.getName());
			if (tank.isEmpty() || tank.getActualFluid().equals(f)) {
				world.removeBlock(x, y-1, z);
				tank.addLiquid(TileEntityReactorBoiler.WATER_PER_STEAM, f);
			}
		}

		this.balance(world, x, y, z);
		//tank.addLiquid(100, ReactorCraft.H2O_lo);
	}

	private Fluid getFluidFromSteamMetadata(int smeta) {
		//ReikaJavaLibrary.pConsole(String.format("%4s", Integer.toBinaryString(smeta)).replace(" ", "0"), Dist.DEDICATED_SERVER);
		if ((smeta&4) == 4)
			return ReactorFluids.getLegacyFluid("rc lowpammonia");
		return ReactorFluids.getLegacyFluid("rc lowpwater");
	}

	private void balance(Level world, int x, int y, int z) {
		for (int i = 0; i < 6; i++) {
			Direction dir = dirs[i];
			int dx = x+dir.offsetX;
			int dy = y+dir.offsetY;
			int dz = z+dir.offsetZ;
			ReactorTiles rt = ReactorTiles.getTE(world, dx, dy, dz);
			if (rt == ReactorTiles.CONDENSER) {
				TileEntityCondenser te = (TileEntityCondenser)world.getBlockEntity(dx, dy, dz);
				int dL = te.tank.getFluidLevel() - tank.getFluidLevel();
				if (dL/4 > 0) {
					tank.addLiquid(dL/4, te.tank.getActualFluid());
					te.tank.removeLiquid(dL/4);
				}
			}
		}
	}

	@Override
	protected void animateWithTick(Level world, BlockPos pos) {

	}

	@Override
	public FluidStack drain(Direction from, FluidStack resource, boolean doDrain) {
		return this.canDrain(from, resource.getFluid()) ? tank.drain(resource.amount, doDrain) : null;
	}

	@Override
	public FluidStack drain(Direction from, int maxDrain, boolean doDrain) {
		//ReikaJavaLibrary.pConsole(from, Dist.DEDICATED_SERVER);
		if (this.canDrain(from, null)) {
			return tank.drain(maxDrain, doDrain);
		}
		return null;
	}

	@Override
	public boolean canDrain(Direction from, Fluid fluid) {
		return from == Direction.UP && ReikaFluidHelper.isFluidDrainableFromTank(fluid, tank);
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
	public boolean canReceiveFrom(Direction from) {
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
		return false;//WorkingFluid.getWorkingFluid(f) != ItemStack.EMPTY;
	}

	@Override
	public ConnectOverride overridePipeConnection(PipeType type, Direction with) {
		return type == PipeType.FLUID ? (with == Direction.UP ? ConnectOverride.CONNECT : ConnectOverride.DISCONNECT) : ConnectOverride.DEFAULT;
	}

	@Override
	public boolean canConnectToPipeOnSide(MachineRegistry p, Direction side) {
		return side == Direction.UP && this.canConnectToPipe(p);
	}

	@Override
	public Flow getFlowForSide(Direction side) {
		return side == Direction.UP ? Flow.OUTPUT : Flow.NONE;
	}

}
