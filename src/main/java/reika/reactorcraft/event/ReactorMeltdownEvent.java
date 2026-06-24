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

import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;

public class ReactorMeltdownEvent extends Event {

	public final Level world;
	public final int centerX;
	public final int centerY;
	public final int centerZ;

	public ReactorMeltdownEvent(Level world, int x, int y, int z) {
		this.world = world;
		centerX = x;
		centerY = y;
		centerZ = z;
	}

}
