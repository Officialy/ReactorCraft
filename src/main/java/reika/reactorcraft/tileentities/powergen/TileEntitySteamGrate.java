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

import reika.dragonapi.DragonAPI;
import reika.dragonapi.modinteract.AtmosphereHandler;
import reika.reactorcraft.auxiliary.SteamTile;
import reika.reactorcraft.base.TileEntityReactorBase;
import reika.reactorcraft.blocks.BlockSteam;
import reika.reactorcraft.registry.ReactorBlocks;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.registry.WorkingFluid;
import reika.rotarycraft.api.interfaces.Screwdriverable;

public class TileEntitySteamGrate extends TileEntityReactorBase implements Screwdriverable, SteamTile {
	public TileEntitySteamGrate(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.GRATE.get(), pos, state);
	}


	private int steam;
	private boolean requireRedstone;

	private WorkingFluid fluid = WorkingFluid.EMPTY;

	@Override
	protected void animateWithTick(Level world, BlockPos pos) {

	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		this.getSteam(world, x, y, z);

		if (!world.isClientSide() && this.canMakeSteam(world, x, y, z)) {
			steam--;
			world.setBlock(x, y+1, z, ReactorBlocks.STEAM.getBlockInstance(), this.getSteamMetadata(), 3);
		}

		if (steam <= 0) {
			fluid = WorkingFluid.EMPTY;
		}

		if (DragonAPI.debugtest)
			steam = 3;
		//fluid = WorkingFluid.AMMONIA;
		//ReikaJavaLibrary.pConsole(steam, Dist.DEDICATED_SERVER);
	}

	private boolean canMakeSteam(Level world, int x, int y, int z) {
		if (steam <= 0)
			return false;
		if (this.hasRedstoneSignal() != requireRedstone)
			return false;
		if (AtmosphereHandler.isNoAtmo(world, x, y+1, z, blockType, false))
			return false;
		return ((BlockSteam)ReactorBlocks.STEAM.getBlockInstance()).canMoveInto(world, x, y+1, z);
	}

	private Direction getFacing(int meta) {
		switch(meta) {
			case 0:
				return Direction.EAST;
			case 1:
				return Direction.WEST;
			case 2:
				return Direction.SOUTH;
			case 3:
				return Direction.NORTH;
			default:
				return Direction.UNKNOWN;
		}
	}

	private int getSteamMetadata() {
		if (fluid == WorkingFluid.AMMONIA)
			return 7;
		return 3;
	}

	private boolean canTakeInWorkingFluid(WorkingFluid f) {
		if (f == WorkingFluid.EMPTY)
			return false;
		if (fluid == WorkingFluid.EMPTY)
			return true;
		if (fluid == f)
			return true;
		return false;
	}

	private void getSteam(Level world, int x, int y, int z) {
		for (int i = 0; i < 6; i++) {
			Direction dir = dirs[i];
			int dx = x+dir.offsetX;
			int dy = y+dir.offsetY;
			int dz = z+dir.offsetZ;
			ReactorTiles rt = ReactorTiles.getTE(world, dx, dy, dz);
			if (rt == ReactorTiles.STEAMLINE) {
				TileEntitySteamLine te = (TileEntitySteamLine)world.getBlockEntity(dx, dy, dz);
				if (this.canTakeInWorkingFluid(te.getWorkingFluid())) {
					fluid = te.getWorkingFluid();
					int ds = te.getSteam()-steam;
					if (ds > 0) {
						int rm = ds/4+1;
						steam += rm;
						te.removeSteam(rm);
					}
				}
			}
		}
	}

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.GRATE;
	}

	@Override
	protected void readSyncTag(CompoundTag NBT)
	{
		super.readSyncTag(NBT);

		steam = NBT.getIntOr("energy", 0);

		fluid = WorkingFluid.getFromNBT(NBT);

		requireRedstone = NBT.getBooleanOr("red", false);
	}

	@Override
	protected void writeSyncTag(CompoundTag NBT)
	{
		super.writeSyncTag(NBT);

		NBT.putInt("energy", steam);

		fluid.saveToNBT(NBT);

		NBT.putBoolean("red", requireRedstone);
	}

	@Override
	public boolean onShiftRightClick(Level world, int x, int y, int z, Direction side) {
		return requireRedstone = !requireRedstone;
	}

	@Override
	public boolean onRightClick(Level world, int x, int y, int z, Direction side) {
		return false;
	}

	@Override
	public int getSteam() {
		return steam;
	}

}
