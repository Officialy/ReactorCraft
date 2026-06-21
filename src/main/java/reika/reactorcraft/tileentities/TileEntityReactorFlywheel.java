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

import net.minecraft.world.level.block.state.BlockState;
import reika.reactorcraft.registry.ReactorBlockEntities;

import net.minecraft.world.level.block.Block;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;

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
	public TileEntityReactorFlywheel(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.FLYWHEEL.get(), pos, state);
	}


	private int iotick;
	private long power;
	private int omega;
	private int torque;

	private Direction facing;

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

	public Direction getFacing() {
		return facing != null ? facing : Direction.EAST;
	}

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.FLYWHEEL;
	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		facing = this.setFacing(meta);
		int dx = x+this.getFacing().offsetX;
		int dy = y+this.getFacing().offsetY;
		int dz = z+this.getFacing().offsetZ;
		ReactorTiles r = ReactorTiles.getTE(world, dx, dy, dz);
		if (r != null && r.isTurbine()) {
			TileEntityTurbineCore te = (TileEntityTurbineCore)world.getBlockEntity(dx, dy, dz);
			//if (te.getOmega() > omega && omega < MAXSPEED && te.getTorque() >= MINTORQUE) {
			//	omega++;
			//}
			//else if (omega > 0) {
			//	omega--;
			//}
			//torque = te.getTorque();
			//ReikaJavaLibrary.pConsole(torque+"/"+te.getTorque()+":"+omega+"/"+te.getOmega(), Dist.DEDICATED_SERVER);
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

	private Direction setFacing(int meta) {
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
				return null;
		}
	}

	@Override
	protected void animateWithTick(Level world, BlockPos pos) {
		int dx = x+this.getFacing().offsetX;
		int dy = y+this.getFacing().offsetY;
		int dz = z+this.getFacing().offsetZ;
		ReactorTiles r = ReactorTiles.getTE(world, dx, dy, dz);
		if (r != null && r.isTurbine()) {
			TileEntityTurbineCore te = (TileEntityTurbineCore)world.getBlockEntity(dx, dy, dz);
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
	public boolean canWriteTo(Direction from) {
		Direction dir = this.getFacing().getOpposite();
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
	public boolean onShiftRightClick(Level world, int x, int y, int z, Direction side) {
		return false;
	}

	@Override
	public boolean onRightClick(Level world, int x, int y, int z, Direction side) {
		int meta = this;
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

		facing = dirs[NBT.getIntOr("face", 0)];
		hasMultiBlock = NBT.getBooleanOr("multi", false);

		power = NBT.getLongOr("pwr", 0L);
	}

	@Override
	protected void writeSyncTag(CompoundTag NBT) {
		super.writeSyncTag(NBT);

		NBT.putInt("face", this.getFacing().ordinal());
		NBT.putBoolean("multi", hasMultiBlock);

		NBT.putLong("pwr", power);
	}

	@Override
	public void breakBlock() {
		if (!level.isRemote) {
			for (int i = 0; i < 6; i++) {
				Direction dir = dirs[i];
				int dx = xCoord+dir.offsetX;
				int dy = yCoord+dir.offsetY;
				int dz = zCoord+dir.offsetZ;
				Block b = level.getBlock(dx, dy, dz);
				if (b instanceof BlockReCMultiBlock) {
					((BlockReCMultiBlock)b).breakMultiBlock(level, dx, dy, dz);
				}
			}
		}
	}

}
