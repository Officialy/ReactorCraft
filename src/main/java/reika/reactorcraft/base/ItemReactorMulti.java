/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.base;

import net.minecraft.world.item.ItemStack;

/** Reactor item state stored in typed components. Legacy damage hooks remain available to
 * reactor algorithms while concrete materials and colors use their registered item identities. */
public class ItemReactorMulti extends ReactorItemBase implements reika.dragonapi.interfaces.LegacyItemData {

	private final int dataValues;

	public ItemReactorMulti(Properties properties, int dataValues) {
		super(properties);
		this.dataValues = dataValues;
	}

	@Override
	public int getDataValues() {
		return dataValues;
	}

	protected net.minecraft.core.component.DataComponentType<Integer> variantComponent() {
        return reika.reactorcraft.registry.ReactorDataComponents.MAGNET_CHARGE.get();
    }

    @Override
    public int getDamage(ItemStack stack) {
        return Math.clamp(stack.getOrDefault(variantComponent(), stack.getOrDefault(net.minecraft.core.component.DataComponents.DAMAGE, 0)), 0, getMaxDamage(stack));
    }

    @Override
    public void setDamage(ItemStack stack, int value) {
        int bounded = Math.clamp(value, 0, getMaxDamage(stack));
        if (bounded == 0) stack.remove(variantComponent());
        else stack.set(variantComponent(), bounded);
        stack.remove(net.minecraft.core.component.DataComponents.DAMAGE);
    }

    @Override
    public void migrateLegacyComponents(ItemStack stack) {
        if (stack.has(net.minecraft.core.component.DataComponents.DAMAGE)) setDamage(stack, getDamage(stack));
    }

    /** Compatibility range for existing callers; storage uses the domain component, never durability. */
    @Override
    public int getMaxDamage(ItemStack stack) { return Math.max(0, dataValues - 1); }

}
