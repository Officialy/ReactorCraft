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

import net.minecraft.world.level.block.state.BlockState;
import reika.reactorcraft.registry.ReactorBlockEntities;

import net.minecraft.world.level.Level;

import reika.reactorcraft.auxiliary.ReactorCoreTE;
import reika.reactorcraft.base.TileEntityReactorBase;
import reika.reactorcraft.entities.EntityNeutron;
import reika.reactorcraft.registry.ReactorTiles;

public class TileEntityNeutronReflector extends TileEntityReactorBase implements ReactorCoreTE {
	public TileEntityNeutronReflector(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.REFLECTOR.get(), pos, state);
	}


	@Override
	public boolean onNeutron(EntityNeutron e, Level world, BlockPos pos) {
		e.moderate();
		if (rand.nextInt(4) == 0) {
			e.motionX = -e.motionX;
			e.motionZ = -e.motionZ;
			e.velocityChanged = true;
			return false;
		}
		else
			return rand.nextBoolean();
	}

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.REFLECTOR;
	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {

	}

	@Override
	protected void animateWithTick(Level world, BlockPos pos) {

	}

	@Override
	protected boolean isTickingTE() {
		return false;
	}

}
