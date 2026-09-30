package reika.reactorcraft.modinterface;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.parts.IPartHost;
import appeng.parts.AEBasePart;
import reika.dragonapi.instantiable.data.immutable.WorldLocation;
import reika.dragonapi.libraries.java.ReikaJavaLibrary;
import reika.dragonapi.modinteract.deepinteract.MESystemReader;
import reika.reactorcraft.entities.EntityNeutron;
import reika.reactorcraft.entities.EntityNeutron.NeutronType;
import reika.reactorcraft.registry.ReactorItems;

/**
 * 1.7.10 {@code RadiationEffects.createMESystemEffect}: nuclear waste stored anywhere in an ME network leaks waste
 * neutrons out of a random block of that network every two minutes. Only loaded with Applied Energistics 2.
 */
public class WasteMESystemEffect extends MESystemReader.ItemInSystemEffect {

	private static final Random rand = new Random();

	public static void register() {
		MESystemReader.registerMESystemEffect(new WasteMESystemEffect());
	}

	public WasteMESystemEffect() {
		super(() -> ReactorItems.WASTE_ITEM.get().getDefaultInstance(), true); //1.7.10 WILDCARD_VALUE: waste of any isotope
	}

	@Override
	public int getTickFrequency() {
		return 2400;
	}

	@Override
	protected void doEffect(IGrid grid, long amt) {
		HashSet<WorldLocation> locations = new HashSet<>();
		for (IGridNode ign : grid.getNodes()) {
			BlockEntity be = getBlockEntity(ign);
			if (be != null && be.getLevel() != null) {
				locations.add(new WorldLocation(be));
			}
		}
		if (locations.isEmpty())
			return;
		List<WorldLocation> li = new ArrayList<>(locations);
		WorldLocation loc = ReikaJavaLibrary.getRandomCollectionEntry(rand, li);
		this.leakRadiation(loc);
	}

	/** 1.7.10 {@code IGridBlock.isWorldAccessible()+getLocation()}: the in-world block a node belongs to. */
	private static BlockEntity getBlockEntity(IGridNode ign) {
		Object owner = ign.getOwner();
		if (owner instanceof BlockEntity be)
			return be;
		if (owner instanceof AEBasePart part) {
			IPartHost host = part.getHost();
			return host != null ? host.getBlockEntity() : null;
		}
		return null;
	}

	protected void leakRadiation(WorldLocation loc) {
		Direction dir = Direction.values()[rand.nextInt(6)];
		var world = loc.getWorld();
		if (world != null && !world.isClientSide())
			world.addFreshEntity(new EntityNeutron(world, loc.pos, dir, NeutronType.WASTE));
	}

}
