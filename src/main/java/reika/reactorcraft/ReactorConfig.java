/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft;

import java.util.ArrayList;
import java.util.HashSet;

import reika.dragonapi.base.DragonAPIMod;
import reika.dragonapi.instantiable.io.ControlledConfig;
import reika.dragonapi.interfaces.configuration.ConfigList;
import reika.dragonapi.interfaces.registry.IDRegistry;
import reika.dragonapi.libraries.java.ReikaJavaLibrary;
import reika.reactorcraft.registry.ReactorAchievements;

public class ReactorConfig extends ControlledConfig {

	private DataElement<Integer> potionID;

	private static final ArrayList<String> entries = ReikaJavaLibrary.getEnumEntriesWithoutInitializing(ReactorAchievements.class);

	public DataElement<Integer>[] achievementIDs = new DataElement[entries.size()];
	private DataElement<int[]> heavyWaterDimensions;

	private HashSet<Integer> heavyWaterDimensionSet;

	public ReactorConfig(DragonAPIMod mod, ConfigList[] option, IDRegistry[] id) {
		super(mod, option, id);

		potionID = this.registerAdditionalOption("Other", "Radiation Effect ID", 140);

		for (int i = 0; i < entries.size(); i++) {
			String name = entries.get(i);
			achievementIDs[i] = this.registerAdditionalOption("Achievement IDs", name, 72000+i);
		}

		heavyWaterDimensions = this.registerAdditionalOption("Other Options", "Heavy Water Dimensions (Empty for All)", new int[0]);
	}

	public boolean isDimensionValidForHeavyWater(int dim) {
		if (heavyWaterDimensionSet == null) {
			heavyWaterDimensionSet = new HashSet();
			for (int val : heavyWaterDimensions.getData()) {
				heavyWaterDimensionSet.add(val);
			}
		}
		return heavyWaterDimensionSet.isEmpty() || heavyWaterDimensionSet.contains(dim);
	}

	public int getRadiationPotionID() {
		return potionID.getData();
	}
}
