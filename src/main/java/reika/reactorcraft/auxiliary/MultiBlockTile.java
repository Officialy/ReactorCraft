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

import reika.dragonapi.interfaces.blockentity.BreakAction;

public interface MultiBlockTile extends BreakAction {

	public boolean hasMultiBlock();

	public void setHasMultiBlock(boolean has);

}
