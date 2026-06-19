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

import net.minecraft.world.level.block.Block;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.ForgeDirection;

import reika.reactorcraft.auxiliary.MultiBlockTile;
import reika.reactorcraft.base.BlockReCMultiBlock;
import reika.reactorcraft.base.TileEntityReactorBase;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.tileentities.powergen.TileEntityHiPTurbine;
import reika.reactorcraft.tileentities.powergen.TileEntityTurbineCore;
import reika.rotarycraft.api.interfaces.Screwdriverable;
import reika.rotarycraft.api.power.ShaftPowerReceiver;
import reika.rotarycraft.auxiliary.ShaftPowerEmitter;

public class TileEntityReactorFlywheel extends TileEntityReactorBase implements ShaftPowerEmitter, Screwdriverable, MultiBlockTile {

	private int iotick;
	private long power;
	private int omega;
	private int torque;

	private ForgeDirection facing;

	private boolean hasMultiBlock = false;

	//public static final int MAXSPEED = 8192;
	//public static final int MINTORQUE = 32768;
	private static final int MAXTORQUE = 12750;
	private static final int MAXTORQUE_AMMONIA = MAXTORQUE*2;

	public boolean hasMultiBlock() {
		return hasMultiBlock;
	}

	public void setHasMultiBlock(boolean has) {
		hasMultiBlock = has;
	}

	public ForgeDirection getFacing() {
		return facing != null ? facing : ForgeDirection.EAST;
	}

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.FLYWHEEL;
	}

	@Override
	public void updateEntity(Level world, int x, int y, int z, int meta) {
		facing = this.setFacing(meta);
		int dx = x+this.getFacing().offsetX;
		int dy = y+this.getFacing().offsetY;
		int dz = z+this.getFacing().offsetZ;
		ReactorTiles r = ReactorTiles.getTE(world, dx, dy, dz);
		if (r != null && r.isTurbine()) {
			TileEntityTurbineCore te = (TileEntityTurbineCore)world.getTileEntity(dx, dy, dz);
			//if (te.getOmega() > omega && omega < MAXSPEED && te.getTorque() >= MINTORQUE) {
			//	omega++;
			//}
			//else if (omega > 0) {
			//	omega--;
			//}
			//torque = te.getTorque();
			//ReikaJavaLibrary.pConsole(torque+"/"+te.getTorque()+":"+omega+"/"+te.getOmega(), Side.SERVER);
			omega = te.getOmega();
			torque = TileEntityReactorFlywheel.clampTorque(te);
		}
		else {
			if (omega > 0)
				omega -= (omega/32)+1;
		}
		if (omega <= 0) {
			torque = 0;
			omega = 0;
		}
		power = (long)omega*(long)torque;
		BlockEntity tg = this.getAdjacentTileEntity(this.getFacing().getOpposite());
		if (tg instanceof ShaftPowerReceiver) {
			ShaftPowerReceiver rec = (ShaftPowerReceiver)tg;
			rec.setOmega(this.getOmega());
			rec.setTorque(this.getTorque());
			rec.setPower(this.getPower());
		}
	}

	public static int clampTorque(TileEntityTurbineCore te) {
		return te instanceof TileEntityHiPTurbine ? te.getTorque() : Math.min(te.getTorque(), te.isAmmonia() ? MAXTORQUE_AMMONIA : MAXTORQUE);
	}

	private ForgeDirection setFacing(int meta) {
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
				return null;
		}
	}

	@Override
	protected void animateWithTick(Level world, int x, int y, int z) {
		int dx = x+this.getFacing().offsetX;
		int dy = y+this.getFacing().offsetY;
		int dz = z+this.getFacing().offsetZ;
		ReactorTiles r = ReactorTiles.getTE(world, dx, dy, dz);
		if (r != null && r.isTurbine()) {
			TileEntityTurbineCore te = (TileEntityTurbineCore)world.getTileEntity(dx, dy, dz);
			phi = te.phi*6;
		}
		iotick -= 8;
	}

	@Override
	public int getOmega() {
		return omega;
	}

	@Override
	public int getTorque() {
		return torque;
	}

	@Override
	public long getPower() {
		return power;
	}

	@Override
	public int getIORenderAlpha() {
		return iotick;
	}

	@Override
	public void setIORenderAlpha(int io) {
		iotick = io;
	}

	@Override
	public long getMaxPower() {
		return Long.MAX_VALUE;
	}

	@Override
	public long getCurrentPower() {
		return power;
	}

	@Override
	public boolean canWriteTo(ForgeDirection from) {
		ForgeDirection dir = this.getFacing().getOpposite();
		return dir == from;
	}

	@Override
	public boolean isEmitting() {
		return this.hasMultiBlock();
	}

	@Override
	public int getEmittingX() {
		return xCoord+this.getFacing().getOpposite().offsetX;
	}

	@Override
	public int getEmittingY() {
		return yCoord+this.getFacing().getOpposite().offsetY;
	}

	@Override
	public int getEmittingZ() {
		return zCoord+this.getFacing().getOpposite().offsetZ;
	}

	@Override
	public boolean onShiftRightClick(Level world, int x, int y, int z, ForgeDirection side) {
		return false;
	}

	@Override
	public boolean onRightClick(Level world, int x, int y, int z, ForgeDirection side) {
		int meta = this.getBlockMetadata();
		if (this.hasMultiBlock()) {
			this.setBlockMetadata((meta-meta%2)+(1-(meta%2)));
		}
		else {
			if (meta < 3)
				this.setBlockMetadata(meta+1);
			else
				this.setBlockMetadata(0);
		}
		return true;
	}

	@Override
	protected void readSyncTag(CompoundTag NBT) {
		super.readSyncTag(NBT);

		facing = dirs[NBT.getInteger("face")];
		hasMultiBlock = NBT.getBoolean("multi");

		power = NBT.getLong("pwr");
	}

	@Override
	protected void writeSyncTag(CompoundTag NBT) {
		super.writeSyncTag(NBT);

		NBT.setInteger("face", this.getFacing().ordinal());
		NBT.setBoolean("multi", hasMultiBlock);

		NBT.setLong("pwr", power);
	}

	@Override
	public void breakBlock() {
		if (!worldObj.isRemote) {
			for (int i = 0; i < 6; i++) {
				ForgeDirection dir = dirs[i];
				int dx = xCoord+dir.offsetX;
				int dy = yCoord+dir.offsetY;
				int dz = zCoord+dir.offsetZ;
				Block b = worldObj.getBlock(dx, dy, dz);
				if (b instanceof BlockReCMultiBlock) {
					((BlockReCMultiBlock)b).breakMultiBlock(worldObj, dx, dy, dz);
				}
			}
		}
	}

}
