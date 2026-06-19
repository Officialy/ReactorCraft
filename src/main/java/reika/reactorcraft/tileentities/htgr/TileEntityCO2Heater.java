/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.tileentities.htgr;

import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import reika.dragonapi.libraries.mathsci.ReikaThermoHelper;
import reika.dragonapi.libraries.registry.ReikaItemHelper;
import reika.reactorcraft.base.TileEntityIntermediateBoiler;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.registry.ReactorType;
import reika.rotarycraft.auxiliary.ItemStacks;

public class TileEntityCO2Heater extends TileEntityIntermediateBoiler {

	@Override
	public int getLiquidUsage() {
		return 100;
	}

	@Override
	public int getMinimumTemperature() {
		return TileEntityPebbleBed.MINTEMP;
	}

	@Override
	protected double getFluidHeatCapacity() {
		return ReikaThermoHelper.CO2_HEAT;
	}

	@Override
	public int getMaxTemperature() {
		return 3000;
	}

	@Override
	public int getCapacity() {
		return 12000;
	}

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.CO2HEATER;
	}

	@Override
	public Fluid getInputFluid() {
		return FluidRegistry.getFluid("rc co2");
	}

	@Override
	protected Fluid getOutputFluid() {
		return FluidRegistry.getFluid("rc hot co2");
	}

	@Override
	protected void overheat(Level world, int x, int y, int z) {
		world.createExplosion(null, x+0.5, y+0.5, z+0.5, 4, true);
		for (int i = 0; i < 4; i++)
			ReikaItemHelper.dropItem(world, x+rand.nextDouble(), y+rand.nextDouble(), z+rand.nextDouble(), ItemStacks.scrap);
	}

	@Override
	public ReactorType getDefaultReactorType() {
		return ReactorType.HTGR;
	}

}
