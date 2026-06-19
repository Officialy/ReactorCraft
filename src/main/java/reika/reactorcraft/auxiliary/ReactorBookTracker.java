/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2017
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.auxiliary;

import net.minecraft.item.ItemStack;

import reika.reactorcraft.registry.ReactorItems;
import reika.rotarycraft.auxiliary.HandbookTracker;

public class ReactorBookTracker extends HandbookTracker {

	@Override
	public ItemStack getItem() {
		return ReactorItems.BOOK.getStackOf();
	}

	@Override
	public String getID() {
		return "ReactorCraft_Handbook";
	}

}
