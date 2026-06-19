/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.tileentities.fission.breeder;

import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import reika.dragonapi.libraries.java.ReikaRandomHelper;
import reika.dragonapi.libraries.mathsci.ReikaThermoHelper;
import reika.dragonapi.libraries.registry.ReikaItemHelper;
import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.base.TileEntityIntermediateBoiler;
import reika.reactorcraft.entities.EntityNeutron;
import reika.reactorcraft.entities.EntityNeutron.NeutronType;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.registry.ReactorType;
import reika.rotarycraft.auxiliary.ItemStacks;

public class TileEntitySodiumHeater extends TileEntityIntermediateBoiler {

	@Override
	public int getLiquidUsage() {
		return 100;
	}

	@Override
	public int getMinimumTemperature() {
		return 301;
	}

	@Override
	protected double getFluidHeatCapacity() {
		return ReikaThermoHelper.SODIUM_HEAT;
	}

	@Override
	public int getMaxTemperature() {
		return 2000;
	}

	@Override
	public int getCapacity() {
		return 12000;
	}

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.SODIUMBOILER;
	}

	@Override
	public Fluid getInputFluid() {
		return FluidRegistry.getFluid("rc sodium");
	}

	@Override
	protected Fluid getOutputFluid() {
		return ReactorCraft.NA_hot;
	}

	@Override
	protected void overheat(Level world, int x, int y, int z) {
		world.createExplosion(null, x+0.5, y+0.5, z+0.5, 4, true);
		for (int i = 0; i < 4; i++) {
			ReikaItemHelper.dropItem(world, x+rand.nextDouble(), y+rand.nextDouble(), z+rand.nextDouble(), ItemStacks.scrap);
			ReikaItemHelper.dropItem(world, x+rand.nextDouble(), y+rand.nextDouble(), z+rand.nextDouble(), ItemStacks.ironscrap);
		}
	}

	@Override
	public boolean onNeutron(EntityNeutron e, Level world, int x, int y, int z) {
		NeutronType type = e.getType();
		return !tank.isEmpty() && ReikaRandomHelper.doWithChance(type.getSodiumBoilerAbsorptionChance());
	}

	@Override
	public ReactorType getDefaultReactorType() {
		return ReactorType.BREEDER;
	}

}
