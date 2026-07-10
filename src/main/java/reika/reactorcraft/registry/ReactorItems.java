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

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.Equippable;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.base.ItemReactorMulti;
import reika.reactorcraft.items.ItemCanister;
import reika.reactorcraft.items.ItemGeigerCounter;
import reika.reactorcraft.items.ItemHeavyBucket;
import reika.reactorcraft.items.ItemIronFinder;
import reika.reactorcraft.items.ItemRadiationCleaner;
import reika.reactorcraft.items.ItemRadiationGoggles;
import reika.reactorcraft.items.ItemReactorBook;
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

    /** Single-stack tool/utility items (was setMaxStackSize(1) upstream). */
    private static Item.Properties toolProperties() {
        return itemProperties().stacksTo(1);
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

    // --- Fuel rods (burnup carried in ItemStack.getDamageValue(); one damage per burnup step) ---
    // Registry id "fuel" (matches the 1.7.10 "item.fuel"); the machine block keeps "fuel_rod". Uranium
    // and plutonium fuel are distinct items upstream (ItemReactorFuel vs ItemPlutonium), so they must
    // register separately — a shared item collapses ReactorFuel.getFrom()'s per-item lookup.
    public static final DeferredItem<ItemReactorFuel> FUEL_ROD = reg("fuel", () -> new ItemReactorFuel(itemProperties(), 100));
    public static final DeferredItem<ItemReactorFuel> PLUTONIUM_ROD = reg("plutonium", () -> new ItemReactorFuel(itemProperties(), 100));
    public static final DeferredItem<ItemReactorFuel> FUEL_PELLET = reg("fuel_pellet", () -> new ItemReactorFuel(itemProperties(), 25));
    public static final DeferredItem<ItemReactorFuel> BREEDER_FUEL = reg("breeder_fuel", () -> new ItemReactorFuel(itemProperties(), 20));
    public static final DeferredItem<Item> DEPLETED_FUEL = reg("depleted_fuel", () -> new Item(itemProperties()));
    public static final DeferredItem<Item> DEPLETED_PELLET = reg("depleted_pellet", () -> new Item(itemProperties()));

    // --- Fluid canister (empty; fluid contents carried by CANISTER_FLUID data component) ---
    public static final DeferredItem<ItemCanister> CANISTER = reg("canister", () -> new ItemCanister(toolProperties(), 16));

    // --- Heavy-water bucket (empties to a vanilla bucket) ---
    public static final DeferredItem<ItemHeavyBucket> HEAVY_BUCKET = reg("heavy_water_bucket", () -> new ItemHeavyBucket(toolProperties().craftRemainder(Items.BUCKET)));

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
    public static final DeferredItem<ItemReactorBook> REACTOR_BOOK = reg("reactor_book", () -> new ItemReactorBook(toolProperties()));
    public static final DeferredItem<Item> MAGNET_ITEM = reg("magnet", () -> new ItemReactorMulti(itemProperties(), 8));
    public static final DeferredItem<ItemRadiationGoggles> GOGGLES_ITEM = reg("radiation_goggles",
            () -> new ItemRadiationGoggles(toolProperties().component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD).build())));

    // Hazmat suit: the radiation-shielding armour (a full worn set makes RadiationEffects.hasHazmatSuit
    // true, so the wearer survives reactor radiation). Registered as equippable radiation-fabric gear;
    // protection is the radiation immunity, not damage reduction.
    public static final DeferredItem<Item> HAZMAT_HELMET = reg("hazmat_helmet",
            () -> new Item(toolProperties().component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD).build())));
    public static final DeferredItem<Item> HAZMAT_CHESTPLATE = reg("hazmat_chestplate",
            () -> new Item(toolProperties().component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.CHEST).build())));
    public static final DeferredItem<Item> HAZMAT_LEGGINGS = reg("hazmat_leggings",
            () -> new Item(toolProperties().component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.LEGS).build())));
    public static final DeferredItem<Item> HAZMAT_BOOTS = reg("hazmat_boots",
            () -> new Item(toolProperties().component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.FEET).build())));

    // Coil-charged radiation tools (RotaryCraft ChargeableTool: charge is stored as the damage value,
    // exactly as upstream -- max charge 32 kJ, so durability 32000; the "damage bar" is the charge gauge).
    public static final DeferredItem<ItemGeigerCounter> GEIGER_COUNTER = reg("geiger_counter",
            () -> new ItemGeigerCounter(toolProperties().durability(32000)));
    public static final DeferredItem<ItemRadiationCleaner> RADIATION_CLEANER = reg("radiation_cleaner",
            () -> new ItemRadiationCleaner(toolProperties().durability(32000)));

    // Magnetic Ore Finder: held-item HUD marking nearby magnetic ores (IronFinderOverlay).
    public static final DeferredItem<ItemIronFinder> IRON_FINDER = reg("iron_finder",
            () -> new ItemIronFinder(toolProperties()));

    public static final ItemRef FUEL = ref(FUEL_ROD, 100);
    public static final ItemRef PLUTONIUM = ref(PLUTONIUM_ROD, 100);
    public static final ItemRef DEPLETED = ref(DEPLETED_FUEL);
    public static final ItemRef BREEDERFUEL = ref(BREEDER_FUEL, 20);
    public static final ItemRef PELLET = ref(FUEL_PELLET, 25);
    public static final ItemRef OLDPELLET = ref(DEPLETED_PELLET);
    public static final ItemRef CANISTER_REF = ref(CANISTER, 16);
    public static final ItemRef FLUORITE_REF = ref(FLUORITE_GEMS.get(FluoriteTypes.WHITE), FluoriteTypes.colorList.length);
    public static final ItemRef RAW = ref(FUEL_DUST, 10);
    public static final ItemRef WASTE = ref(WASTE_ITEM);
    public static final ItemRef BOOK = ref(REACTOR_BOOK);
    public static final ItemRef GOGGLES = ref(GOGGLES_ITEM);
    public static final ItemRef MAGNET = ref(MAGNET_ITEM, 8);
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
