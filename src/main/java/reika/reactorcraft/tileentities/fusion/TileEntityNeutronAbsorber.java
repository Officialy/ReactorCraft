/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.tileentities.fusion;
import net.minecraft.core.BlockPos;

import net.minecraft.world.level.block.state.BlockState;
import reika.reactorcraft.registry.ReactorBlockEntities;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.Level;

import reika.dragonapi.instantiable.StepTimer;
import reika.dragonapi.libraries.io.ReikaSoundHelper;
import reika.reactorcraft.auxiliary.Temperatured;
import reika.reactorcraft.auxiliary.TypedReactorCoreTE;
import reika.reactorcraft.base.TileEntityReactorBase;
import reika.reactorcraft.entities.EntityNeutron;
import reika.reactorcraft.entities.EntityNeutron.NeutronType;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.registry.ReactorType;
import reika.reactorcraft.tileentities.fission.TileEntityWaterCell.LiquidStates;

public class TileEntityNeutronAbsorber extends TileEntityReactorBase implements Temperatured, TypedReactorCoreTE {
	public TileEntityNeutronAbsorber(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.ABSORBER.get(), pos, state);
	}


	private StepTimer tempTimer = new StepTimer(20);

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.ABSORBER;
	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		tempTimer.update();
		if (tempTimer.checkCap()) {
			this.updateTemperature(world, x, y, z);

			if (!world.isClientSide()) {
				if (temperature >= this.getMaxTemperature()) {
					world.setBlock(x, y, z, Blocks.flowing_lava);
					ReikaSoundHelper.playSoundAtBlock(world, x, y, z, "random.fizz");
				}
			}
		}
	}

	@Override
	protected void animateWithTick(Level world, BlockPos pos) {

	}

	@Override
	public boolean onNeutron(EntityNeutron e, Level world, BlockPos pos) {
		if (e.getType() == NeutronType.FUSION) {
			temperature += 40;
			return true;
		}
		return false;
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
	public int getMaxTemperature() {
		return 1500;
	}

	@Override
	public boolean canDumpHeatInto(LiquidStates liq) {
		return liq != LiquidStates.EMPTY;
	}

	@Override
	public ReactorType getReactorType() {
		return ReactorType.FUSION;
	}

}
