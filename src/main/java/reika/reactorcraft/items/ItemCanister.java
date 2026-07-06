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

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;

import reika.reactorcraft.base.ItemReactorTool;
import reika.reactorcraft.registry.ReactorDataComponents;

/** Fluid canister; the contents ride the CANISTER_FLUID component and name the item
 *  ("Sodium Canister" etc., like the legacy can.* names). The emptied-canister crafting
 *  remainder is handled at the recipe level. */
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

	@Override
	public Component getName(ItemStack is) {
		SimpleFluidContent c = is.get(ReactorDataComponents.CANISTER_FLUID.get());
		if (c != null && !c.isEmpty())
			return Component.translatable("item.reactorcraft.canister.filled", c.getFluid().getFluidType().getDescription());
		return super.getName(is);
	}

}
