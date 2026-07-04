package reika.reactorcraft.data;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import net.minecraft.data.PackOutput;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.common.data.LanguageProvider;

import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.registry.ReactorBlocks;
import reika.reactorcraft.registry.ReactorItems;

/**
 * en_us language provider. Display names come from the original ReactorCraft en_US.lang, keyed here by
 * the modern registry id; ids whose prettified form already matches the original name fall through to
 * {@link #prettify}. Block-items share the {@code block.reactorcraft.*} key {@code addBlock} emits, so
 * they're filtered out of the item pass to avoid a duplicate-key crash.
 */
public class ReactorLang extends LanguageProvider {

    private static final Map<String, String> NAMES = new HashMap<>();

    private static void n(String id, String name) {
        NAMES.put(id, name);
    }

    static {
        // --- Machine blocks (original machine.* names) ---
        n("fuel_rod", "Fuel Core");
        n("reactor_cpu", "Central Control");
        n("turbine_core", "Turbine");
        n("steam_line", "Steam Line");
        n("heavy_pump", "Centrifugal Fluid Extractor");
        n("waste_container", "Spent Fuel Container");
        n("reactor_boiler", "Steam Boiler");
        n("reactor_pump", "Pressurizer");
        n("synthesizer", "Fluid Synthesizer");
        n("toroid_magnet", "Toroid Magnet");
        n("tritizer", "Neutron Irradiation Chamber");
        n("breeder_core", "Breeder Reactor Core");
        n("sodium_boiler", "Sodium Heater");
        n("waste_storage", "Nuclear Waste Disposal Drum");
        n("fusion_injector", "Fusion Plasma Injector");
        n("fusion_heater", "Hydrogen Preheater");
        n("magnetic_pipe", "Magnetic Containment Pipe");
        n("co2_heater", "Carbon Dioxide Heat Exchanger");
        n("pebble_bed", "Pebble Bed Reactor Core");
        n("reactor_generator", "Turbine Generator");
        n("fusion_marker", "Tokamak Blueprint Highlighter");
        n("turbine_meter", "Turbine Dynamometer");
        n("high_pressure_turbine", "High-Pressure Turbine");
        n("thorium_core", "Thorium Fuel Core");
        n("waste_pipe", "Nuclear Waste Duct");
        n("fuel_dump", "Fuel Dump Valve");
        n("solar_top", "Solar Tower Sodium Cycler");
        n("solar_exchanger", "Solar Tower Sodium Heat Exchanger");
        n("waste_decayer", "Forced Fission Chamber");
        n("mini_turbine", "Miniature Turbine");
        // Multiblock casings (single blocks in the port)
        n("generator_multi", "Generator Housing");
        n("flywheel_multi", "Turbine Flywheel Frame");
        n("turbine_multi", "Turbine Housing");

        // --- Ores (original ore.* names) ---
        n("pitchblende_ore", "Pitchblende");
        n("end_pitchblende_ore", "Pitchblende");
        n("ammonium_ore", "Ammonium Chloride");
        n("calcite_ore", "Calcite");
        n("thorium_ore", "Thorite");

        // --- Material / technical blocks (original block.* / fluid names) ---
        n("slag", "Corium");
        n("corium", "Corium");
        n("scrubber", "Steam Scrubber");
        n("thorium_fuel", "Molten Thorium Fuel");

        // --- Items (original item.* / raw.* / crafting.* names) ---
        n("uranium_ingot", "Raw Uranium Ingot");
        n("ammonium_dust", "Ammonium Chloride");
        n("thorium_dust", "Thorium Dust");
        n("calcite", "Calcite Crystal");
        n("lime", "Quicklime");
        n("fuel_dust", "Enriched Uranium Dust");
        n("depleted_dust", "Depleted Uranium Dust");
        n("waste_dust", "Unprocessed Nuclear Waste");
        n("fuel", "Uranium Fuel Pellet");
        n("plutonium", "Plutonium Fuel Pellet");
        n("fuel_pellet", "TRISO Fuel Pellet");
        n("breeder_fuel", "Breeder Reactor Fuel");
        n("depleted_fuel", "Depleted Uranium");
        n("depleted_pellet", "Depleted TRISO Fuel");
        n("waste", "Nuclear Waste");
        n("magnet", "Permanent Magnet");
        n("radiation_goggles", "Radiation Goggles");
        n("reactor_book", "ReactorCraft Handbook");
        n("heavy_water_bucket", "Heavy Water Bucket");
        n("canister", "Fluid Canister");
        // Crafting components (CraftingItems)
        n("canister_part", "Fuel Canister");
        n("rod", "Absorption Rod");
        n("tank", "Obsidian Tank");
        n("alloy", "Cd-In-Ag Alloy Ingot");
        n("backing", "Hardened Backing Panel");
        n("magnetic", "Ferromagnetic Plate");
        n("magnet_core", "Magnetic Core");
        n("coolant", "Coolant Pack");
        n("wire", "Gold Wiring");
        n("shield", "Neutron Shielding");
        n("ferromagnetic_ingot", "Ferromagnetic Ingot");
        n("hysteresis_unit", "Hysteresis Plate");
        n("hysteresis_ring", "Hysteresis Ring");
        n("uranium_dust", "Uranium Dust");
        n("radiation_fabric", "Radiation-Shielding Fabric");
        n("carbide_flakes", "Tungsten Carbide Flakes");
        n("carbide", "Tungsten Carbide Ingot");
        n("turbine_core_part", "Steam Turbine Core");
        n("lodestone", "Lodestone");
    }

    public ReactorLang(PackOutput output, String locale) {
        super(output, ReactorCraft.MODID, locale);
    }

    @Override
    protected void addTranslations() {
        add("tab.reactorcraft", "ReactorCraft");

        ReactorBlocks.BLOCKS.getEntries().forEach(holder ->
                addBlock(holder, nameOf(holder.getId().getPath())));

        ReactorBlocks.ITEMS.getEntries().forEach(holder -> {
            if (holder.get() instanceof BlockItem) return;
            addItem(holder, nameOf(holder.getId().getPath()));
        });

        ReactorItems.ITEMS.getEntries().forEach(holder ->
                addItem(holder, nameOf(holder.getId().getPath())));
    }

    private static String nameOf(String path) {
        String mapped = NAMES.get(path);
        if (mapped != null)
            return mapped;
        // Fluorite gems: "<colour>_fluorite" → "<Colour> Fluorite Crystal".
        if (path.endsWith("_fluorite"))
            return prettify(path) + " Crystal";
        return prettify(path);
    }

    private static String prettify(String path) {
        String[] parts = path.split("_");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append(' ');
            String p = parts[i];
            if (p.isEmpty()) continue;
            sb.append(Character.toUpperCase(p.charAt(0)));
            if (p.length() > 1) sb.append(p.substring(1).toLowerCase(Locale.ROOT));
        }
        return sb.toString();
    }
}
