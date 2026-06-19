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

/** A ReactorCraft item carrying several {@code getDamageValue()} variants (raw materials, fluorite
 *  colours, ingots, crafting items, …). The 1.7.10 {@code hasSubtypes}/metadata scheme is gone; the
 *  variant count is now supplied at registration (see PORTING.md item-variant decision). */
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

}
