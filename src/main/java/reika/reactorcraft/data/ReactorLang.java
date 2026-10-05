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
        n("preheater_housing_corner", "Preheater Unit Housing Corner");
        n("preheater_housing_edge", "Preheater Unit Housing Edge");
        n("preheater_housing_face", "Preheater Unit Housing Face");
        n("plasma_injector_column", "Plasma Injector Column Piece");
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
        n("geiger_counter", "Geiger Counter");
        n("radiation_cleaner", "Radiation Cleanup Tool");
        n("iron_finder", "Magnetic Ore Finder");
        n("reactor_book", "ReactorCraft Handbook");
        n("heavy_water_bucket", "Heavy Water Bucket");
        n("canister", "Empty Canister");
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

    /** Original fluid.* display names, keyed by the FluidType registry path. */
    private static final Map<String, String> FLUIDS = new HashMap<>();
    static {
        FLUIDS.put("heavy_water", "Heavy Water");
        FLUIDS.put("hydrofluoric_acid", "Hydrofluoric Acid");
        FLUIDS.put("uranium_hexafluoride", "Uranium Hexafluoride");
        FLUIDS.put("ammonia", "Ammonia");
        FLUIDS.put("sodium", "Molten Sodium");
        FLUIDS.put("chlorine", "Chlorine Gas");
        FLUIDS.put("oxygen", "Oxygen Gas");
        FLUIDS.put("liquid_oxygen", "Liquid Oxygen");
        FLUIDS.put("low_pressure_ammonia", "Low Pressure Ammonia");
        FLUIDS.put("low_pressure_water", "Low Pressure Water");
        FLUIDS.put("hot_sodium", "Superheated Molten Sodium");
        FLUIDS.put("warm_sodium", "Hot Molten Sodium");
        FLUIDS.put("deuterium", "Deuterium");
        FLUIDS.put("tritium", "Tritium");
        FLUIDS.put("carbon_dioxide", "Carbon Dioxide Gas");
        FLUIDS.put("hot_carbon_dioxide", "Hot Carbon Dioxide Gas");
        FLUIDS.put("fusion_plasma", "Fusion Plasma");
        FLUIDS.put("corium", "Corium");
        FLUIDS.put("nuclear_waste", "Nuclear Waste");
        FLUIDS.put("lithium", "Molten Lithium");
        FLUIDS.put("lifbe", "Lithium Beryllium Fluoride");
        FLUIDS.put("lifbe_fuel", "Molten Thorium Fuel");
        FLUIDS.put("lifbe_fuel_preheat", "Preheated Thorium Fuel");
        FLUIDS.put("hot_lifbe", "Hot Beryllium Fluoride");
    }

    @Override
    protected void addTranslations() {
        add("advancements.reactorcraft.recusebook.title", "Knowledge is power");
        add("advancements.reactorcraft.recusebook.description", "Realize that building nuclear reactors without understanding them is a bad idea");
        add("advancements.reactorcraft.mineuranium.title", "The Nuclear Age");
        add("advancements.reactorcraft.mineuranium.description", "Mine pitchblende");
        add("advancements.reactorcraft.minecadmium.title", "Diamoooh...wait...");
        add("advancements.reactorcraft.minecadmium.description", "Not actually diamond");
        add("advancements.reactorcraft.pebble.title", "Pebble Bed");
        add("advancements.reactorcraft.pebble.description", "Make TRISO fuel");
        add("advancements.reactorcraft.uf6.title", "Hexa-what?");
        add("advancements.reactorcraft.uf6.description", "Make uranium hexafluoride");
        add("advancements.reactorcraft.depleted.title", "Mostly useless");
        add("advancements.reactorcraft.depleted.description", "Make depleted uranium");
        add("advancements.reactorcraft.fission.title", "Splitting the Atom");
        add("advancements.reactorcraft.fission.description", "Trigger a fission reaction");
        add("advancements.reactorcraft.plutonium.title", "Synthesis");
        add("advancements.reactorcraft.plutonium.description", "Make plutonium in a breeder reactor");
        add("advancements.reactorcraft.pupoison.title", "Unsafe Handling");
        add("advancements.reactorcraft.pupoison.description", "Hold plutonium with your bare hands");
        add("advancements.reactorcraft.holdwaste.title", "That Was Dumb");
        add("advancements.reactorcraft.holdwaste.description", "Hold nuclear waste in your bare hands");
        add("advancements.reactorcraft.decay.title", "Half-Lives");
        add("advancements.reactorcraft.decay.description", "Let a whole drum of nuclear waste decay");
        add("advancements.reactorcraft.wasteleak.title", "Spent Fuel...Crater");
        add("advancements.reactorcraft.wasteleak.description", "Fail to cool a spent fuel container");
        add("advancements.reactorcraft.ammonia.title", "Efficiency");
        add("advancements.reactorcraft.ammonia.description", "Use ammonia as the working fluid in a reactor");
        add("advancements.reactorcraft.nh3explode.title", "Autoignition");
        add("advancements.reactorcraft.nh3explode.description", "Cause an ammonia explosion");
        add("advancements.reactorcraft.gigaturbine.title", "Behold Real Power");
        add("advancements.reactorcraft.gigaturbine.description", "Produce 1GW of power on a single turbine");
        add("advancements.reactorcraft.hotcore.title", "Running Hot");
        add("advancements.reactorcraft.hotcore.description", "Have a reactor overheat");
        add("advancements.reactorcraft.scram.title", "That Was Close");
        add("advancements.reactorcraft.scram.description", "Trigger an emergency shutdown");
        add("advancements.reactorcraft.meltdown.title", "Time to Move");
        add("advancements.reactorcraft.meltdown.description", "Trigger a nuclear meltdown");
        add("advancements.reactorcraft.heavywater.title", "Like Water, but Heavy");
        add("advancements.reactorcraft.heavywater.description", "Obtain heavy water");
        add("advancements.reactorcraft.candu.title", "Can Do");
        add("advancements.reactorcraft.candu.description", "Use heavy water in a nuclear reactor");
        add("advancements.reactorcraft.plasma.title", "HOW hot?");
        add("advancements.reactorcraft.plasma.description", "Make fusion plasma");
        add("advancements.reactorcraft.escape.title", "Containment Breach");
        add("advancements.reactorcraft.escape.description", "Allow fusion plasma to escape");
        add("advancements.reactorcraft.meltpipe.title", "Not-So-Containing Pipe");
        add("advancements.reactorcraft.meltpipe.description", "Fail to electrify the magnetic containment pipe");
        add("advancements.reactorcraft.fusion.title", "Stellar Energy");
        add("advancements.reactorcraft.fusion.description", "Kickstart a nuclear fusion reaction");
        add("advancements.reactorcraft.fiftygw.title", "Permanent Surplus");
        add("advancements.reactorcraft.fiftygw.description", "Generate 50GW of power from a single reactor");
        add("advancements.reactorcraft.pebblefail.title", "How did you manage THAT?");
        add("advancements.reactorcraft.pebblefail.description", "Have a pebble bed reactor fail");
        add("advancements.reactorcraft.thoriumdump.title", "Not really the point");
        add("advancements.reactorcraft.thoriumdump.description", "Trigger an emergency fuel dump");
        add("advancements.reactorcraft.plasmadie.title", "Face Full Of Fire");
        add("advancements.reactorcraft.plasmadie.description", "Die in a Plasma Leak");
        add("tab.reactorcraft", "ReactorCraft");
        add("item.reactorcraft.canister.filled", "%s Canister");
        add("config.jade.plugin_reactorcraft", "ReactorCraft");
        add("config.jade.plugin_reactorcraft.machine_state", "Machine State");
        add("jade.reactorcraft.status", "Status: %s");
        add("jade.reactorcraft.temperature", "Temperature: %s / %s °C");
        add("jade.reactorcraft.power", "Power: %s / %s W");
        add("jade.reactorcraft.shaft", "Torque: %s / %s Nm; speed: %s / %s rad/s");
        add("jade.reactorcraft.inventory", "Inventory: %s / %s slots occupied");
        add("jade.reactorcraft.fluid", "%s: %s / %s mB");
        add("jade.reactorcraft.comparator", "Comparator: %s / 15");
        add("jei.reactorcraft.item_consumed", "Item consumed");
        add("jei.reactorcraft.item_catalyst", "Catalyst; item retained");
        add("jei.reactorcraft.min_temperature", "Minimum temperature: %s °C");
        add("jei.reactorcraft.base_duration", "Base duration: %s ticks");
        add("jei.reactorcraft.neutron_chance", "%s%% chance per neutron interaction");
        add("jei.reactorcraft.intermediate_consumed", "Intermediate fluid; %s mB consumed per output step");
        add("jei.reactorcraft.output_chance", "%s%% chance");
        add("jei.reactorcraft.intermediate_time", "Intermediate stage: %s ticks");
        add("jei.reactorcraft.output_time", "Output stage: %s ticks");
        add("jei.reactorcraft.centrifuge_speed", "Minimum speed: %s rad/s");
        for (var e : FLUIDS.entrySet())
            add("fluid_type.reactorcraft." + e.getKey(), e.getValue());

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
