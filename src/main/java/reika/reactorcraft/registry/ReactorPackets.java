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


public enum ReactorPackets implements reika.dragonapi.interfaces.PacketId {

	CPUTOGGLE(0),
	CPURAISE(1),
	CPULOWER(2),
	ORERADIATION(3),
	;

	private final int wireId;
	private static final java.util.Map<Integer, ReactorPackets> BY_ID = reika.dragonapi.interfaces.PacketId.index(values());
	@Override public int wireId() { return wireId; }

	private final int numInts;



	ReactorPackets(int id) { this(id, 0); }

	ReactorPackets(int id, int ints) {
		wireId = id;
		numInts = ints;
	}

	public boolean isLongPacket() {
		return false;
	}

	public int getNumberDataInts() {
		return numInts;
	}

	public static ReactorPackets getEnum(int index) {
		return reika.dragonapi.interfaces.PacketId.lookup(BY_ID, index);
	}

}
