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
import net.minecraft.core.BlockPos;

import net.minecraft.world.level.Level;

import reika.reactorcraft.entities.EntityNeutron;

public interface NeutronBlock {

	boolean onNeutron(EntityNeutron e, Level world, BlockPos pos);

}
