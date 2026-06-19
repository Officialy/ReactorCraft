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

import reika.dragonapi.DragonAPICore;
import reika.dragonapi.modinteract.AtmosphereHandler;
import reika.reactorcraft.auxiliary.SteamTile;
import reika.reactorcraft.base.TileEntityReactorBase;
import reika.reactorcraft.blocks.BlockSteam;
import reika.reactorcraft.registry.ReactorBlocks;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.registry.WorkingFluid;
import reika.rotarycraft.api.interfaces.Screwdriverable;

public class TileEntitySteamGrate extends TileEntityReactorBase implements Screwdriverable, SteamTile {

	private int steam;
	private boolean requireRedstone;

	private WorkingFluid fluid = WorkingFluid.EMPTY;

	@Override
	protected void animateWithTick(Level world, int x, int y, int z) {

	}

	@Override
	public void updateEntity(Level world, int x, int y, int z, int meta) {
		this.getSteam(world, x, y, z);

		if (!world.isRemote && this.canMakeSteam(world, x, y, z)) {
			steam--;
			world.setBlock(x, y+1, z, ReactorBlocks.STEAM.getBlockInstance(), this.getSteamMetadata(), 3);
		}

		if (steam <= 0) {
			fluid = WorkingFluid.EMPTY;
		}

		if (DragonAPICore.debugtest)
			steam = 3;
		//fluid = WorkingFluid.AMMONIA;
		//ReikaJavaLibrary.pConsole(steam, Side.SERVER);
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

	private ForgeDirection getFacing(int meta) {
		switch(meta) {
			case 0:
				return ForgeDirection.EAST;
			case 1:
				return ForgeDirection.WEST;
			case 2:
				return ForgeDirection.SOUTH;
			case 3:
				return ForgeDirection.NORTH;
			default:
				return ForgeDirection.UNKNOWN;
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
			ForgeDirection dir = dirs[i];
			int dx = x+dir.offsetX;
			int dy = y+dir.offsetY;
			int dz = z+dir.offsetZ;
			ReactorTiles rt = ReactorTiles.getTE(world, dx, dy, dz);
			if (rt == ReactorTiles.STEAMLINE) {
				TileEntitySteamLine te = (TileEntitySteamLine)world.getTileEntity(dx, dy, dz);
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

		steam = NBT.getInteger("energy");

		fluid = WorkingFluid.getFromNBT(NBT);

		requireRedstone = NBT.getBoolean("red");
	}

	@Override
	protected void writeSyncTag(CompoundTag NBT)
	{
		super.writeSyncTag(NBT);

		NBT.setInteger("energy", steam);

		fluid.saveToNBT(NBT);

		NBT.setBoolean("red", requireRedstone);
	}

	@Override
	public boolean onShiftRightClick(Level world, int x, int y, int z, ForgeDirection side) {
		return requireRedstone = !requireRedstone;
	}

	@Override
	public boolean onRightClick(Level world, int x, int y, int z, ForgeDirection side) {
		return false;
	}

	@Override
	public int getSteam() {
		return steam;
	}

}
