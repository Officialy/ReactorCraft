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

import net.minecraft.init.Items;

import reika.reactorcraft.base.ItemReactorTool;

public class ItemHeavyBucket extends ItemReactorTool {

	public ItemHeavyBucket(int tex) {
		super(tex);
		this.setContainerItem(Items.bucket);
	}

}
