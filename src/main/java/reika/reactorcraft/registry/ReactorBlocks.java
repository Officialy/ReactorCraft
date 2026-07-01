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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.blocks.BlockCoriumFlowing;
import reika.reactorcraft.blocks.BlockReactorDuct;
import reika.reactorcraft.blocks.BlockReactorLine;
import reika.reactorcraft.blocks.BlockReactorMachine;
import reika.reactorcraft.blocks.BlockReactorMachineModelled;
import reika.reactorcraft.blocks.BlockReactorMat;
import reika.reactorcraft.blocks.BlockSteam;
import reika.reactorcraft.blocks.BlockThoriumFuel;
import reika.reactorcraft.blocks.multi.BlockFlywheelMulti;
import reika.reactorcraft.blocks.multi.BlockGeneratorMulti;
import reika.reactorcraft.blocks.multi.BlockSolenoidCasing;
import reika.reactorcraft.blocks.multi.BlockSolenoidCasing.SolenoidPart;
import reika.reactorcraft.blocks.multi.BlockTurbineMulti;

/**
 * 26.2 block registry for the ore→fuel slice, replacing the 1.7.10 metadata block enum
 * ({@code ReactorBlocks.ORE} held all ore types in metadata; {@code FLUORITEORE} held the eight
 * colours). Here each ore type and each fluorite colour is its own block; drops and smelting are
 * data-driven (loot tables + smelting recipes). The {@code setId} ThreadLocal mirrors
 * {@code RotaryBlocks}, required because {@code BlockBehaviour.Properties} must be id-stamped before
 * the {@code Block} constructor runs in 26.2.
 */
public final class ReactorBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ReactorCraft.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ReactorCraft.MODID);

    private static final ThreadLocal<ResourceKey<Block>> CURRENT_BLOCK_KEY = new ThreadLocal<>();

    public static BlockBehaviour.Properties blockProperties() {
        BlockBehaviour.Properties p = BlockBehaviour.Properties.of();
        ResourceKey<Block> k = CURRENT_BLOCK_KEY.get();
        if (k != null) p.setId(k);
        return p;
    }

    private static DeferredBlock<Block> register(String name, Supplier<Block> factory) {
        DeferredBlock<Block> block = BLOCKS.register(name, rl -> {
            CURRENT_BLOCK_KEY.set(ResourceKey.create(Registries.BLOCK, rl));
            try {
                return factory.get();
            } finally {
                CURRENT_BLOCK_KEY.remove();
            }
        });
        ITEMS.registerSimpleBlockItem(block);
        return block;
    }

    private static Block ore() {
        return new Block(blockProperties().strength(3.0F, 5.0F).requiresCorrectToolForDrops().sound(SoundType.STONE));
    }

    private static Block storage() {
        return new Block(blockProperties().strength(5.0F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.METAL));
    }

    public static final DeferredBlock<Block> PITCHBLENDE_ORE = register("pitchblende_ore", ReactorBlocks::ore);
    public static final DeferredBlock<Block> END_PITCHBLENDE_ORE = register("end_pitchblende_ore", ReactorBlocks::ore);
    public static final DeferredBlock<Block> CADMIUM_ORE = register("cadmium_ore", ReactorBlocks::ore);
    public static final DeferredBlock<Block> INDIUM_ORE = register("indium_ore", ReactorBlocks::ore);
    public static final DeferredBlock<Block> SILVER_ORE = register("silver_ore", ReactorBlocks::ore);
    public static final DeferredBlock<Block> AMMONIUM_ORE = register("ammonium_ore", ReactorBlocks::ore);
    public static final DeferredBlock<Block> CALCITE_ORE = register("calcite_ore", ReactorBlocks::ore);
    public static final DeferredBlock<Block> MAGNETITE_ORE = register("magnetite_ore", ReactorBlocks::ore);
    public static final DeferredBlock<Block> THORIUM_ORE = register("thorium_ore", ReactorBlocks::ore);

    public static final EnumMap<FluoriteTypes, DeferredBlock<Block>> FLUORITE_ORE = new EnumMap<>(FluoriteTypes.class);
    static {
        for (FluoriteTypes f : FluoriteTypes.colorList) {
            FLUORITE_ORE.put(f, register(f.getOreBlockName(), ReactorBlocks::ore));
        }
    }

    // Storage / decorative material blocks referenced by the crafting recipes.
    public static final DeferredBlock<Block> GRAPHITE_BLOCK = register("graphite_block", ReactorBlocks::storage);
    public static final DeferredBlock<Block> CALCITE_BLOCK = register("calcite_block", ReactorBlocks::storage);
    public static final DeferredBlock<Block> LODESTONE_BLOCK = register("lodestone_block", ReactorBlocks::storage);

    public static Block fluoriteOre(FluoriteTypes f) {
        return FLUORITE_ORE.get(f).get();
    }

    private static BlockBehaviour.Properties machineProperties() {
        return blockProperties().strength(4.0F, 15.0F).requiresCorrectToolForDrops().sound(SoundType.METAL);
    }

    private static DeferredBlock<Block> registerMachine(String name, Supplier<Block> factory) {
        return register(name, factory);
    }

    /** Register a block with no block-item (technical blocks like flowing steam). */
    private static DeferredBlock<Block> registerNoItem(String name, Supplier<Block> factory) {
        return BLOCKS.register(name, rl -> {
            CURRENT_BLOCK_KEY.set(ResourceKey.create(Registries.BLOCK, rl));
            try {
                return factory.get();
            } finally {
                CURRENT_BLOCK_KEY.remove();
            }
        });
    }

    // --- Multiblock machine casings (named-blockstate variants; see Block*Multi) ---
    public static final DeferredBlock<Block> GENERATORMULTI = registerMachine("generator_multi", () -> new BlockGeneratorMulti(machineProperties().noOcclusion()));
    public static final DeferredBlock<Block> FLYWHEELMULTI = registerMachine("flywheel_multi", () -> new BlockFlywheelMulti(machineProperties().noOcclusion()));
    // Solenoid casing split into six individually-named, individually-placeable blocks (was one
    // block with a `part` metadata property). Validation/forming keys on block identity now; see
    // BlockSolenoidCasing.layout(). The SolenoidPart enum names the structural ROLE (face/edge/wall/
    // spoke/shell); the registry id is the canonical in-game flavour name (from the 1.7.10 lang).
    public static final DeferredBlock<Block> FERROMAGNETIC_BASE = registerMachine("ferromagnetic_base", () -> new BlockSolenoidCasing(machineProperties().noOcclusion(), SolenoidPart.FACE));
    public static final DeferredBlock<Block> MAGNETIC_LINKAGE = registerMachine("magnetic_linkage", () -> new BlockSolenoidCasing(machineProperties().noOcclusion(), SolenoidPart.EDGE));
    public static final DeferredBlock<Block> CENTRAL_MAGNET = registerMachine("central_permanent_magnet", () -> new BlockSolenoidCasing(machineProperties().noOcclusion(), SolenoidPart.WALL));
    public static final DeferredBlock<Block> AUXILIARY_MAGNET = registerMachine("auxiliary_permanent_magnet", () -> new BlockSolenoidCasing(machineProperties().noOcclusion(), SolenoidPart.WALL_EDGE));
    public static final DeferredBlock<Block> HYSTERESIS_ROD = registerMachine("hysteresis_rod", () -> new BlockSolenoidCasing(machineProperties().noOcclusion(), SolenoidPart.SPOKE));
    public static final DeferredBlock<Block> SOLENOID_HUB = registerMachine("solenoid_hub", () -> new BlockSolenoidCasing(machineProperties().noOcclusion(), SolenoidPart.SHELL));
    public static final DeferredBlock<Block> TURBINEMULTI = registerMachine("turbine_multi", () -> new BlockTurbineMulti(machineProperties().noOcclusion()));

    // --- Reactor material block (6 variants incl. scrubber/graphite/lodestone; see MatBlocks) ---
    public static final DeferredBlock<Block> MATS = register("reactor_mat", () -> new BlockReactorMat(machineProperties().randomTicks().noOcclusion()));
    public static final DeferredBlock<Block> CORIUMFLOWING = register("corium", () -> new BlockCoriumFlowing(machineProperties().randomTicks().noOcclusion().strength(100, 500)));

    // --- Molten LiFBe thorium fuel pool (finite-fluid replacement; placed/consumed by the fuel dump) ---
    public static final DeferredBlock<Block> THORIUM_FUEL = registerNoItem("thorium_fuel", () -> new BlockThoriumFuel(blockProperties().strength(100, 500).lightLevel(s -> 7).randomTicks().noOcclusion().noLootTable()));

    // --- Flowing steam (air-like, self-propagating toward turbines) ---
    public static final DeferredBlock<Block> STEAM = registerNoItem("steam", () -> new BlockSteam(
            blockProperties().strength(3600000.0F).noCollision().noLootTable().replaceable().noOcclusion()));

    // --- Reactor machine blocks (one DeferredBlock per ReactorTiles constant) ---
    public static final DeferredBlock<Block> FUEL = registerMachine("fuel_rod", () -> new BlockReactorMachine(machineProperties()));
    public static final DeferredBlock<Block> CONTROL = registerMachine("control_rod", () -> new BlockReactorMachineModelled(machineProperties()));
    public static final DeferredBlock<Block> COOLANT = registerMachine("coolant_cell", () -> new BlockReactorMachine(machineProperties()));
    public static final DeferredBlock<Block> CPU = registerMachine("reactor_cpu", () -> new BlockReactorMachine(machineProperties()));
    public static final DeferredBlock<Block> TURBINECORE = registerMachine("turbine_core", () -> new BlockReactorMachineModelled(machineProperties()));
    public static final DeferredBlock<Block> CONDENSER = registerMachine("condenser", () -> new BlockReactorMachineModelled(machineProperties()));
    public static final DeferredBlock<Block> STEAMLINE = registerMachine("steam_line", () -> new BlockReactorLine(machineProperties()));
    public static final DeferredBlock<Block> FLUIDEXTRACTOR = registerMachine("heavy_pump", () -> new BlockReactorMachineModelled(machineProperties()));
    public static final DeferredBlock<Block> CENTRIFUGE = registerMachine("isotope_centrifuge", () -> new BlockReactorMachineModelled(machineProperties()));
    public static final DeferredBlock<Block> PROCESSOR = registerMachine("uranium_processor", () -> new BlockReactorMachineModelled(machineProperties()));
    public static final DeferredBlock<Block> WASTECONTAINER = registerMachine("waste_container", () -> new BlockReactorMachine(machineProperties()));
    public static final DeferredBlock<Block> BOILER = registerMachine("reactor_boiler", () -> new BlockReactorMachine(machineProperties()));
    public static final DeferredBlock<Block> GRATE = registerMachine("steam_grate", () -> new BlockReactorMachineModelled(machineProperties()));
    public static final DeferredBlock<Block> PUMP = registerMachine("reactor_pump", () -> new BlockReactorMachineModelled(machineProperties()));
    public static final DeferredBlock<Block> SYNTHESIZER = registerMachine("synthesizer", () -> new BlockReactorMachine(machineProperties()));
    public static final DeferredBlock<Block> MAGNET = registerMachine("toroid_magnet", () -> new BlockReactorMachineModelled(machineProperties()));
    public static final DeferredBlock<Block> ELECTROLYZER = registerMachine("electrolyzer", () -> new BlockReactorMachineModelled(machineProperties()));
    public static final DeferredBlock<Block> TRITIZER = registerMachine("tritizer", () -> new BlockReactorMachine(machineProperties()));
    public static final DeferredBlock<Block> BREEDER = registerMachine("breeder_core", () -> new BlockReactorMachine(machineProperties()));
    public static final DeferredBlock<Block> SODIUMBOILER = registerMachine("sodium_boiler", () -> new BlockReactorMachine(machineProperties()));
    public static final DeferredBlock<Block> EXCHANGER = registerMachine("heat_exchanger", () -> new BlockReactorMachineModelled(machineProperties()));
    public static final DeferredBlock<Block> STORAGE = registerMachine("waste_storage", () -> new BlockReactorMachineModelled(machineProperties()));
    public static final DeferredBlock<Block> INJECTOR = registerMachine("fusion_injector", () -> new BlockReactorMachine(machineProperties()));
    public static final DeferredBlock<Block> HEATER = registerMachine("fusion_heater", () -> new BlockReactorMachine(machineProperties()));
    public static final DeferredBlock<Block> GASPIPE = registerMachine("gas_duct", () -> new BlockReactorDuct(machineProperties()));
    public static final DeferredBlock<Block> MAGNETPIPE = registerMachine("magnetic_pipe", () -> new BlockReactorDuct(machineProperties()));
    public static final DeferredBlock<Block> ABSORBER = registerMachine("neutron_absorber", () -> new BlockReactorMachine(machineProperties()));
    public static final DeferredBlock<Block> SOLENOID = registerMachine("solenoid_magnet", () -> new BlockReactorMachineModelled(machineProperties()));
    public static final DeferredBlock<Block> COLLECTOR = registerMachine("gas_collector", () -> new BlockReactorMachineModelled(machineProperties()));
    public static final DeferredBlock<Block> PEBBLEBED = registerMachine("pebble_bed", () -> new BlockReactorMachine(machineProperties()));
    public static final DeferredBlock<Block> CO2HEATER = registerMachine("co2_heater", () -> new BlockReactorMachine(machineProperties()));
    public static final DeferredBlock<Block> FLYWHEEL = registerMachine("turbine_flywheel", () -> new BlockReactorMachineModelled(machineProperties()));
    public static final DeferredBlock<Block> REFLECTOR = registerMachine("neutron_reflector", () -> new BlockReactorMachine(machineProperties()));
    public static final DeferredBlock<Block> GENERATOR = registerMachine("reactor_generator", () -> new BlockReactorMachineModelled(machineProperties()));
    public static final DeferredBlock<Block> MARKER = registerMachine("fusion_marker", () -> new BlockReactorMachineModelled(machineProperties()));
    public static final DeferredBlock<Block> TURBINEMETER = registerMachine("turbine_meter", () -> new BlockReactorMachine(machineProperties()));
    public static final DeferredBlock<Block> BIGTURBINE = registerMachine("high_pressure_turbine", () -> new BlockReactorMachineModelled(machineProperties()));
    public static final DeferredBlock<Block> DIFFUSER = registerMachine("steam_diffuser", () -> new BlockReactorMachineModelled(machineProperties()));
    public static final DeferredBlock<Block> THORIUM = registerMachine("thorium_core", () -> new BlockReactorMachine(machineProperties()));
    public static final DeferredBlock<Block> WASTEPIPE = registerMachine("waste_pipe", () -> new BlockReactorDuct(machineProperties()));
    public static final DeferredBlock<Block> FUELDUMP = registerMachine("fuel_dump", () -> new BlockReactorMachine(machineProperties()));
    public static final DeferredBlock<Block> SOLAR = registerMachine("solar_exchanger", () -> new BlockReactorMachineModelled(machineProperties()));
    public static final DeferredBlock<Block> SOLARTOP = registerMachine("solar_top", () -> new BlockReactorMachineModelled(machineProperties()));
    public static final DeferredBlock<Block> MINITURBINE = registerMachine("mini_turbine", () -> new BlockReactorMachineModelled(machineProperties()));
    public static final DeferredBlock<Block> HEATPIPE = registerMachine("heat_pipe", () -> new BlockReactorLine(machineProperties()));
    public static final DeferredBlock<Block> WASTEDECAYER = registerMachine("waste_decayer", () -> new BlockReactorMachine(machineProperties()));

    private ReactorBlocks() {}
}
