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

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.phys.AABB;

import reika.dragonapi.libraries.mathsci.ReikaMathLibrary;
import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.base.ItemReactorTool;
import reika.reactorcraft.entities.EntityRadiation;
import reika.rotarycraft.api.interfaces.ChargeableTool;

public class ItemGeigerCounter extends ItemReactorTool implements ChargeableTool {

	public ItemGeigerCounter(Properties properties) {
		super(properties);
	}

	@Override
	public void inventoryTick(ItemStack is, ServerLevel world, Entity e, EquipmentSlot slot) {
		if (slot == EquipmentSlot.MAINHAND && is.getDamageValue() > 0) {
			int r = 20;
			if (e instanceof LivingEntity le && le.hasEffect(ReactorCraft.radiation))
				r = -1;
			AABB box = r >= 0 ? new AABB(e.getX(), e.getY(), e.getZ(), e.getX(), e.getY(), e.getZ()).inflate(r, r, r) : null;
			List<EntityRadiation> li = box == null ? null : world.getEntitiesOfClass(EntityRadiation.class, box);
			if (li == null) {
				e.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 2, 2F);
			}
			else if (!li.isEmpty()) {
				li.sort(Comparator.comparingDouble(er -> er.distanceToSqr(e)));
				EntityRadiation er = li.get(0);
				double dist = ReikaMathLibrary.py3d(e.getX()-er.getX(), e.getY()-er.getY(), e.getZ()-er.getZ());
				if (e.getRandom().nextDouble()*r*16 > dist*dist) {
					e.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1, 2F);
				}
			}
			if (e.getRandom().nextInt(8) == 0)
				is.setDamageValue(is.getDamageValue()-1);
		}
	}

	@Override
	public int setCharged(ItemStack is, int charge, boolean strongcoil) {
		int ret = is.getDamageValue();
		is.setDamageValue(charge);
		return ret;
	}

	@Override
	public void appendHoverText(ItemStack is, Item.TooltipContext ctx, TooltipDisplay display, Consumer<Component> li, TooltipFlag flag) {
		li.accept(Component.literal("Charge: "+is.getDamageValue()+" kJ"));
	}

}
