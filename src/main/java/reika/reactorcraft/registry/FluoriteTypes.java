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

/**
 * The eight fluorite colours. In 26.2 each colour is a separate ore block + gem item
 * (see {@code ReactorBlocks}/{@code ReactorItems}) rather than a block/item metadata variant,
 * so this enum now only carries the colour identity used for naming, models and tint.
 */
public enum FluoriteTypes {

    BLUE(0, 38, 255),
    PINK(255, 255, 236),
    ORANGE(255, 155, 0),
    MAGENTA(178, 0, 255),
    GREEN(0, 188, 18),
    RED(255, 50, 50),
    WHITE(255, 255, 255),
    YELLOW(255, 216, 0);

    public final int red;
    public final int green;
    public final int blue;

    public static final FluoriteTypes[] colorList = values();

    FluoriteTypes(int r, int g, int b) {
        red = r;
        green = g;
        blue = b;
    }

    public String getColorName() {
        return this.name().toLowerCase(Locale.ENGLISH);
    }

    /** Registry name of this colour's ore block, e.g. {@code white_fluorite_ore}. */
    public String getOreBlockName() {
        return this.getColorName() + "_fluorite_ore";
    }

    /** Registry name of this colour's gem item, e.g. {@code white_fluorite}. */
    public String getGemItemName() {
        return this.getColorName() + "_fluorite";
    }

    public int getColor() {
        return (red << 16) | (green << 8) | blue;
    }
}
