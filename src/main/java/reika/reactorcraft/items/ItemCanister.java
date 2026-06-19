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

import reika.reactorcraft.ReactorNames;
import reika.reactorcraft.base.ItemReactorTool;

public class ItemCanister extends ItemReactorTool {

	public ItemCanister(int tex) {
		super(tex);
		this.setContainerItem(this);
		hasSubtypes = true;
		this.setMaxDamage(0);
	}

	@Override
	public int getDataValues() {
		return ReactorNames.canNames.length;
	}

}
