/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2017
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.auxiliary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

import reika.dragonapi.DragonAPI;
import reika.dragonapi.instantiable.data.immutable.WorldLocation;
import reika.dragonapi.instantiable.data.maps.TileEntityCache;
import reika.reactorcraft.tileentities.fission.TileEntityCPU;
import reika.reactorcraft.tileentities.fission.TileEntityControlRod;


public class ReactorControlLayout {

	private WorldLocation controller;

	private final TileEntityCache<TileEntityControlRod> controls = new TileEntityCache();
	private int minX = Integer.MAX_VALUE;
	private int minY = Integer.MAX_VALUE;
	private int minZ = Integer.MAX_VALUE;
	private int maxX = Integer.MIN_VALUE;
	private int maxY = Integer.MIN_VALUE;
	private int maxZ = Integer.MIN_VALUE;

	public ReactorControlLayout(TileEntityCPU cpu) {
		controller = new WorldLocation(cpu);
	}

	public int getSizeX() {
		return maxX-minX+1;
	}

	public int getSizeY() {
		return maxY-minY+1;
	}

	public int getSizeZ() {
		return maxZ-minZ+1;
	}

	public void addControlRod(TileEntityControlRod rod) {
		controls.put(rod);
		this.updateLimit(rod);
	}

	private void updateLimit(TileEntityControlRod rod) {
		if (minX > rod.xCoord)
			minX = rod.xCoord;
		if (maxX < rod.xCoord)
			maxX = rod.xCoord;
		if (minY > rod.yCoord)
			minY = rod.yCoord;
		if (maxY < rod.yCoord)
			maxY = rod.yCoord;
		if (minZ > rod.zCoord)
			minZ = rod.zCoord;
		if (maxZ < rod.zCoord)
			maxZ = rod.zCoord;
	}

	public void removeControlRod(TileEntityControlRod rod) {
		controls.remove(rod);
		if (minX == rod.xCoord || maxX == rod.xCoord || minY == rod.yCoord || maxY == rod.yCoord || minZ == rod.zCoord || maxZ == rod.zCoord)
			this.recalcLimits();
	}

	private void recalcLimits() {
		minX = Integer.MAX_VALUE;
		minY = Integer.MAX_VALUE;
		minZ = Integer.MAX_VALUE;
		maxX = Integer.MIN_VALUE;
		maxY = Integer.MIN_VALUE;
		maxZ = Integer.MIN_VALUE;
		for (TileEntityControlRod te : controls.values()) {
			this.updateLimit(te);
		}
	}

	public boolean hasControlRodAtRelativePosition(Level world, int x, int y, int z) {
		return this.getControlRodAtRelativePosition(world, x, y, z) != ItemStack.EMPTY;
	}

	public boolean hasControlRodAtAbsolutePosition(Level world, int x, int y, int z) {
		return this.getControlRodAtAbsolutePosition(world, x, y, z) != ItemStack.EMPTY;
	}

	public TileEntityControlRod getControlRodAtRelativePosition(Level world, int x, int y, int z) {
		return controls.get(new WorldLocation(world, x+controller.xCoord, y+controller.yCoord, z+controller.zCoord));
	}

	public TileEntityControlRod getControlRodAtAbsolutePosition(Level world, int x, int y, int z) {
		return controls.get(new WorldLocation(world, x, y, z));
	}

	public int getMinX() {
		return minX-controller.xCoord;
	}

	public int getMaxX() {
		return maxX-controller.xCoord;
	}

	public int getMinY() {
		return minY-controller.yCoord;
	}

	public int getMaxY() {
		return maxY-controller.yCoord;
	}

	public int getMinZ() {
		return minZ-controller.zCoord;
	}

	public int getMaxZ() {
		return maxZ-controller.zCoord;
	}

	@SideOnly(Dist.CLIENT)
	public int getDisplayColorAtRelativePosition(Level world, int x, int y, int z) {
		TileEntityControlRod rod = this.getControlRodAtRelativePosition(world, x, y, z);
		if (rod != null) {
			if (((TileEntityCPU)controller.getBlockEntity(world)).getPower() >= this.getMinPower())
				return rod.isActive() ? 0x00ff00 : 0xff0000;
			else
				return 0xa0a0a0;
		}
		return 0x6a6a6a;
	}

	public long getMinPower() {
		return DragonAPI.debugtest ? 0 : this.getPowerPerRod()*controls.size();
	}

	private long getPowerPerRod() {
		return TileEntityCPU.POWERPERROD;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(controller.toString());
		sb.append(" ");
		sb.append(controls);
		return sb.toString();
	}

	public void SCRAM() {
		int iter = 0;
		for (WorldLocation c : controls.keySet()) {
			TileEntityControlRod rod = controls.get(c);
			rod.drop(iter == 0);
			iter++;
		}
	}

	public void clear() {
		controls.clear();
		minX = Integer.MAX_VALUE;
		minY = Integer.MAX_VALUE;
		minZ = Integer.MAX_VALUE;
		maxX = Integer.MIN_VALUE;
		maxY = Integer.MIN_VALUE;
		maxZ = Integer.MIN_VALUE;
	}

	public int getNumberRods() {
		return controls.size();
	}

	public Collection<TileEntityControlRod> getAllRods() {
		return Collections.unmodifiableCollection(controls.values());
	}

	public TileEntityControlRod getRandomRod(Random rand) {
		ArrayList<TileEntityControlRod> li = new ArrayList(controls.values());
		return li.get(rand.nextInt(li.size()));
	}

	public int countLoweredRods() {
		int count = 0;
		for (WorldLocation c : controls.keySet()) {
			TileEntityControlRod rod = controls.get(c);
			if (rod.isActive())
				count++;
		}
		return count;
	}

	public boolean isEmpty() {
		return controls.isEmpty();
	}

	public void writeToNBT(CompoundTag NBT) {
		controls.writeToNBT(NBT);
		controller.saveAdditional(/*PORT*/"control", NBT);
		NBT.setInteger("maxx", maxX);
		NBT.setInteger("maxy", maxY);
		NBT.setInteger("maxz", maxZ);
		NBT.setInteger("minx", minX);
		NBT.setInteger("miny", minY);
		NBT.setInteger("minz", minZ);
	}

	public void readFromNBT(CompoundTag NBT) {
		controls.readFromNBT(NBT);
		controller = WorldLocation.load("control", NBT);
		maxX = NBT.getInteger("maxx");
		maxY = NBT.getInteger("maxy");
		maxZ = NBT.getInteger("maxz");
		minX = NBT.getInteger("minx");
		minY = NBT.getInteger("miny");
		minZ = NBT.getInteger("minz");
	}

}
