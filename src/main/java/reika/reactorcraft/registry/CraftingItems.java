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

/**
 * The ReactorCraft crafting components. In 1.7.10 these were metadata variants of a single
 * {@code ItemReactorMulti} with {@code GameRegistry} recipe helpers; in 26.2 each is its own
 * {@link net.minecraft.world.item.Item} (see {@link ReactorItems#CRAFTING}) and the recipes are
 * emitted by datagen, so this enum keeps only the component identity (registry id + gating flag).
 */
public enum CraftingItems {

    CANISTER("canister_part"),
    ROD("rod"),
    TANK("tank"),
    ALLOY("alloy"),
    BACKING("backing"),
    MAGNETIC("magnetic"),
    MAGNETCORE("magnet_core"),
    COOLANT("coolant"),
    WIRE("wire"),
    SHIELD("shield"),
    FERROINGOT("ferromagnetic_ingot"),
    HYSTERESIS("hysteresis_unit"),
    HYSTERESISRING("hysteresis_ring"),
    GRAPHITE("graphite"),
    UDUST("uranium_dust"),
    FABRIC("radiation_fabric"),
    CARBIDEFLAKES("carbide_flakes"),
    CARBIDE("carbide"),
    TURBCORE("turbine_core");

    private final String registryName;

    public static final CraftingItems[] partList = values();

    CraftingItems(String registryName) {
        this.registryName = registryName;
    }

    public String registryName() {
        return registryName;
    }

    public boolean isGating() {
        return switch (this) {
            case WIRE, COOLANT, UDUST, FABRIC, HYSTERESIS, HYSTERESISRING -> false;
            default -> true;
        };
    }
}
