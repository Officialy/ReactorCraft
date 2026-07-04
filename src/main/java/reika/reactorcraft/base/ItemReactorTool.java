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

/** Base for single-stack ReactorCraft tool/utility items (stack size / equippable / remainder are set
 *  on the item {@code Properties} at registration; see {@code ReactorItems.toolProperties}). */
public abstract class ItemReactorTool extends ReactorItemBase {

	public ItemReactorTool(Properties properties) {
		super(properties);
	}

}
