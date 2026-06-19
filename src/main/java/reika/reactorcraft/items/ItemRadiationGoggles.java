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

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

import reika.reactorcraft.base.ItemReactorTool;

public class ItemRadiationGoggles extends ItemReactorTool {

	public ItemRadiationGoggles(int tex) {
		super(tex);
	}

	@Override
	public boolean isValidArmor(ItemStack stack, int type, Entity e) {
		return type == 0;
	}

}
