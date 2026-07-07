package reika.reactorcraft.registry;

import java.util.ArrayList;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import reika.dragonapi.instantiable.data.immutable.BlockKey;
import reika.dragonapi.instantiable.data.maps.BlockMap;
import reika.rotarycraft.registry.RotaryBlocks;

public enum RadiationShield {

	STEEL("Steel", 90, 95, new BlockKey(RotaryBlocks.HSLA_STEEL_BLOCK.get())),
	WATER("Water", 30, 10, new BlockKey(Blocks.WATER)),
	BEDINGOT("Bedrock Ingot", 97.5, 100, new BlockKey(RotaryBlocks.BEDROCK.get())),
	OBSIDIAN("Obsidian", 50, 80, new BlockKey(Blocks.OBSIDIAN)),
	BLASTGLASS("Blast Glass", 80, 20, new BlockKey(RotaryBlocks.BLASTGLASS.get()));

	public final double neutronAbsorbChance;
	public final double radiationDeflectChance;
	public final String displayName;

	private final ArrayList<BlockKey> blocks = new ArrayList<>();

	private static final BlockMap<RadiationShield> blockMap = new BlockMap<>();

	public static final RadiationShield[] shieldList = values();

	public static String getDataAsString() {
		StringBuilder sb = new StringBuilder();
		for (RadiationShield type : shieldList) {
			sb.append(type.displayName + ": " + type.neutronAbsorbChance + "% neutron absorption, " + type.radiationDeflectChance + " radiation containment");
			sb.append("\n");
		}
		return sb.toString();
	}

	RadiationShield(String s, double n, double r, BlockKey... bks) {
		displayName = s;
		neutronAbsorbChance = n;
		radiationDeflectChance = r;
		for (BlockKey bk : bks)
			blocks.add(bk);
	}

	public static RadiationShield getFrom(Block b, int meta) {
		return blockMap.get(b);
	}

	public static RadiationShield getFrom(BlockKey bk) {
		return blockMap.get(bk);
	}

	static {
		for (RadiationShield rs : shieldList) {
			for (BlockKey bk : rs.blocks)
				blockMap.put(bk, rs);
		}
	}
}
