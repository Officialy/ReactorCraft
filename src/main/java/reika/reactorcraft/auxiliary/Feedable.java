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

public interface Feedable {

	public boolean feed();

	public boolean feedIn(ItemStack is);

	public ItemStack feedOut();

}
