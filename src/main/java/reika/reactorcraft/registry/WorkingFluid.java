/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.registry;

import java.util.Locale;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public enum WorkingFluid {

	EMPTY(0, 0, ""),
	WATER(1F, 100, "water"),
	AMMONIA(2, -33, "rc ammonia");

	public final float efficiency;
	public final int boilingTemp;
	private final String fluidName;

	public static final WorkingFluid[] list = values();

	private WorkingFluid(float e, int boil, String f) {
		efficiency = e;
		boilingTemp = boil;
		fluidName = f;
	}

	public Fluid getFluid() {
		if (fluidName.isEmpty())
			return null;
		if ("water".equals(fluidName))
			return Fluids.WATER;
		return ReactorFluids.getLegacyFluid(fluidName);
	}

	public Fluid getLowPressureFluid() {
		if (this == WATER)
			return ReactorFluids.LOWP_WATER.get();
		if (this == AMMONIA)
			return ReactorFluids.LOWP_AMMONIA.get();
		return null;
	}

	public static WorkingFluid getFromNBT(CompoundTag tag) {
		int val = tag.getIntOr("workingfluid", 0);
		if (val >= 0 && val < list.length)
			return list[val];
		return EMPTY;
	}

	public void saveToNBT(CompoundTag tag) {
		tag.putInt("workingfluid", this.ordinal());
	}

	public static WorkingFluid getWorkingFluid(Fluid f) {
		if (f == null)
			return null;
		if (f == ReactorFluids.HEAVY_WATER.get() || f == Fluids.WATER)
			return WATER;
		for (WorkingFluid wf : list) {
			Fluid fl = wf.getFluid();
			if (f.equals(fl))
				return wf;
		}
		return null;
	}
}
