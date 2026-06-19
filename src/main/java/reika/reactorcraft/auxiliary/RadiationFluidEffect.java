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

import net.minecraft.entity.EntityLivingBase;

import reika.reactorcraft.auxiliary.RadiationEffects.RadiationIntensity;
import reika.rotarycraft.tileentities.storage.tileentityreservoir.FluidEffect;


public class RadiationFluidEffect implements FluidEffect {

	@Override
	public void applyEffect(EntityLivingBase e) {
		RadiationEffects.instance.applyEffects(e, RadiationIntensity.HIGHLEVEL);
	}

}
