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

import java.util.function.Consumer;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import reika.dragonapi.libraries.mathsci.Isotopes;
import reika.dragonapi.libraries.mathsci.Isotopes.ElementGroup;
import reika.reactorcraft.auxiliary.RadiationEffects;
import reika.reactorcraft.auxiliary.RadiationEffects.RadiationIntensity;
import reika.reactorcraft.base.ItemReactorMulti;
import reika.reactorcraft.entities.EntityNuclearWaste;
import reika.reactorcraft.registry.ReactorAchievements;

/**
 * Nuclear waste — a stackable item whose reactor-isotope / element-group identity rides the
 * {@code getDamageValue()} variant (the same damage-value carrier as fuel burnup and magnet charge):
 * <ul>
 *   <li>{@code 0 .. Isotopes.values().length-1} — an individual isotope, by {@code Isotopes.ordinal()}
 *       (what {@code WasteManager.getWaste(Isotopes)} stamps when a reactor produces waste).</li>
 *   <li>{@code 1000 + ElementGroup.ordinal()} — mixed element-group waste, the centrifuge's first
 *       separation stage (what {@code WasteManager.getWaste(ElementGroup)} stamps).</li>
 * </ul>
 * The 1.7.10 {@code getSubItems}/{@code getTextureOffset} sprite-sheet scheme is gone: waste's original
 * {@code hasMetadataSprites()==false} means every variant shares the one {@code item/waste} sprite, and
 * the per-variant identity now shows only in the tooltip. Display name is the single {@code item.reactorcraft.waste}
 * lang key ("Nuclear Waste") for all variants, so no {@code getName} override is needed.
 */
public class ItemNuclearWaste extends ItemReactorMulti {

	// Widest damage value used: mixed-group waste at 1000 + ElementGroup.ordinal() (max 1003 for the
	// 4th group). Individual isotope ordinals (0..32) sit far below. Passing dataValues = MAX_META+1
	// makes the inherited ItemReactorMulti.getMaxDamage (dataValues-1) report the full 0..MAX_META
	// range, so the vanilla get/setDamage clamp preserves the value instead of pinning it to 0 — the
	// exact fix that made the magnet charge survive. The item stays stackable with no durability bar
	// (isDamageableItem() checks the absent MAX_DAMAGE component, not this method).
	private static final int MAX_META = 1000 + ElementGroup.values().length;

	public ItemNuclearWaste(Properties properties) {
		super(properties, MAX_META + 1);
	}

	@Override
	public int getEntityLifespan(ItemStack itemStack, Level level) {
		return Integer.MAX_VALUE;
	}

	@Override
	public boolean hasCustomEntity(ItemStack stack) {
		return true;
	}

	@Override
	public Entity createEntity(Level level, Entity location, ItemStack stack) {
		EntityNuclearWaste ei = new EntityNuclearWaste(level, location.getX(), location.getY(), location.getZ(), stack);
		ei.setDeltaMovement(location.getDeltaMovement());
		ei.setPickUpDelay(10);
		return ei;
	}

	// Radiation while carried (legacy onUpdate). applyEffects self-checks creative/immunity/shielding,
	// so it is safe to run for any living holder; the achievement stays gated on a non-creative player.
	@Override
	public void inventoryTick(ItemStack is, ServerLevel world, Entity e, EquipmentSlot slot) {
		if (e instanceof LivingEntity le) {
			if (RadiationEffects.instance.applyEffects(le, RadiationIntensity.HIGHLEVEL)) {
				if (le instanceof Player ep && !ep.isCreative())
					ReactorAchievements.HOLDWASTE.triggerAchievement(ep);
			}
		}
	}

	@Override
	public void appendHoverText(ItemStack is, Item.TooltipContext ctx, TooltipDisplay display, Consumer<Component> li, TooltipFlag flag) {
		int dmg = is.getDamageValue();
		if (dmg >= 1000) {
			ElementGroup g = ElementGroup.values()[dmg-1000];
			li.accept(Component.literal("Mixed Waste: "+g.displayName));
		}
		else {
			Isotopes atom = Isotopes.getIsotope(dmg);
			li.accept(Component.literal(atom.getDisplayName()));
			li.accept(Component.literal("Half Life: "+atom.getHalfLifeAsDisplay()));
		}
	}

}
