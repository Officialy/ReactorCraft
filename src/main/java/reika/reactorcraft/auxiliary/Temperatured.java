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

import reika.dragonapi.interfaces.blockentity.ThermalTile;
import reika.reactorcraft.tileentities.fission.TileEntityWaterCell.LiquidStates;
import reika.rotarycraft.auxiliary.interfaces.HeatConduction;

/** Reactor core blocks only. */
public interface Temperatured extends ThermalTile, HeatConduction {

	public abstract int getTemperature();

	public void setTemperature(int T);

	public int getMaxTemperature();

	public boolean canDumpHeatInto(LiquidStates liq);

}
