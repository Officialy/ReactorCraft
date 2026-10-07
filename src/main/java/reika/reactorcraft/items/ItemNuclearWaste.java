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
 * Stackable nuclear waste with a persistent, named isotope or element-group component.
 * The legacy damage hooks expose isotope ordinals and 1000 + group ordinals to existing
 * reactor algorithms; writes store stable names instead of enum positions. Old DAMAGE
 * stacks migrate on load. All identities share the original waste sprite and display name.
 */
public class ItemNuclearWaste extends ItemReactorMulti {

    // Range of the compatibility numeric view, including mixed element groups.
	private static final int MAX_META = 1000 + ElementGroup.values().length;

	public ItemNuclearWaste(Properties properties) {
		super(properties, MAX_META + 1);
	}

    /** Stable identities preserve saves when isotope enum ordering changes in future versions. */
    public static String identity(int legacy) {
        if (legacy >= 1000 && legacy - 1000 < ElementGroup.values().length)
            return "group:" + ElementGroup.values()[legacy - 1000].name().toLowerCase(java.util.Locale.ROOT);
        if (legacy >= 0 && legacy < Isotopes.values().length)
            return "isotope:" + Isotopes.values()[legacy].name().toLowerCase(java.util.Locale.ROOT);
        throw new IllegalArgumentException("Invalid legacy waste identity: " + legacy);
    }

    @Override
    public int getDamage(ItemStack stack) {
        String identity = stack.get(reika.reactorcraft.registry.ReactorDataComponents.WASTE_IDENTITY.get());
        if (identity == null) return stack.getOrDefault(net.minecraft.core.component.DataComponents.DAMAGE, 0);
        for (Isotopes atom : Isotopes.values()) if (identity.equals(identity(atom.ordinal()))) return atom.ordinal();
        for (ElementGroup group : ElementGroup.values()) if (identity.equals(identity(1000 + group.ordinal()))) return 1000 + group.ordinal();
        throw new IllegalArgumentException("Unknown waste identity: " + identity);
    }

    @Override
    public void setDamage(ItemStack stack, int legacy) {
        stack.set(reika.reactorcraft.registry.ReactorDataComponents.WASTE_IDENTITY.get(), identity(legacy));
        stack.remove(net.minecraft.core.component.DataComponents.DAMAGE);
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
			li.accept(Component.translatable("tooltip.reactorcraft.mixed_waste", g.displayName));
		}
		else {
			Isotopes atom = Isotopes.getIsotope(dmg);
			li.accept(Component.literal(atom.getDisplayName()));
			li.accept(Component.translatable("tooltip.reactorcraft.half_life", atom.getHalfLifeAsDisplay()));
		}
	}

}
