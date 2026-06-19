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

import net.minecraft.world.level.Level;

public interface FusionReactorToroidPart {

	public FusionReactorToroidPart getNextPart(Level world, int x, int y, int z);

}
