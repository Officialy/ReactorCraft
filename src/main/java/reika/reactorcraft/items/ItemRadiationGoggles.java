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

/** Head-slot radiation goggles. The 1.7.10 {@code isValidArmor(stack,0,e)} (helmet slot) is now the
 *  {@code EQUIPPABLE} data component (HEAD) applied to the Properties at registration. */
public class ItemRadiationGoggles extends ItemReactorTool {

	public ItemRadiationGoggles(Properties properties) {
		super(properties);
	}

}
