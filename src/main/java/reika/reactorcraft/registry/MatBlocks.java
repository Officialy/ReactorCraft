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
 * The variants of the reactor "material" block ({@code ReactorBlocks.MATS} / BlockReactorMat).
 * Modelled as an {@code EnumProperty<MatBlocks>} on the block; none of them carry a block entity.
 */
public enum MatBlocks implements StringRepresentable {

	CONCRETE("block.concrete"),
	SLAG("block.slag"),
	CALCITE("block.calcite"),
	SCRUBBER("block.scrubber"),
	LODESTONE("block.lodestone"),
	GRAPHITE("block.graphite");

	private final String translationKey;

	public static final MatBlocks[] matList = values();

	private MatBlocks(String n) {
		translationKey = n;
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
		// VARIANT-ITEM-PORT: the 6 mats currently share one block-item; the variant is set on the
		// placed blockstate. Split into per-variant items / data components when the item layer lands.
		return new ItemStack(ReactorBlocks.MATS.get(), size);
	}

	@Override
	public String getSerializedName() {
		return this.name().toLowerCase(Locale.ROOT);
	}

}
