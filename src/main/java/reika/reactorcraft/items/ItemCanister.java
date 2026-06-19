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

/** Fluid canister; multiple {@code getDamageValue()} variants (one per fluid). Variant count is
 *  supplied at registration. The 1.7.10 self crafting-remainder (emptied canister stays) is a
 *  registration/recipe-level concern in 26.2 (USE_REMAINDER component / recipe design). */
public class ItemCanister extends ItemReactorTool {

	private final int dataValues;

	public ItemCanister(Properties properties, int dataValues) {
		super(properties);
		this.dataValues = dataValues;
	}

	@Override
	public int getDataValues() {
		return dataValues;
	}

}
