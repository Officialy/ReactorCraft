package reika.reactorcraft.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FlowingFluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import reika.reactorcraft.ReactorCraft;

/**
 * The complete set of ReactorCraft-owned fluids (26.2), transcribed verbatim from the 1.7.10
 * {@code ReactorCraft} {@code new Fluid("rc ...")} block — densities, viscosities, temperatures and
 * luminosities are the original values. RotaryCraft-owned fluids (lubricant, liquid nitrogen, steam)
 * are NOT registered here; reference them through {@code RotaryFluids}.
 * <p>
 * The 1.7.10 {@code setGaseous(true)} flag has no direct 26.2 {@link FluidType.Properties} setter;
 * the gaseous nature is preserved through the (often low/negative) density that the original used.
 * Each fluid has a {@link FluidType} plus a single {@code BaseFlowingFluid.Source}; since none of
 * these fluids has a distinct flowing variant in-world (they only move through machine tanks/pipes)
 * the flowing supplier is wired to the source, which avoids the {@code BaseFlowingFluid#isSame} NPE.
 * Client-side still/flow textures + per-fluid tints are bound in {@code ReactorFluidModels}.
 */
public final class ReactorFluids {

    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(BuiltInRegistries.FLUID, ReactorCraft.MODID);
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, ReactorCraft.MODID);

    /** A registered source fluid paired with its render tint (ARGB), for client model binding. */
    public record TintedFluid(DeferredHolder<Fluid, FlowingFluid> fluid, int tint) {}

    private static final List<TintedFluid> TINTED = new ArrayList<>();

    public static List<TintedFluid> tintedFluids() {
        return TINTED;
    }

    private static DeferredHolder<FluidType, FluidType> type(String name, Consumer<FluidType.Properties> cfg) {
        return FLUID_TYPES.register(name, () -> {
            FluidType.Properties p = FluidType.Properties.create();
            cfg.accept(p);
            return new FluidType(p);
        });
    }

    @SuppressWarnings("unchecked")
    private static DeferredHolder<Fluid, FlowingFluid> source(String name, DeferredHolder<FluidType, FluidType> type, int tint) {
        final DeferredHolder<Fluid, FlowingFluid>[] ref = new DeferredHolder[1];
        ref[0] = FLUIDS.register(name, () ->
                new BaseFlowingFluid.Source(new BaseFlowingFluid.Properties(type, ref[0], ref[0])));
        TINTED.add(new TintedFluid(ref[0], tint));
        return ref[0];
    }

    // --- FluidTypes ---
    public static final DeferredHolder<FluidType, FluidType> HEAVY_WATER_TYPE = type("heavy_water", p -> p.density(1100).viscosity(1050));
    public static final DeferredHolder<FluidType, FluidType> HF_TYPE = type("hydrofluoric_acid", p -> p.density(115).viscosity(10));
    public static final DeferredHolder<FluidType, FluidType> UF6_TYPE = type("uranium_hexafluoride", p -> p.density(15).viscosity(10));
    public static final DeferredHolder<FluidType, FluidType> AMMONIA_TYPE = type("ammonia", p -> p.density(682).viscosity(600));
    public static final DeferredHolder<FluidType, FluidType> SODIUM_TYPE = type("sodium", p -> p.density(927).viscosity(700).temperature(800));
    public static final DeferredHolder<FluidType, FluidType> CHLORINE_TYPE = type("chlorine", p -> p.density(320).viscosity(12));
    public static final DeferredHolder<FluidType, FluidType> OXYGEN_TYPE = type("oxygen", p -> p.density(138).viscosity(20));
    public static final DeferredHolder<FluidType, FluidType> LIQUID_OXYGEN_TYPE = type("liquid_oxygen", p -> p.density(1141).viscosity(195).temperature(90));
    public static final DeferredHolder<FluidType, FluidType> LOWP_AMMONIA_TYPE = type("low_pressure_ammonia", p -> p.density(200).viscosity(600));
    public static final DeferredHolder<FluidType, FluidType> LOWP_WATER_TYPE = type("low_pressure_water", p -> p.density(800).viscosity(800));
    public static final DeferredHolder<FluidType, FluidType> HOT_SODIUM_TYPE = type("hot_sodium", p -> p.density(720).viscosity(650).temperature(2000).lightLevel(8));
    public static final DeferredHolder<FluidType, FluidType> WARM_SODIUM_TYPE = type("warm_sodium", p -> p.density(864).viscosity(650).temperature(1100).lightLevel(6));
    public static final DeferredHolder<FluidType, FluidType> DEUTERIUM_TYPE = type("deuterium", p -> p.density(-1).viscosity(10));
    public static final DeferredHolder<FluidType, FluidType> TRITIUM_TYPE = type("tritium", p -> p.density(-1).viscosity(10));
    public static final DeferredHolder<FluidType, FluidType> CO2_TYPE = type("carbon_dioxide", p -> p.density(2).viscosity(7));
    public static final DeferredHolder<FluidType, FluidType> HOT_CO2_TYPE = type("hot_carbon_dioxide", p -> p.density(1).viscosity(5).lightLevel(2));
    public static final DeferredHolder<FluidType, FluidType> PLASMA_TYPE = type("fusion_plasma", p -> p.density(-1).viscosity(100).temperature(150000000).lightLevel(15));
    public static final DeferredHolder<FluidType, FluidType> CORIUM_TYPE = type("corium", p -> p.density(5000).viscosity(8000).temperature(2173));
    public static final DeferredHolder<FluidType, FluidType> WASTE_TYPE = type("nuclear_waste", p -> p.density(4000).viscosity(12000).temperature(800));
    public static final DeferredHolder<FluidType, FluidType> LITHIUM_TYPE = type("lithium", p -> p.density(516).viscosity(645).temperature(454).lightLevel(6));
    public static final DeferredHolder<FluidType, FluidType> LIFBE_TYPE = type("lifbe", p -> p.density(6300).viscosity(2400).temperature(773));
    public static final DeferredHolder<FluidType, FluidType> LIFBE_FUEL_TYPE = type("lifbe_fuel", p -> p.density(6750).viscosity(2550).temperature(473));
    public static final DeferredHolder<FluidType, FluidType> LIFBE_FUEL_PREHEAT_TYPE = type("lifbe_fuel_preheat", p -> p.density(6700).viscosity(2125).temperature(673));
    public static final DeferredHolder<FluidType, FluidType> HOT_LIFBE_TYPE = type("hot_lifbe", p -> p.density(6000).viscosity(2400).temperature(1273).lightLevel(8));

    // --- Source fluids (with render tint) ---
    public static final DeferredHolder<Fluid, FlowingFluid> HEAVY_WATER = source("heavy_water", HEAVY_WATER_TYPE, 0xFF3F76E4);
    public static final DeferredHolder<Fluid, FlowingFluid> HF = source("hydrofluoric_acid", HF_TYPE, 0xFFCBE34A);
    public static final DeferredHolder<Fluid, FlowingFluid> UF6 = source("uranium_hexafluoride", UF6_TYPE, 0xFFB6E6A0);
    public static final DeferredHolder<Fluid, FlowingFluid> AMMONIA = source("ammonia", AMMONIA_TYPE, 0xFFCFE8FF);
    public static final DeferredHolder<Fluid, FlowingFluid> SODIUM = source("sodium", SODIUM_TYPE, 0xFFC8C8D0);
    public static final DeferredHolder<Fluid, FlowingFluid> CHLORINE = source("chlorine", CHLORINE_TYPE, 0xFF7FE03A);
    public static final DeferredHolder<Fluid, FlowingFluid> OXYGEN = source("oxygen", OXYGEN_TYPE, 0xFFAEEFFF);
    public static final DeferredHolder<Fluid, FlowingFluid> LIQUID_OXYGEN = source("liquid_oxygen", LIQUID_OXYGEN_TYPE, 0xFF99CFFF);
    public static final DeferredHolder<Fluid, FlowingFluid> LOWP_AMMONIA = source("low_pressure_ammonia", LOWP_AMMONIA_TYPE, 0xFFDDEFFF);
    public static final DeferredHolder<Fluid, FlowingFluid> LOWP_WATER = source("low_pressure_water", LOWP_WATER_TYPE, 0xFFA9C7E0);
    public static final DeferredHolder<Fluid, FlowingFluid> HOT_SODIUM = source("hot_sodium", HOT_SODIUM_TYPE, 0xFFE6A060);
    public static final DeferredHolder<Fluid, FlowingFluid> WARM_SODIUM = source("warm_sodium", WARM_SODIUM_TYPE, 0xFFD8B090);
    public static final DeferredHolder<Fluid, FlowingFluid> DEUTERIUM = source("deuterium", DEUTERIUM_TYPE, 0xFFE0F4FF);
    public static final DeferredHolder<Fluid, FlowingFluid> TRITIUM = source("tritium", TRITIUM_TYPE, 0xFFCFFFE8);
    public static final DeferredHolder<Fluid, FlowingFluid> CO2 = source("carbon_dioxide", CO2_TYPE, 0xFFB0B0B0);
    public static final DeferredHolder<Fluid, FlowingFluid> HOT_CO2 = source("hot_carbon_dioxide", HOT_CO2_TYPE, 0xFFC8A890);
    public static final DeferredHolder<Fluid, FlowingFluid> PLASMA = source("fusion_plasma", PLASMA_TYPE, 0xFFFF4CE0);
    public static final DeferredHolder<Fluid, FlowingFluid> CORIUM = source("corium", CORIUM_TYPE, 0xFFFF5A1E);
    public static final DeferredHolder<Fluid, FlowingFluid> WASTE = source("nuclear_waste", WASTE_TYPE, 0xFF7BA22E);
    public static final DeferredHolder<Fluid, FlowingFluid> LITHIUM = source("lithium", LITHIUM_TYPE, 0xFFFF9ECF);
    public static final DeferredHolder<Fluid, FlowingFluid> LIFBE = source("lifbe", LIFBE_TYPE, 0xFF2FB6A8);
    public static final DeferredHolder<Fluid, FlowingFluid> LIFBE_FUEL = source("lifbe_fuel", LIFBE_FUEL_TYPE, 0xFF2E9E7E);
    public static final DeferredHolder<Fluid, FlowingFluid> LIFBE_FUEL_PREHEAT = source("lifbe_fuel_preheat", LIFBE_FUEL_PREHEAT_TYPE, 0xFF3FB68E);
    public static final DeferredHolder<Fluid, FlowingFluid> HOT_LIFBE = source("hot_lifbe", HOT_LIFBE_TYPE, 0xFF40D8C0);

    /** Resolves 1.7.10 {@code FluidRegistry} names ({@code "rc ammonia"}, {@code "water"}, …) to registered fluids. */
    public static Fluid getLegacyFluid(String name) {
        if (name == null || name.isEmpty())
            return null;
        return switch (name) {
            case "water" -> net.minecraft.world.level.material.Fluids.WATER;
            case "rc heavy water" -> HEAVY_WATER.get();
            case "rc hydrofluoric acid" -> HF.get();
            case "rc uranium hexafluoride" -> UF6.get();
            case "rc ammonia" -> AMMONIA.get();
            case "rc lowpwater" -> LOWP_WATER.get();
            case "rc lowpammonia" -> LOWP_AMMONIA.get();
            case "rc sodium" -> SODIUM.get();
            case "rc hot sodium" -> HOT_SODIUM.get();
            case "rc warm sodium" -> WARM_SODIUM.get();
            case "rc chlorine" -> CHLORINE.get();
            case "rc oxygen" -> OXYGEN.get();
            case "rc liquid oxygen" -> LIQUID_OXYGEN.get();
            case "rc deuterium" -> DEUTERIUM.get();
            case "rc tritium" -> TRITIUM.get();
            case "rc carbon dioxide" -> CO2.get();
            case "rc hot carbon dioxide" -> HOT_CO2.get();
            case "rc fusion plasma" -> PLASMA.get();
            case "rc corium" -> CORIUM.get();
            case "rc nuclear waste" -> WASTE.get();
            case "rc lithium" -> LITHIUM.get();
            case "rc lifbe" -> LIFBE.get();
            case "rc lifbe fuel" -> LIFBE_FUEL.get();
            case "rc lifbe fuel preheat" -> LIFBE_FUEL_PREHEAT.get();
            case "rc hot lifbe" -> HOT_LIFBE.get();
            default -> null;
        };
    }

    private ReactorFluids() {}
}
