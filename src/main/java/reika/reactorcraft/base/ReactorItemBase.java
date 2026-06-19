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

import net.minecraft.world.item.Item;

/**
 * Thin modern base for ReactorCraft items. The 1.7.10 version drove the texture atlas via
 * {@code IndexedItemSprites} + sprite indices and built variants from item metadata; in 26.2 that
 * is all replaced by JSON item models + lang (datagen) and, where a single item carries several
 * variants (fuel burnup, waste isotopes, fluorite colours), the {@code getDamageValue()} carrier
 * documented in PORTING.md. Display names come from lang; crafting achievements are granted by the
 * data-driven advancements (triggerAchievement), not an onCreated hook.
 */
public abstract class ReactorItemBase extends Item {

	public ReactorItemBase(Properties properties) {
		super(properties);
	}

	/** Number of {@code getDamageValue()} variants this item carries (1 = single-variant). */
	public int getDataValues() {
		return 1;
	}
}
