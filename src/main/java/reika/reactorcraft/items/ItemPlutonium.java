/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2017
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.items;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import reika.reactorcraft.auxiliary.RadiationEffects;
import reika.reactorcraft.auxiliary.RadiationEffects.RadiationIntensity;
import reika.reactorcraft.registry.ReactorAchievements;

public class ItemPlutonium extends ItemReactorFuel {

	public ItemPlutonium(int tex) {
		super(tex);
	}

	@Override
	public void onUpdate(ItemStack is, World world, Entity e, int slot, boolean flag) {
		if (e instanceof EntityPlayer) {
			EntityPlayer ep = (EntityPlayer)e;
			if (!ep.capabilities.isCreativeMode) {
				if (RadiationEffects.instance.applyEffects(ep, RadiationIntensity.MODERATE)) {
					ReactorAchievements.PUPOISON.triggerAchievement(ep);
				}
			}
		}
	}
}
