/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2017
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.world;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

import reika.dragonapi.interfaces.OreGenerator;
import reika.dragonapi.interfaces.RetroactiveGenerator;
import reika.reactorcraft.registry.ReactorOres;

public class ReactorOreGenerator implements RetroactiveGenerator {

	public static final ReactorOreGenerator instance = new ReactorOreGenerator();

	public final ArrayList<OreGenerator> generators = new ArrayList();

	private ReactorOreGenerator() {
		generators.add(new BasicReactorOreGenerator());
	}

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkgen, IChunkProvider provider) {
		for (int i = 0; i < ReactorOres.oreList.length; i++) {
			ReactorOres ore = ReactorOres.oreList[i];
			if (ore.canGenerateInChunk(world, chunkX, chunkZ)) {
				for (OreGenerator gen : generators) {
					gen.generateOre(ore, random, world, chunkX, chunkZ);
				}
			}
		}
	}

	@Override
	public String getIDString() {
		return "ReactorCraft Ores";
	}

	@Override
	public boolean canGenerateAt(World world, int chunkX, int chunkZ) {
		return true;
	}

}
