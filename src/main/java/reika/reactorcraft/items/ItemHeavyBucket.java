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

import reika.reactorcraft.base.ItemReactorTool;

/** Heavy-water/coolant bucket. The empty-bucket crafting remainder is set via
 *  {@code Properties.craftRemainder(Items.BUCKET)} at registration. */
public class ItemHeavyBucket extends ItemReactorTool {

	public ItemHeavyBucket(Properties properties) {
		super(properties);
	}

}
