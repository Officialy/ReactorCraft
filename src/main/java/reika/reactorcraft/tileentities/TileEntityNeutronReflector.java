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

import net.minecraft.world.World;

import reika.reactorcraft.auxiliary.ReactorCoreTE;
import reika.reactorcraft.base.TileEntityReactorBase;
import reika.reactorcraft.entities.EntityNeutron;
import reika.reactorcraft.registry.ReactorTiles;

public class TileEntityNeutronReflector extends TileEntityReactorBase implements ReactorCoreTE {

	@Override
	public boolean onNeutron(EntityNeutron e, World world, int x, int y, int z) {
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
	public void updateEntity(World world, int x, int y, int z, int meta) {

	}

	@Override
	protected void animateWithTick(World world, int x, int y, int z) {

	}

	@Override
	protected boolean isTickingTE() {
		return false;
	}

}
