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

import reika.dragonapi.instantiable.data.maps.TileEntityCache;
import reika.reactorcraft.tileentities.htgr.TileEntityPebbleBed;

public class PebbleBedArrangement {

	private final TileEntityCache<TileEntityPebbleBed> positions = new TileEntityCache();

	public PebbleBedArrangement(TileEntityPebbleBed te) {
		positions.put(te);
	}

	/** The one being fed in is the one being 'eaten' */
	public void merge(PebbleBedArrangement pba) {
		if (pba == this)
			return;
		if (pba == null || pba.positions.isEmpty())
			return;
		for (TileEntityPebbleBed te : pba.positions.values()) {
			positions.put(te);
			te.setReactorObject(this);
		}
		pba.clear();
	}

	public void clear() {
		positions.clear();
	}

	public int getSize() {
		return positions.size();
	}

	public void add(TileEntityPebbleBed te) {
		positions.put(te);
	}

	public void remove(TileEntityPebbleBed te) {
		positions.remove(te);
	}

	@Override
	public String toString() {
		return "#"+this.hashCode()+" > "+this.getSize()+": "+ positions.keySet();
	}

}
