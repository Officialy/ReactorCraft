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

import reika.dragonapi.interfaces.tileentity.BreakAction;
import reika.reactorcraft.tileentities.fission.TileEntityCPU;


public interface LinkableReactorCore extends TypedReactorCoreTE, BreakAction {

	public void link(TileEntityCPU te);

}
