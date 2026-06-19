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

public abstract class ItemReactorTool extends ReactorItemBase {

	public ItemReactorTool(int tex) {
		super(tex);

		this.setMaxStackSize(1);
		this.setMaxDamage(0);
		canRepair = false;
	}

}
