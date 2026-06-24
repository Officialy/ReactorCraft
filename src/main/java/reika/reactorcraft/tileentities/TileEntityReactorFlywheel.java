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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import reika.reactorcraft.auxiliary.MultiBlockTile;
import reika.reactorcraft.base.BlockReCMultiBlock;
import reika.reactorcraft.base.TileEntityReactorBase;
import reika.reactorcraft.registry.ReactorBlockEntities;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.tileentities.powergen.TileEntityHiPTurbine;
import reika.reactorcraft.tileentities.powergen.TileEntityTurbineCore;
import reika.rotarycraft.api.interfaces.Screwdriverable;
import reika.rotarycraft.api.power.ShaftPowerReceiver;
import reika.rotarycraft.api.power.ShaftPowerEmitter;

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

	private static final int MAXTORQUE = 12750;
	private static final int MAXTORQUE_AMMONIA = MAXTORQUE*2;

	/** The flywheel only ever faces horizontally; index order mirrors the legacy meta 0..3 (E/W/S/N). */
	private static final Direction[] FACES = {Direction.EAST, Direction.WEST, Direction.SOUTH, Direction.NORTH};

	public boolean hasMultiBlock() {
		return hasMultiBlock;
	}

	public void setHasMultiBlock(boolean has) {
		hasMultiBlock = has;
	}

	public Direction getFacing() {
		return facing != null ? facing : Direction.EAST;
	}

	private int faceIndex() {
		Direction f = this.getFacing();
		for (int i = 0; i < FACES.length; i++)
			if (FACES[i] == f)
				return i;
		return 0;
	}

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.FLYWHEEL;
	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		BlockPos tp = pos.relative(this.getFacing());
		ReactorTiles r = ReactorTiles.getTE(world, tp);
		if (r != null && r.isTurbine()) {
			TileEntityTurbineCore te = (TileEntityTurbineCore)world.getBlockEntity(tp);
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
		BlockEntity tg = this.getAdjacentBlockEntity(this.getFacing().getOpposite());
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

	@Override
	protected void animateWithTick(Level world, BlockPos pos) {
		BlockPos tp = pos.relative(this.getFacing());
		ReactorTiles r = ReactorTiles.getTE(world, tp);
		if (r != null && r.isTurbine()) {
			TileEntityTurbineCore te = (TileEntityTurbineCore)world.getBlockEntity(tp);
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
		return this.getFacing().getOpposite() == from;
	}

	@Override
	public boolean isEmitting() {
		return this.hasMultiBlock();
	}

	@Override
	public BlockPos getEmittingPos(BlockPos pos) {
		return pos.relative(this.getFacing().getOpposite());
	}

	@Override
	public boolean onShiftRightClick(Level world, BlockPos pos, Direction side) {
		return false;
	}

	@Override
	public boolean onRightClick(Level world, BlockPos pos, Direction side) {
		int idx = this.faceIndex();
		if (this.hasMultiBlock()) {
			idx = (idx-idx%2)+(1-(idx%2));
		}
		else {
			idx = idx < 3 ? idx+1 : 0;
		}
		facing = FACES[idx];
		this.triggerBlockUpdate();
		return true;
	}

	@Override
	protected void readSyncTag(CompoundTag NBT) {
		super.readSyncTag(NBT);

		facing = Direction.values()[NBT.getIntOr("face", Direction.EAST.ordinal())];
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
		if (!level.isClientSide()) {
			BlockPos pos = this.getBlockPos();
			for (int i = 0; i < 6; i++) {
				Direction dir = dirs[i];
				BlockPos p = pos.relative(dir);
				Block b = level.getBlockState(p).getBlock();
				if (b instanceof BlockReCMultiBlock) {
					((BlockReCMultiBlock)b).breakMultiBlock(level, p.getX(), p.getY(), p.getZ());
				}
			}
		}
	}

}
