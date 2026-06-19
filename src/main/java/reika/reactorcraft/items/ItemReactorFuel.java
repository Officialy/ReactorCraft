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

import reika.reactorcraft.base.ItemReactorMulti;

/** Reactor fuel; the burnup stage is the {@code getDamageValue()} variant (durability + setNoRepair
 *  set on the Properties at registration). */
public class ItemReactorFuel extends ItemReactorMulti {

	public ItemReactorFuel(Properties properties, int dataValues) {
		super(properties, dataValues);
	}

}
