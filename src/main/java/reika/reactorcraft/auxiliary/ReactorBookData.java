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

import reika.dragonapi.instantiable.data.maps.ArrayMap;
import reika.reactorcraft.registry.ReactorBook;

public class ReactorBookData {

	private static final ArrayMap<ReactorBook> tabMappings = new ArrayMap(2);

	private static void mapHandbook() {
		for (int i = 0; i < ReactorBook.tabList.length; i++) {
			ReactorBook h = ReactorBook.tabList[i];
			tabMappings.putV(h, h.getScreen(), h.getPage());
		}
	}

	public static ReactorBook getMapping(int screen, int page) {
		return tabMappings.getV(screen, page);
	}

	static {
		mapHandbook();
	}
}
