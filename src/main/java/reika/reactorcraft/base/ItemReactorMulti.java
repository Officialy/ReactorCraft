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

/** A ReactorCraft item carrying several {@code getDamageValue()} variants (raw materials, fluorite
 *  colours, ingots, crafting items, fuel burnup, magnet charge, …). The 1.7.10 {@code hasSubtypes}/
 *  metadata scheme is gone; the variant count is now supplied at registration (see PORTING.md
 *  item-variant decision). */
public class ItemReactorMulti extends ReactorItemBase {

	private final int dataValues;

	public ItemReactorMulti(Properties properties, int dataValues) {
		super(properties);
		this.dataValues = dataValues;
	}

	@Override
	public int getDataValues() {
		return dataValues;
	}

	/**
	 * The variant is carried in the stack's {@code DAMAGE} component, but these items register with no
	 * durability, so the vanilla {@code getMaxDamage} (which reads the {@code MAX_DAMAGE} component)
	 * returns 0 — and both {@code Item.getDamage}/{@code setDamage} clamp to {@code [0, getMaxDamage]},
	 * which would pin every variant to 0. Reporting the variant range here lets the clamp store the
	 * real damage value (fuel burnup, magnet charge, waste isotope, …). Crucially this does NOT make
	 * the item damageable: {@code ItemStack.isDamageableItem()} checks the {@code MAX_DAMAGE}
	 * <em>component</em> (still absent), so the item stays stackable with no durability bar.
	 */
	@Override
	public int getMaxDamage(ItemStack stack) {
		return Math.max(0, dataValues - 1);
	}

}
