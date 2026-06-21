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
import java.util.function.Supplier;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * Worldgen + processing data for ReactorCraft ores, transcribed from the 1.7.10 {@code ReactorOres}
 * enum (min/max Y band, vein size, veins-per-chunk, target dimension, mining XP). The legacy enum
 * carried 1.7.10-only generation/oredict logic; here we keep only the data the 26.2 data-driven
 * pipeline needs — the configured/placed ore features (datagen {@code RegistrySetBuilder}), the
 * smelting recipes, and the block loot tables.
 * <p>
 * Y values are kept at their original absolute heights (still valid bands in the taller 26.2 world).
 */
public enum ReactorOreType {

    PITCHBLENDE(ReactorBlocks.PITCHBLENDE_ORE, ReactorItems.URANIUM_INGOT, 8, 24, 16, 3, Dimension.OVERWORLD, 1.0F, false),
    CADMIUM(ReactorBlocks.CADMIUM_ORE, ReactorItems.CADMIUM_INGOT, 12, 32, 9, 3, Dimension.OVERWORLD, 0.7F, false),
    INDIUM(ReactorBlocks.INDIUM_ORE, ReactorItems.INDIUM_INGOT, 0, 16, 7, 2, Dimension.OVERWORLD, 1.0F, false),
    SILVER(ReactorBlocks.SILVER_ORE, ReactorItems.SILVER_INGOT, 16, 40, 9, 2, Dimension.OVERWORLD, 0.5F, false),
    CALCITE(ReactorBlocks.CALCITE_ORE, ReactorItems.CALCITE, 32, 60, 4, 12, Dimension.OVERWORLD, 0.4F, true),
    MAGNETITE(ReactorBlocks.MAGNETITE_ORE, ReactorItems.LODESTONE, 60, 128, 16, 7, Dimension.OVERWORLD, 0.8F, true),
    THORIUM(ReactorBlocks.THORIUM_ORE, ReactorItems.THORIUM_DUST, 0, 32, 24, 1, Dimension.OVERWORLD, 0.8F, false),
    FLUORITE(null, null, 32, 60, 8, 12, Dimension.OVERWORLD, 0.4F, true),
    AMMONIUM(ReactorBlocks.AMMONIUM_ORE, ReactorItems.AMMONIUM_DUST, 32, 32, 8, 6, Dimension.NETHER, 0.8F, true),
    ENDBLENDE(ReactorBlocks.END_PITCHBLENDE_ORE, ReactorItems.URANIUM_INGOT, 0, 64, 16, 6, Dimension.END, 1.0F, false);

    public enum Dimension { OVERWORLD, NETHER, END }

    private final Supplier<? extends Block> block;
    private final Supplier<? extends Item> product;
    public final int minY;
    public final int maxY;
    public final int veinSize;
    public final int perChunk;
    public final Dimension dimension;
    public final float xp;
    /** True if mining the block drops its material directly; false if it drops the block (smelt to refine). */
    public final boolean dropsMaterial;

    public static final ReactorOreType[] list = values();

    ReactorOreType(Supplier<? extends Block> block, Supplier<? extends Item> product, int minY, int maxY,
                   int veinSize, int perChunk, Dimension dimension, float xp, boolean dropsMaterial) {
        this.block = block;
        this.product = product;
        this.minY = minY;
        this.maxY = maxY;
        this.veinSize = veinSize;
        this.perChunk = perChunk;
        this.dimension = dimension;
        this.xp = xp;
        this.dropsMaterial = dropsMaterial;
    }

    public String featureName() {
        return this.name().toLowerCase(Locale.ENGLISH) + "_ore";
    }

    public Block getBlock() {
        return this == FLUORITE ? ReactorBlocks.fluoriteOre(FluoriteTypes.WHITE) : block.get();
    }

    public Item getProduct() {
        return this == FLUORITE ? ReactorItems.fluorite(FluoriteTypes.WHITE) : product.get();
    }
}
