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

/** Reactor fuel with typed burnup storage and legacy damage-hook compatibility. */
public class ItemReactorFuel extends ItemReactorMulti {

	public ItemReactorFuel(Properties properties, int dataValues) {
		super(properties, dataValues);
	}
    @Override
    protected net.minecraft.core.component.DataComponentType<Integer> variantComponent() {
        return reika.reactorcraft.registry.ReactorDataComponents.FUEL_BURNUP.get();
    }

}
