/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.items;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import reika.reactorcraft.api.MagneticOreOverride;
import reika.reactorcraft.base.ReactorItemBase;
import reika.reactorcraft.registry.ReactorBlocks;

/**
 * Magnetic Ore Finder: while held, magnetically-susceptible ores near the player show on the HUD
 * (see {@code IronFinderOverlay}). 26.2 port notes: the legacy ore set was vanilla iron/redstone
 * plus a dozen mod ores via ModOreList (nickel, cobalt, magnetite, ...) -- of those only
 * ReactorCraft's own magnetite exists here, so the set is the iron/redstone ore TAGS (which also
 * cover deepslate variants) + magnetite + any {@link MagneticOreOverride} block. The ChromatiCraft
 * aura-pouch interop is gated out (mod not ported).
 */
public class ItemIronFinder extends ReactorItemBase {

	// CHROMA-PORT: aura pouch "works in pouch" effect + tickAuraPouch entity-data timestamp.

	/** Legacy scan cache: at most one volume scan per player per 500 ms. */
	private static final Map<UUID, OreCollection> cache = new HashMap<>();

	public ItemIronFinder(Properties properties) {
		super(properties);
	}

	private static boolean isMagneticOre(Level world, BlockPos pos, BlockState state, Player ep) {
		if (state.getBlock() instanceof MagneticOreOverride m)
			return m.showOnHUD(world, pos, ep);
		return state.is(BlockTags.IRON_ORES)
				|| state.is(Blocks.REDSTONE_ORE) || state.is(Blocks.DEEPSLATE_REDSTONE_ORE)
				|| state.is(ReactorBlocks.MAGNETITE_ORE.get());
	}

	public static Set<BlockPos> getOreNearby(Player ep, int range) {
		OreCollection c = cache.get(ep.getUUID());
		if (c == null || System.currentTimeMillis() - c.time >= 500) {
			c = new OreCollection(findOreNearby(ep, range));
			cache.put(ep.getUUID(), c);
		}
		return Collections.unmodifiableSet(c.locations);
	}

	private static HashSet<BlockPos> findOreNearby(Player ep, int range) {
		HashSet<BlockPos> m = new HashSet<>();
		Level world = ep.level();
		BlockPos eye = BlockPos.containing(ep.getX(), ep.getY() + ep.getEyeHeight(), ep.getZ());
		for (BlockPos p : BlockPos.betweenClosed(eye.offset(-range, -range, -range), eye.offset(range, range, range))) {
			BlockState state = world.getBlockState(p);
			if (isMagneticOre(world, p, state, ep))
				m.add(p.immutable());
		}
		return m;
	}

	private record OreCollection(long time, HashSet<BlockPos> locations) {
		private OreCollection(HashSet<BlockPos> locations) {
			this(System.currentTimeMillis(), locations);
		}
	}

}
