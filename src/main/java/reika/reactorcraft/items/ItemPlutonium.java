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

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import reika.reactorcraft.auxiliary.RadiationEffects;
import reika.reactorcraft.auxiliary.RadiationEffects.RadiationIntensity;
import reika.reactorcraft.registry.ReactorAchievements;

public class ItemPlutonium extends ItemReactorFuel {

	public ItemPlutonium(Properties properties, int dataValues) {
		super(properties, dataValues);
	}

	@Override
	public void inventoryTick(ItemStack is, ServerLevel world, Entity e, EquipmentSlot slot) {
		if (e instanceof Player ep) {
			if (!ep.isCreative()) {
				if (RadiationEffects.instance.applyEffects(ep, RadiationIntensity.MODERATE)) {
					ReactorAchievements.PUPOISON.triggerAchievement(ep);
				}
			}
		}
	}
}
