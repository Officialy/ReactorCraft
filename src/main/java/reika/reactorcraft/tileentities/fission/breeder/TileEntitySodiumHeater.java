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
import net.minecraft.core.BlockPos;

import net.minecraft.world.level.block.state.BlockState;
import reika.reactorcraft.registry.ReactorBlockEntities;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

import reika.dragonapi.libraries.java.ReikaRandomHelper;
import reika.dragonapi.libraries.mathsci.ReikaThermoHelper;
import reika.reactorcraft.registry.ReactorFluids;
import reika.reactorcraft.base.TileEntityIntermediateBoiler;
import reika.reactorcraft.entities.EntityNeutron;
import reika.reactorcraft.entities.EntityNeutron.NeutronType;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.registry.ReactorType;

public class TileEntitySodiumHeater extends TileEntityIntermediateBoiler {
	public TileEntitySodiumHeater(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.SODIUMBOILER.get(), pos, state);
	}


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
		return ReactorFluids.getLegacyFluid("rc sodium");
	}

	@Override
	protected Fluid getOutputFluid() {
		return ReactorFluids.HOT_SODIUM.get();
	}

	@Override
	protected void overheat(Level world, BlockPos pos) {
		world.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4, Level.ExplosionInteraction.BLOCK);
		// MOD-PORT: scrap/ironscrap drops (RotaryCraft ItemStacks) restored when those items are ported.
	}

	@Override
	public boolean onNeutron(EntityNeutron e, Level world, BlockPos pos) {
		NeutronType type = e.getNeutronType();
		return !tank.isEmpty() && ReikaRandomHelper.doWithChance(type.getSodiumBoilerAbsorptionChance());
	}

	@Override
	public ReactorType getDefaultReactorType() {
		return ReactorType.BREEDER;
	}

}
