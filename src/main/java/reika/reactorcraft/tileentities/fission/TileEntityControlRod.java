/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.tileentities.fission;

import net.minecraft.world.level.block.state.BlockState;
import reika.reactorcraft.registry.ReactorBlockEntities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;

import reika.dragonapi.instantiable.data.immutable.Coordinate;
import reika.dragonapi.libraries.ReikaAABBHelper;
import reika.dragonapi.libraries.java.ReikaRandomHelper;
import reika.reactorcraft.auxiliary.LinkableReactorCore;
import reika.reactorcraft.base.TileEntityReactorBase;
import reika.reactorcraft.entities.EntityNeutron;
import reika.reactorcraft.registry.ReactorSounds;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.registry.ReactorType;
import reika.reactorcraft.tileentities.fission.TileEntityWaterCell.LiquidStates;

public class TileEntityControlRod extends TileEntityReactorBase implements LinkableReactorCore {
	public TileEntityControlRod(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.CONTROL.get(), pos, state);
	}


	private boolean lowered = true;
	private Motions motion;

	private static final int MINOFFSET = -5;
	private static final int MAXOFFSET = 20;

	private int rodOffset = MINOFFSET;

	private Coordinate CPU;

	public void link(TileEntityCPU te) {
		CPU = new Coordinate(te);
	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		this.moveRods();
		thermalTicker.update();
		if (thermalTicker.checkCap())
			this.updateTemperature(world, x, y, z);
	}

	private void moveRods() {
		if (motion != null) {
			rodOffset += motion.stepHeight;
		}
		if (rodOffset <= MINOFFSET || rodOffset >= MAXOFFSET) {
			motion = ItemStack.EMPTY;
			rodOffset = Math.max(MINOFFSET, rodOffset);
			rodOffset = Math.min(MAXOFFSET, rodOffset);
			lowered = rodOffset == MINOFFSET;
		}
	}

	@Override
	protected void animateWithTick(Level world, BlockPos pos) {

	}

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.CONTROL;
	}

	public void toggle(boolean sound, boolean spread) {
		if (lowered) {
			motion = Motions.RAISING;
		}
		else {
			motion = Motions.LOWERING;
		}

		if (spread) {
			BlockEntity te = this.getAdjacentTileEntity(Direction.UP);
			while (te instanceof TileEntityControlRod) {
				TileEntityControlRod tc = (TileEntityControlRod)te;
				tc.toggle(false, false);
				te = tc.getAdjacentTileEntity(Direction.UP);
			}
			te = this.getAdjacentTileEntity(Direction.DOWN);
			while (te instanceof TileEntityControlRod) {
				TileEntityControlRod tc = (TileEntityControlRod)te;
				tc.toggle(false, false);
				te = tc.getAdjacentTileEntity(Direction.DOWN);
			}
		}

		if (sound)
			ReactorSounds.CONTROL.playSoundAtBlock(level, xCoord, yCoord, zCoord, 1, 1.3F);
	}

	public void setActive(boolean active, boolean sound) {
		motion = active ? Motions.LOWERING : Motions.RAISING;
		if (sound)
			ReactorSounds.CONTROL.playSoundAtBlock(level, xCoord, yCoord, zCoord, 1, 1.3F);
	}

	public void drop(boolean sound) {
		if (sound)
			if (rodOffset > MINOFFSET && motion != Motions.SCRAM)
				ReactorSounds.SCRAM.playSoundAtBlock(level, xCoord, yCoord, zCoord, 1, 1F);
		motion = Motions.SCRAM;
	}

	public boolean isActive() {
		return lowered && rodOffset == MINOFFSET;
	}

	@Override
	public boolean onNeutron(EntityNeutron e, Level world, BlockPos pos) {
		return this.isActive() ? ReikaRandomHelper.doWithChance(60) : false;
	}

	@Override
	public int getTemperature() {
		return temperature;
	}

	@Override
	public void setTemperature(int T) {
		temperature = T;
	}

	@Override
	protected void writeSyncTag(CompoundTag NBT)
	{
		super.writeSyncTag(NBT);

		NBT.putBoolean("down", lowered);

		if (motion != null)
			NBT.putInt("motion", motion.ordinal());

		NBT.putInt("rodoffset", rodOffset);

		if (CPU != null)
			CPU.saveAdditional("cpu", NBT);
	}

	@Override
	protected void readSyncTag(CompoundTag NBT)
	{
		super.readSyncTag(NBT);

		lowered = NBT.getBooleanOr("down", false);

		if (NBT.hasKey("motion"))
			motion = Motions.values()[NBT.getIntOr("motion", 0)];

		rodOffset = NBT.getIntOr("rodoffset", 0);

		CPU = Coordinate.load("cpu", NBT);
	}

	@Override
	public int getMaxTemperature() {
		return 0;
	}

	private void onMeltdown(Level world, int x, int y, int z) {

	}

	@Override
	public boolean canDumpHeatInto(LiquidStates liq) {
		return liq.isWater();
	}

	@Override
	public int getTextureState(Direction side) {
		return this.isActive() ? 1 : 0;
	}

	public int getRodPosition() {
		return rodOffset;
	}

	@Override
	public void breakBlock() {
		if (CPU != null) {
			BlockEntity te = CPU.getBlockEntity(level);
			if (te instanceof TileEntityCPU) {
				((TileEntityCPU)te).getLayout().removeControlRod(this);
				((TileEntityCPU)te).removeTemperatureCheck(this);
			}
		}
	}

	@Override
	public ReactorType getReactorType() {
		return ReactorType.FISSION;
	}

	@Override
	public AABB getRenderBoundingBox() {
		return ReikaAABBHelper.getBlockAABB(this).addCoord(0, 2, 0);
	}

	private static enum Motions {
		RAISING(1),
		LOWERING(-1),
		SCRAM(-7);

		public final int stepHeight;

		private Motions(int dh) {
			stepHeight = dh;
		}
	}

}
