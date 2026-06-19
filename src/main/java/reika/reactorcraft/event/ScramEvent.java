/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2017
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.event;

import reika.dragonapi.instantiable.event.TileEntityEvent;
import reika.reactorcraft.tileentities.fission.TileEntityCPU;

public class ScramEvent extends TileEntityEvent {

	public final int triggerTemperature;
	public final int rodCount;

	public ScramEvent(TileEntityCPU te, int temp) {
		super(te);

		triggerTemperature = temp;
		rodCount = te.getLayout().getNumberRods();
	}

}
