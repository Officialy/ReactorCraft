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

import java.util.EnumMap;
import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.base.ItemReactorMulti;
import reika.reactorcraft.items.ItemCanister;
import reika.reactorcraft.items.ItemRadiationGoggles;
import reika.reactorcraft.items.ItemReactorFuel;

/**
 * 26.2 item registry for the ore→fuel slice, replacing the 1.7.10 metadata-variant
 * {@code ReactorItems} enum (one registered {@link Item} + {@code getStackOfMetadata}). Per the
 * PORTING.md item-variant decision and the plan's data-component refactor, each former metadata
 * variant is now either its own {@link DeferredItem} (ore products, fluorite colours, crafting
 * parts) or a single item carrying the variant in a {@link ReactorDataComponents} component
 * (fuel-rod burnup, canister fluid, waste isotope).
 */
public final class ReactorItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ReactorCraft.MODID);

    private static final ThreadLocal<ResourceKey<Item>> CURRENT_ITEM_KEY = new ThreadLocal<>();

    public static Item.Properties itemProperties() {
        Item.Properties p = new Item.Properties();
        ResourceKey<Item> k = CURRENT_ITEM_KEY.get();
        if (k != null) p.setId(k);
        return p;
    }

    private static <I extends Item> DeferredItem<I> reg(String name, Supplier<I> factory) {
        return ITEMS.register(name, rl -> {
            CURRENT_ITEM_KEY.set(ResourceKey.create(Registries.ITEM, rl));
            try {
                return factory.get();
            } finally {
                CURRENT_ITEM_KEY.remove();
            }
        });
    }

    // --- Ore products (smelted from the ore blocks) ---
    public static final DeferredItem<Item> URANIUM_INGOT = reg("uranium_ingot", () -> new Item(itemProperties()));
    public static final DeferredItem<Item> CADMIUM_INGOT = reg("cadmium_ingot", () -> new Item(itemProperties()));
    public static final DeferredItem<Item> INDIUM_INGOT = reg("indium_ingot", () -> new Item(itemProperties()));
    public static final DeferredItem<Item> SILVER_INGOT = reg("silver_ingot", () -> new Item(itemProperties()));
    public static final DeferredItem<Item> AMMONIUM_DUST = reg("ammonium_dust", () -> new Item(itemProperties()));
    public static final DeferredItem<Item> THORIUM_DUST = reg("thorium_dust", () -> new Item(itemProperties()));
    public static final DeferredItem<Item> CALCITE = reg("calcite", () -> new Item(itemProperties()));
    public static final DeferredItem<Item> LODESTONE = reg("lodestone", () -> new Item(itemProperties()));

    // --- Secondary materials (raw-dust line) ---
    public static final DeferredItem<Item> LIME = reg("lime", () -> new Item(itemProperties()));
    public static final DeferredItem<Item> EMERALD_DUST = reg("emerald_dust", () -> new Item(itemProperties()));
    public static final DeferredItem<Item> FUEL_DUST = reg("fuel_dust", () -> new Item(itemProperties()));
    public static final DeferredItem<Item> DEPLETED_DUST = reg("depleted_dust", () -> new Item(itemProperties()));
    public static final DeferredItem<Item> WASTE_DUST = reg("waste_dust", () -> new Item(itemProperties()));

    // --- Fuel rods (burnup carried by the FUEL_BURNUP data component) ---
    public static final DeferredItem<ItemReactorFuel> FUEL_ROD = reg("fuel_rod", () -> new ItemReactorFuel(itemProperties(), 1));
    public static final DeferredItem<ItemReactorFuel> FUEL_PELLET = reg("fuel_pellet", () -> new ItemReactorFuel(itemProperties(), 1));
    public static final DeferredItem<ItemReactorFuel> BREEDER_FUEL = reg("breeder_fuel", () -> new ItemReactorFuel(itemProperties(), 1));
    public static final DeferredItem<Item> DEPLETED_FUEL = reg("depleted_fuel", () -> new Item(itemProperties()));
    public static final DeferredItem<Item> DEPLETED_PELLET = reg("depleted_pellet", () -> new Item(itemProperties()));

    // --- Fluid canister (empty; fluid contents carried by CANISTER_FLUID data component) ---
    public static final DeferredItem<ItemCanister> CANISTER = reg("canister", () -> new ItemCanister(itemProperties(), 1));

    // --- Crafting components (CraftingItems enum, one item per former metadata) ---
    public static final EnumMap<CraftingItems, DeferredItem<Item>> CRAFTING = new EnumMap<>(CraftingItems.class);
    static {
        for (CraftingItems c : CraftingItems.partList) {
            CRAFTING.put(c, reg(c.registryName(), () -> new Item(itemProperties())));
        }
    }

    // --- Fluorite gems (one per colour) ---
    public static final EnumMap<FluoriteTypes, DeferredItem<Item>> FLUORITE_GEMS = new EnumMap<>(FluoriteTypes.class);
    static {
        for (FluoriteTypes f : FluoriteTypes.colorList) {
            FLUORITE_GEMS.put(f, reg(f.getGemItemName(), () -> new Item(itemProperties())));
        }
    }

    public static Item crafting(CraftingItems c) {
        return CRAFTING.get(c).get();
    }

    public static Item fluorite(FluoriteTypes f) {
        return FLUORITE_GEMS.get(f).get();
    }

    // --- TE-cluster items + compatibility refs (mirrors old enum API) ---
    public static final DeferredItem<Item> WASTE_ITEM = reg("waste", () -> new Item(itemProperties()));
    public static final DeferredItem<Item> REACTOR_BOOK = reg("reactor_book", () -> new Item(itemProperties()));
    public static final DeferredItem<Item> MAGNET_ITEM = reg("magnet", () -> new ItemReactorMulti(itemProperties(), 4));
    public static final DeferredItem<ItemRadiationGoggles> GOGGLES_ITEM = reg("radiation_goggles", () -> new ItemRadiationGoggles(itemProperties()));

    public static final ItemRef FUEL = ref(FUEL_ROD, 16);
    public static final ItemRef PLUTONIUM = ref(FUEL_ROD, 8);
    public static final ItemRef DEPLETED = ref(DEPLETED_FUEL);
    public static final ItemRef BREEDERFUEL = ref(BREEDER_FUEL, 8);
    public static final ItemRef PELLET = ref(FUEL_PELLET, 8);
    public static final ItemRef OLDPELLET = ref(DEPLETED_PELLET);
    public static final ItemRef CANISTER_REF = ref(CANISTER, 16);
    public static final ItemRef FLUORITE_REF = ref(FLUORITE_GEMS.get(FluoriteTypes.WHITE), FluoriteTypes.colorList.length);
    public static final ItemRef RAW = ref(FUEL_DUST, 10);
    public static final ItemRef WASTE = ref(WASTE_ITEM);
    public static final ItemRef BOOK = ref(REACTOR_BOOK);
    public static final ItemRef GOGGLES = ref(GOGGLES_ITEM);
    public static final ItemRef MAGNET = ref(MAGNET_ITEM, 4);
    public static final ItemRef FLUORITE = FLUORITE_REF;

    private static ItemRef ref(DeferredItem<? extends Item> item) {
        return new ItemRef(item, 1);
    }

    private static ItemRef ref(DeferredItem<? extends Item> item, int variants) {
        return new ItemRef(item, variants);
    }
    public static final class ItemRef {
        private final DeferredItem<? extends Item> item;
        private final int variants;

        ItemRef(DeferredItem<? extends Item> item, int variants) {
            this.item = item;
            this.variants = variants;
        }

        public Item getItemInstance() {
            return item.get();
        }

        public ItemStack getStackOf() {
            return new ItemStack(getItemInstance());
        }

        public ItemStack getStackOfMetadata(int meta) {
            ItemStack s = getStackOf();
            if (meta != 0)
                s.setDamageValue(meta);
            return s;
        }

        public int getNumberMetadatas() {
            return variants;
        }

        public boolean matchWith(ItemStack stack) {
            return stack != null && !stack.isEmpty() && stack.getItem() == getItemInstance();
        }
    }

    private ReactorItems() {}
}
