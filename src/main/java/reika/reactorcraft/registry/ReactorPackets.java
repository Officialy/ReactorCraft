/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.registry;

import reika.reactorcraft.ReactorCraft;

public enum ReactorPackets {

	CPUTOGGLE(),
	CPURAISE(),
	CPULOWER(),
	ORERADIATION(),
	;

	private final int numInts;

	private static final ReactorPackets[] list = values();

	ReactorPackets() {
		this(0);
	}

	ReactorPackets(int ints) {
		numInts = ints;
	}

	public boolean isLongPacket() {
		return false;
	}

	public int getNumberDataInts() {
		return numInts;
	}

	public static ReactorPackets getEnum(int index) {
		if (index >= 0 && index < list.length)
			return list[index];
		ReactorCraft.LOGGER.error("Index "+index+" does not correspond to an existing packet classification!");
		return null;
	}

}
