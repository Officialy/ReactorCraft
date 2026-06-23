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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

import reika.reactorcraft.base.TileEntityReactorPiping;
import reika.reactorcraft.registry.ReactorBlockEntities;
import reika.reactorcraft.registry.ReactorTiles;

public class TileEntityGasDuct extends TileEntityReactorPiping {

	public TileEntityGasDuct(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.GASPIPE.get(), pos, state);
	}

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.GASPIPE;
	}

	@Override
	public boolean isValidFluid(Fluid f) {
		// Gases have a negative fluid-type density (lighter than air).
		return f.getFluidType().getDensity() < 0;
	}

	@Override
	protected void onIntake(BlockEntity te) {

	}

}
