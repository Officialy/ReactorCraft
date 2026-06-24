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

import net.minecraft.world.entity.player.Player;

/** Advancement triggers deferred — no-op stubs keep TE logic intact until advancement datagen lands. */
public enum ReactorAchievements {

	RECUSEBOOK, MINEURANIUM, MINECADMIUM, PEBBLE, UF6, DEPLETED, FISSION, PLUTONIUM, PUPOISON,
	HOLDWASTE, DECAY, WASTELEAK, AMMONIA, NH3EXPLODE, GIGATURBINE, HOTCORE, SCRAM, THORIUMDUMP,
	PEBBLEFAIL, PLASMA, PLASMADIE, FUSION, ESCAPE, MELTPIPE, HEAVYWATER, CANDU, MELTDOWN, FIFTYGW;

	public void triggerAchievement(Player player) {
		// Advancement datagen deferred.
	}

	public void triggerAchievement(String playerName) {
		// Advancement datagen deferred.
	}
}
