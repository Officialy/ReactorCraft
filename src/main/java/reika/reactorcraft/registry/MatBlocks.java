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

import java.util.Locale;

import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;

/**
 * The reactor "material" blocks (BlockReactorMat); each is its own registered block, keyed here.
 */
public enum MatBlocks implements StringRepresentable {

	CONCRETE("block.concrete", "concrete"),
	SLAG("block.slag", "slag"),
	CALCITE("block.calcite", "calcite_block"),
	SCRUBBER("block.scrubber", "scrubber"),
	LODESTONE("block.lodestone", "lodestone_block"),
	GRAPHITE("block.graphite", "graphite_block");

	private final String translationKey;
	private final String registryName;

	public static final MatBlocks[] matList = values();

	private MatBlocks(String n, String reg) {
		translationKey = n;
		registryName = reg;
	}

	/** Block registry id. Calcite/lodestone/graphite take a {@code _block} suffix to avoid colliding with the same-named items. */
	public String getRegistryName() {
		return registryName;
	}

	public String getName() {
		return Component.translatable(translationKey).getString();
	}

	public boolean isMultiSidedTexture() {
		return this == SCRUBBER;
	}

	public ItemStack getStackOf() {
		return this.getStackOf(1);
	}

	public ItemStack getStackOf(int size) {
		return new ItemStack(ReactorBlocks.matBlock(this), size);
	}

	@Override
	public String getSerializedName() {
		return this.name().toLowerCase(Locale.ROOT);
	}

}
