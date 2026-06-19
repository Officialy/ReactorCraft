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

/** Base for single-stack ReactorCraft tool/utility items. The 1.7.10 setMaxStackSize(1)/
 *  setMaxDamage(0)/canRepair=false are now expressed on the item {@code Properties} at registration. */
public abstract class ItemReactorTool extends ReactorItemBase {

	public ItemReactorTool(Properties properties) {
		super(properties);
	}

}
