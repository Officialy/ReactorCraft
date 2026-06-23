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

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import reika.reactorcraft.registry.ReactorBlockEntities;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

import reika.dragonapi.libraries.mathsci.ReikaThermoHelper;
import reika.dragonapi.libraries.registry.ReikaItemHelper;
import reika.reactorcraft.base.TileEntityIntermediateBoiler;
import reika.reactorcraft.registry.ReactorFluids;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.registry.ReactorType;
import reika.rotarycraft.registry.RotaryItems;

public class TileEntityCO2Heater extends TileEntityIntermediateBoiler {
	public TileEntityCO2Heater(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.CO2HEATER.get(), pos, state);
	}


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
		return ReactorFluids.getLegacyFluid("rc co2");
	}

	@Override
	protected Fluid getOutputFluid() {
		return ReactorFluids.getLegacyFluid("rc hot co2");
	}

	@Override
	protected void overheat(Level world, BlockPos pos) {
		world.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4, Level.ExplosionInteraction.BLOCK);
		for (int i = 0; i < 4; i++)
			ReikaItemHelper.dropItem(world, pos.getX() + rand.nextDouble(), pos.getY() + rand.nextDouble(), pos.getZ() + rand.nextDouble(), RotaryItems.HSLA_STEEL_SCRAP.toStack());
	}

	@Override
	public ReactorType getDefaultReactorType() {
		return ReactorType.HTGR;
	}

}
