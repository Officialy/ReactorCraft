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

import net.minecraft.world.entity.LivingEntity;

import reika.reactorcraft.auxiliary.RadiationEffects.RadiationIntensity;
import reika.rotarycraft.tileentities.storage.tileentityreservoir.FluidEffect;


public class RadiationFluidEffect implements FluidEffect {

	@Override
	public void applyEffect(LivingEntity e) {
		RadiationEffects.instance.applyEffects(e, RadiationIntensity.HIGHLEVEL);
	}

}
