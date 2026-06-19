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

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.ForgeDirection;

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
	public void updateEntity(Level world, int x, int y, int z, int meta) {
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
			motion = null;
			rodOffset = Math.max(MINOFFSET, rodOffset);
			rodOffset = Math.min(MAXOFFSET, rodOffset);
			lowered = rodOffset == MINOFFSET;
		}
	}

	@Override
	protected void animateWithTick(Level world, int x, int y, int z) {

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
			BlockEntity te = this.getAdjacentTileEntity(ForgeDirection.UP);
			while (te instanceof TileEntityControlRod) {
				TileEntityControlRod tc = (TileEntityControlRod)te;
				tc.toggle(false, false);
				te = tc.getAdjacentTileEntity(ForgeDirection.UP);
			}
			te = this.getAdjacentTileEntity(ForgeDirection.DOWN);
			while (te instanceof TileEntityControlRod) {
				TileEntityControlRod tc = (TileEntityControlRod)te;
				tc.toggle(false, false);
				te = tc.getAdjacentTileEntity(ForgeDirection.DOWN);
			}
		}

		if (sound)
			ReactorSounds.CONTROL.playSoundAtBlock(worldObj, xCoord, yCoord, zCoord, 1, 1.3F);
	}

	public void setActive(boolean active, boolean sound) {
		motion = active ? Motions.LOWERING : Motions.RAISING;
		if (sound)
			ReactorSounds.CONTROL.playSoundAtBlock(worldObj, xCoord, yCoord, zCoord, 1, 1.3F);
	}

	public void drop(boolean sound) {
		if (sound)
			if (rodOffset > MINOFFSET && motion != Motions.SCRAM)
				ReactorSounds.SCRAM.playSoundAtBlock(worldObj, xCoord, yCoord, zCoord, 1, 1F);
		motion = Motions.SCRAM;
	}

	public boolean isActive() {
		return lowered && rodOffset == MINOFFSET;
	}

	@Override
	public boolean onNeutron(EntityNeutron e, Level world, int x, int y, int z) {
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

		NBT.setBoolean("down", lowered);

		if (motion != null)
			NBT.setInteger("motion", motion.ordinal());

		NBT.setInteger("rodoffset", rodOffset);

		if (CPU != null)
			CPU.writeToNBT("cpu", NBT);
	}

	@Override
	protected void readSyncTag(CompoundTag NBT)
	{
		super.readSyncTag(NBT);

		lowered = NBT.getBoolean("down");

		if (NBT.hasKey("motion"))
			motion = Motions.values()[NBT.getInteger("motion")];

		rodOffset = NBT.getInteger("rodoffset");

		CPU = Coordinate.readFromNBT("cpu", NBT);
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
	public int getTextureState(ForgeDirection side) {
		return this.isActive() ? 1 : 0;
	}

	public int getRodPosition() {
		return rodOffset;
	}

	@Override
	public void breakBlock() {
		if (CPU != null) {
			BlockEntity te = CPU.getTileEntity(worldObj);
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
