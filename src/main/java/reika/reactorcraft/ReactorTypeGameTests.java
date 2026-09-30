package reika.reactorcraft;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import reika.reactorcraft.base.TileEntityIntermediateBoiler;
import reika.dragonapi.instantiable.HybridTank;
import reika.reactorcraft.blocks.multi.BlockHeaterMulti;
import reika.reactorcraft.entities.EntityFusion;
import reika.reactorcraft.entities.EntityNeutron;
import reika.reactorcraft.entities.EntityNeutron.NeutronType;
import reika.reactorcraft.registry.ReactorBlocks;
import reika.reactorcraft.registry.ReactorFluids;
import reika.reactorcraft.registry.ReactorItems;
import reika.reactorcraft.registry.ReactorType;
import reika.reactorcraft.tileentities.TileEntitySolarTop;
import reika.reactorcraft.tileentities.TileEntityMagneticPipe;
import reika.reactorcraft.tileentities.fission.TileEntityReactorBoiler;
import reika.reactorcraft.tileentities.fission.TileEntityFuelRod;
import reika.reactorcraft.tileentities.fission.breeder.TileEntityBreederCore;
import reika.reactorcraft.tileentities.fission.breeder.TileEntitySodiumHeater;
import reika.reactorcraft.tileentities.fission.thorium.TileEntityThoriumCore;
import reika.reactorcraft.tileentities.fusion.TileEntityFusionHeater;
import reika.reactorcraft.tileentities.fusion.TileEntityNeutronAbsorber;
import reika.reactorcraft.tileentities.htgr.TileEntityCO2Heater;
import reika.reactorcraft.tileentities.htgr.TileEntityPebbleBed;
import reika.reactorcraft.tileentities.powergen.TileEntityHeatExchanger;
import reika.reactorcraft.tileentities.powergen.TileEntityHeatExchanger.Exchange;
import reika.reactorcraft.tileentities.powergen.TileEntitySolarExchanger;
import reika.rotarycraft.auxiliary.interfaces.SodiumSolarUpgrades.SodiumSolarOutput;
import reika.rotarycraft.base.blocks.BlockRotaryCraftMachine;
import reika.rotarycraft.blockentities.auxiliary.BlockEntityMirror;
import reika.rotarycraft.blockentities.production.BlockEntitySolarTower;
import reika.rotarycraft.blockentities.transmission.BlockEntityCreativeCoil;
import reika.rotarycraft.registry.RotaryBlocks;

/** Functional coverage of every generating ReactorType; uranium and turbines also live in ReactorGameTests. */
public final class ReactorTypeGameTests {
    private static final BlockPos MACHINE = new BlockPos(3, 2, 4);

    private ReactorTypeGameTests() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        ReactorGameTests.register(event, environment, "breeder_fuel_loading_and_neutron_heat", 60, ReactorTypeGameTests::breederFuel);
        ReactorGameTests.register(event, environment, "fission_plutonium_burns_to_empty", 60, ReactorTypeGameTests::plutoniumFuel);
        ReactorGameTests.register(event, environment, "breeder_heats_sodium", 80, h -> heatCoolant(h, true, false));
        ReactorGameTests.register(event, environment, "breeder_sodium_temperature_gate", 80, h -> heatCoolant(h, true, true));
        ReactorGameTests.register(event, environment, "htgr_heats_co2", 80, h -> heatCoolant(h, false, false));
        ReactorGameTests.register(event, environment, "htgr_co2_temperature_gate", 80, h -> heatCoolant(h, false, true));
        ReactorGameTests.register(event, environment, "breeder_sodium_heater_backpressure", 100, h -> heaterBackpressure(h, true));
        ReactorGameTests.register(event, environment, "htgr_co2_heater_backpressure", 100, h -> heaterBackpressure(h, false));
        ReactorGameTests.register(event, environment, "htgr_pebbles_feed_and_generate_heat", 620, ReactorTypeGameTests::pebbleBed);
        ReactorGameTests.register(event, environment, "breeder_hot_sodium_exchange", 100, h -> exchange(h, Exchange.SODIUM));
        ReactorGameTests.register(event, environment, "htgr_hot_co2_exchange", 100, h -> exchange(h, Exchange.CO2));
        ReactorGameTests.register(event, environment, "thorium_hot_lifbe_exchange", 100, h -> exchange(h, Exchange.LIFBE));
        ReactorGameTests.register(event, environment, "solar_warm_sodium_exchange", 100, h -> exchange(h, Exchange.SOLARSODIUM));
        ReactorGameTests.register(event, environment, "heat_exchanger_power_and_batch_gates", 100, ReactorTypeGameTests::exchangeGates);
        ReactorGameTests.register(event, environment, "thorium_neutrons_heat_fuel_salt", 60, ReactorTypeGameTests::thoriumFuel);
        ReactorGameTests.register(event, environment, "thorium_overheat_dumps_fuel", 60, ReactorTypeGameTests::thoriumDump);
        ReactorGameTests.register(event, environment, "fusion_preheater_makes_plasma", 80, h -> fusionHeater(h, false, false));
        ReactorGameTests.register(event, environment, "fusion_preheater_requires_temperature", 80, h -> fusionHeater(h, true, false));
        ReactorGameTests.register(event, environment, "fusion_preheater_requires_casing", 80, h -> fusionHeater(h, false, true));
        ReactorGameTests.register(event, environment, "fusion_preheater_requires_both_isotopes", 80, ReactorTypeGameTests::fusionIsotopes);
        ReactorGameTests.register(event, environment, "fusion_neutrons_heat_absorbers", 80, ReactorTypeGameTests::fusionNeutrons);
        ReactorGameTests.register(event, environment, "fusion_absorber_boils_water", 120, ReactorTypeGameTests::fusionBoiler);
        ReactorGameTests.register(event, environment, "solar_exchanger_power_capacity_and_transactions", 100, ReactorTypeGameTests::solarOutput);
        ReactorGameTests.register(event, environment, "solar_tower_sodium_receiver_and_return", 120, ReactorTypeGameTests::solarTower);
        ReactorGameTests.register(event, environment, "solar_mirror_field_discovery_and_removal", 100, ReactorTypeGameTests::solarMirrors);
        ReactorGameTests.register(event, environment, "solar_tower_water_mode", 100, ReactorTypeGameTests::solarWater);
    }

    private static ResourceHandler<FluidResource> fluids(GameTestHelper h, BlockPos pos, Direction side) {
        var handler = h.getLevel().getCapability(Capabilities.Fluid.BLOCK, h.absolutePos(pos), side);
        h.assertTrue(handler != null, "missing fluid capability at " + pos + " on " + side);
        return handler;
    }

    private static void fill(GameTestHelper h, ResourceHandler<FluidResource> handler, Fluid fluid, int amount) {
        try (Transaction tx = Transaction.openRoot()) {
            h.assertTrue(handler.insert(FluidResource.of(fluid), amount, tx) == amount, "must accept exactly " + amount + " mB of " + fluid);
            tx.commit();
        }
    }

    private static BlockEntityCreativeCoil power(GameTestHelper h, BlockPos machine) {
        BlockPos pos = machine.below();
        h.setBlock(pos.west(), Blocks.REDSTONE_BLOCK);
        h.setBlock(pos, RotaryBlocks.CREATIVE_COIL.get().defaultBlockState().setValue(BlockRotaryCraftMachine.FACING, Direction.DOWN));
        var coil = h.getBlockEntity(pos, BlockEntityCreativeCoil.class);
        coil.setReleaseTorque(64);
        coil.setReleaseOmega(4096);
        return coil;
    }

    private static void breederFuel(GameTestHelper h) {
        h.setBlock(MACHINE, ReactorBlocks.BREEDER.get());
        var core = h.getBlockEntity(MACHINE, TileEntityBreederCore.class);
        var fuel = ReactorItems.BREEDERFUEL.getStackOf();
        h.assertTrue(core.isItemValidForSlot(0, fuel), "an empty breeder fuel slot must accept breeder fuel");
        h.assertTrue(!core.isItemValidForSlot(4, fuel), "waste slots must reject breeder fuel");
        core.setItem(0, fuel);
        h.assertTrue(!core.isItemValidForSlot(0, fuel), "occupied fuel slots must reject a second rod");
        h.runAfterDelay(5, () -> {
            int before = core.getTemperature();
            for (int i = 0; i < 200 && core.getTemperature() == before; i++)
                core.onNeutron(new EntityNeutron(h.getLevel(), h.absolutePos(MACHINE), Direction.EAST, NeutronType.BREEDER), h.getLevel(), h.absolutePos(MACHINE));
            h.assertTrue(core.isFissile() && core.getTemperature() > before && core.getReactorType() == ReactorType.BREEDER,
                    "a fueled breeder must turn interacting neutrons into breeder heat");
            h.succeed();
        });
    }

    private static void plutoniumFuel(GameTestHelper h) {
        h.setBlock(MACHINE, ReactorBlocks.FUEL.get());
        var rod = h.getBlockEntity(MACHINE, TileEntityFuelRod.class);
        rod.setItem(0, ReactorItems.PLUTONIUM.getStackOfMetadata(ReactorItems.PLUTONIUM.getNumberMetadatas() - 1));
        h.runAfterDelay(5, () -> {
            h.assertTrue(rod.getItem(3).is(ReactorItems.PLUTONIUM.getItemInstance()), "plutonium must feed down to the active fuel slot");
            int before = rod.getTemperature();
            // Consumption is probabilistic (4% per fission); allow enough independent interactions.
            for (int i = 0; i < 4000 && rod.isFissile(); i++)
                rod.onNeutron(new EntityNeutron(h.getLevel(), h.absolutePos(MACHINE), Direction.EAST, NeutronType.FISSION), h.getLevel(), h.absolutePos(MACHINE));
            h.assertTrue(rod.getItem(3).isEmpty() && rod.getTemperature() > before && rod.getReactorType() == ReactorType.FISSION,
                    "a plutonium rod at its last burn stage must release fission heat and leave an empty fuel slot");
            h.setBlock(MACHINE, Blocks.AIR);
            h.succeed();
        });
    }

    private static void heatCoolant(GameTestHelper h, boolean sodium, boolean cold) {
        h.setBlock(MACHINE, sodium ? ReactorBlocks.SODIUMBOILER.get() : ReactorBlocks.CO2HEATER.get());
        TileEntityIntermediateBoiler heater = sodium ? h.getBlockEntity(MACHINE, TileEntitySodiumHeater.class) : h.getBlockEntity(MACHINE, TileEntityCO2Heater.class);
        Fluid input = sodium ? ReactorFluids.SODIUM.get() : ReactorFluids.CO2.get();
        Fluid output = sodium ? ReactorFluids.HOT_SODIUM.get() : ReactorFluids.HOT_CO2.get();
        var in = fluids(h, MACHINE, Direction.DOWN);
        var out = fluids(h, MACHINE, Direction.UP);
        fill(h, in, input, 100);
        heater.setTemperature(cold ? heater.getMinimumTemperature() - 100 : heater.getMinimumTemperature() + 200);
        h.assertTrue(heater.canHeat() != cold, "coolant heating must require the correct fluid and threshold temperature");
        h.runAfterDelay(25, () -> {
            h.assertTrue(in.getAmountAsInt(0) == (cold ? 100 : 0) && out.getAmountAsInt(0) == (cold ? 0 : 100), "one heating batch must conserve 100 mB; cold coolant must stay untouched");
            if (!cold) h.assertTrue(out.getResource(0).getFluid() == output, "heated coolant must have the reactor's hot fluid identity");
            h.assertTrue(heater.getReactorType() == (sodium ? ReactorType.BREEDER : ReactorType.HTGR), "coolant heater must retain its reactor type");
            h.succeed();
        });
    }

    private static void pebbleBed(GameTestHelper h) {
        List<TileEntityPebbleBed> cores = new ArrayList<>();
        for (int x = 2; x <= 5; x++) for (int z = 2; z <= 7; z++) {
            BlockPos pos = new BlockPos(x, 2, z);
            h.setBlock(pos, ReactorBlocks.PEBBLEBED.get());
            var core = h.getBlockEntity(pos, TileEntityPebbleBed.class);
            core.setTemperature(core.getAmbientTemperature());
            core.setItem(core.getContainerSize() - 1, ReactorItems.PELLET.getStackOf());
            cores.add(core);
        }
        BlockPos bottom = new BlockPos(2, 1, 2);
        h.setBlock(bottom, ReactorBlocks.PEBBLEBED.get());
        var lower = h.getBlockEntity(bottom, TileEntityPebbleBed.class);
        lower.setTemperature(lower.getAmbientTemperature());
        h.succeedWhen(() -> {
            h.assertTrue(!lower.isEmpty(), "gravity must feed a pellet from the upper core into the lower core");
            int fuelCount = lower.isEmpty() ? 0 : 1;
            for (var core : cores) for (int i = 0; i < core.getContainerSize(); i++) if (!core.getItem(i).isEmpty()) fuelCount++;
            h.assertTrue(fuelCount == 24, "pebble feeding and burnup must conserve all 24 pellets");
            h.assertTrue(cores.stream().anyMatch(c -> c.getTemperature() > c.getAmbientTemperature() + 10), "fueled HTGR cores must generate heat through real decay ticks");
            h.assertTrue(lower.getReactorType() == ReactorType.HTGR, "pebble bed must identify its heat as HTGR");
        });
    }

    private static void heaterBackpressure(GameTestHelper h, boolean sodium) {
        h.setBlock(MACHINE, sodium ? ReactorBlocks.SODIUMBOILER.get() : ReactorBlocks.CO2HEATER.get());
        TileEntityIntermediateBoiler heater = sodium ? h.getBlockEntity(MACHINE, TileEntitySodiumHeater.class) : h.getBlockEntity(MACHINE, TileEntityCO2Heater.class);
        Fluid cold = sodium ? ReactorFluids.SODIUM.get() : ReactorFluids.CO2.get();
        Fluid hot = sodium ? ReactorFluids.HOT_SODIUM.get() : ReactorFluids.HOT_CO2.get();
        fill(h, fluids(h, MACHINE, Direction.DOWN), cold, 100);
        heater.setTemperature(heater.getMinimumTemperature() + 200);
        // Exercise the actual saved tank format, including a nearly full output loaded from disk.
        var saved = heater.saveWithoutMetadata(h.getLevel().registryAccess());
        var tank = new HybridTank(heater.getName().toLowerCase(Locale.ENGLISH) + "out", heater.getCapacity());
        tank.setContents(heater.getCapacity() - 50, hot);
        tank.writeToNBT(h.getLevel().registryAccess(), saved);
        heater.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, h.getLevel().registryAccess(), saved));
        var output = fluids(h, MACHINE, Direction.UP);
        h.assertTrue(!heater.canHeat(), "a 100 mB batch must wait when only 50 mB of output space remains");
        h.startSequence().thenIdle(25).thenExecute(() -> {
            h.assertTrue(heater.getInputFluidLevel() == 100 && output.getAmountAsInt(0) == heater.getCapacity() - 50, "blocked heating must not lose coolant");
            try (Transaction tx = Transaction.openRoot()) { output.extract(FluidResource.of(hot), 50, tx); tx.commit(); }
            h.assertTrue(heater.canHeat(), "draining 50 mB must make room for exactly one full heating batch");
        }).thenIdle(20).thenExecute(() -> h.assertTrue(heater.getInputFluidLevel() == 0 && output.getAmountAsInt(0) == heater.getCapacity(), "heating must resume without truncating its batch")).thenSucceed();
    }

    private static void exchange(GameTestHelper h, Exchange recipe) {
        h.setBlock(MACHINE, ReactorBlocks.EXCHANGER.get());
        var exchanger = h.getBlockEntity(MACHINE, TileEntityHeatExchanger.class);
        power(h, MACHINE);
        var input = fluids(h, MACHINE, Direction.UP);
        var output = fluids(h, MACHINE, Direction.EAST);
        fill(h, input, recipe.hotFluid, 2000);
        h.setBlock(MACHINE.east(), ReactorBlocks.BOILER.get());
        var boiler = h.getBlockEntity(MACHINE.east(), TileEntityReactorBoiler.class);
        boiler.addLiquid(2000, Fluids.WATER);
        h.runAfterDelay(22, () -> {
            int cold = output.getAmountAsInt(0);
            h.assertTrue(cold > 0 && cold + input.getAmountAsInt(0) == 2000, "hot-to-cold exchange must conserve the coolant");
            h.assertTrue(output.getResource(0).getFluid() == recipe.coldFluid && exchanger.getTemperature() > 0, "powered exchange must return the correct cold fluid and release heat");
            h.assertTrue(boiler.getTemperature() > 0 && boiler.getReactorType() == recipe.type, "heat entering the steam boiler must preserve " + recipe.type + " provenance, found " + boiler.getReactorType());
            h.succeed();
        });
    }

    private static void exchangeGates(GameTestHelper h) {
        h.setBlock(MACHINE, ReactorBlocks.EXCHANGER.get());
        var input = fluids(h, MACHINE, Direction.UP);
        var output = fluids(h, MACHINE, Direction.EAST);
        fill(h, input, ReactorFluids.HOT_SODIUM.get(), 99);
        h.startSequence().thenIdle(5).thenExecute(() -> {
            h.assertTrue(input.getAmountAsInt(0) == 99 && output.getAmountAsInt(0) == 0, "unpowered exchanger must not convert coolant");
            var coil = power(h, MACHINE);
            coil.setReleaseOmega(256);
            fill(h, input, ReactorFluids.HOT_SODIUM.get(), 1);
        }).thenIdle(5).thenExecute(() -> {
            h.assertTrue(input.getAmountAsInt(0) == 100 && output.getAmountAsInt(0) == 0, "power above 8192 W must still satisfy the 512 rad/s speed gate");
            h.getBlockEntity(MACHINE.below(), BlockEntityCreativeCoil.class).setReleaseOmega(4096);
        }).thenIdle(5).thenExecute(() -> h.assertTrue(input.getAmountAsInt(0) == 0 && output.getAmountAsInt(0) == 100, "valid shaft power must convert exactly one full batch")).thenSucceed();
    }

    private static void thoriumFuel(GameTestHelper h) {
        h.setBlock(MACHINE, ReactorBlocks.THORIUM.get());
        var core = h.getBlockEntity(MACHINE, TileEntityThoriumCore.class);
        var tanks = fluids(h, MACHINE, null);
        fill(h, fluids(h, MACHINE, Direction.UP), ReactorFluids.LIFBE_FUEL.get(), 100);
        core.setTemperature(500);
        for (int i = 0; i < 100; i++) core.onNeutron(new EntityNeutron(h.getLevel(), h.absolutePos(MACHINE), Direction.EAST, NeutronType.BREEDER), h.getLevel(), h.absolutePos(MACHINE));
        h.assertTrue(tanks.getAmountAsInt(0) == 100 && tanks.getAmountAsInt(1) == 0, "breeder neutrons must not burn thorium fuel");
        for (int i = 0; i < 400 && tanks.getAmountAsInt(1) == 0; i++) core.onNeutron(new EntityNeutron(h.getLevel(), h.absolutePos(MACHINE), Direction.EAST, NeutronType.THORIUM), h.getLevel(), h.absolutePos(MACHINE));
        h.assertTrue(tanks.getAmountAsInt(0) == 0 && tanks.getAmountAsInt(1) == 100 && tanks.getResource(1).getFluid() == ReactorFluids.HOT_LIFBE.get(), "thorium fission must consume exactly 100 mB of fuel and produce hot LiFBe salt");
        h.assertTrue(core.getTemperature() == 550 && core.getReactorType() == ReactorType.THORIUM, "thorium fission must generate 50 degrees of thorium heat");
        h.succeed();
    }

    private static void thoriumDump(GameTestHelper h) {
        BlockPos corePos = MACHINE.above();
        h.setBlock(corePos, ReactorBlocks.THORIUM.get());
        h.setBlock(MACHINE, ReactorBlocks.FUELDUMP.get());
        var core = h.getBlockEntity(corePos, TileEntityThoriumCore.class);
        var input = fluids(h, corePos, Direction.UP);
        fill(h, input, ReactorFluids.LIFBE_FUEL.get(), 1000);
        core.setTemperature(1150);
        h.runAfterDelay(5, () -> {
            h.assertTrue(input.getAmountAsInt(0) == 0, "overheated thorium core must drain into its fuel dump");
            h.assertTrue(h.getLevel().getBlockState(h.absolutePos(MACHINE.below())).is(ReactorBlocks.THORIUM_FUEL.get()), "fuel dump must deposit thorium fuel below itself");
            h.succeed();
        });
    }

    private static void fusionHeater(GameTestHelper h, boolean cold, boolean incomplete) {
        BlockPos pos = new BlockPos(4, 3, 4);
        if (!incomplete) BlockHeaterMulti.layout(pos).forEach((location, block) -> {
            h.setBlock(location, block);
            if (block == ReactorBlocks.MAGNETPIPE.get())
                h.getBlockEntity(location, TileEntityMagneticPipe.class).onDischarge(4096, 1);
        });
        h.setBlock(pos, ReactorBlocks.HEATER.get());
        var heater = h.getBlockEntity(pos, TileEntityFusionHeater.class);
        var tanks = fluids(h, pos, null);
        fill(h, tanks, ReactorFluids.DEUTERIUM.get(), 100);
        fill(h, tanks, ReactorFluids.TRITIUM.get(), 100);
        heater.setTemperature(cold ? 1000 : TileEntityFusionHeater.PLASMA_TEMP + 1000000);
        h.runAfterDelay(6, () -> {
            // Plasma can move into the real magnetic outlet; include it in the conserved output.
            int made = 200 - tanks.getAmountAsInt(0) - tanks.getAmountAsInt(1);
            h.assertTrue(made == (cold || incomplete ? 0 : 200), "plasma requires both isotopes, the fusion temperature and a complete physical casing; made=" + made + ", temperature=" + heater.getTemperature() + ", formed=" + heater.hasMultiBlock());
            if (!incomplete) {
                int stored = tanks.getAmountAsInt(2);
                for (var entry : BlockHeaterMulti.layout(pos).entrySet()) if (entry.getValue() == ReactorBlocks.MAGNETPIPE.get()) {
                    var pipe = h.getBlockEntity(entry.getKey(), TileEntityMagneticPipe.class);
                    stored += pipe.getFluidLevel();
                    if (pipe.getFluidLevel() > 0) h.assertTrue(pipe.getFluidType() == ReactorFluids.PLASMA.get(), "charged magnetic outlet must carry actual fusion plasma");
                }
                h.assertTrue(stored == made, "preheater and magnetic outlet must conserve every produced mB of plasma");
            }
            h.assertTrue(heater.hasMultiBlock() != incomplete, "physical preheater assembly must determine the multiblock state");
            h.succeed();
        });
    }

    private static void fusionNeutrons(GameTestHelper h) {
        BlockPos center = new BlockPos(4, 2, 4);
        List<TileEntityNeutronAbsorber> absorbers = new ArrayList<>();
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos pos = center.relative(dir);
            h.setBlock(pos, ReactorBlocks.ABSORBER.get());
            absorbers.add(h.getBlockEntity(pos, TileEntityNeutronAbsorber.class));
        }
        BlockPos absolute = h.absolutePos(center);
        h.getLevel().addFreshEntity(new EntityFusion(h.getLevel(), absolute.getX() + 0.5, absolute.getY() + 0.5, absolute.getZ() + 0.5, ""));
        h.runAfterDelay(8, () -> {
            h.assertTrue(absorbers.stream().mapToInt(TileEntityNeutronAbsorber::getTemperature).sum() == 120, "a real fusion event must emit three neutrons, each depositing 40 degrees in its absorber");
            h.succeed();
        });
    }

    private static void fusionIsotopes(GameTestHelper h) {
        BlockPos pos = new BlockPos(4, 3, 4);
        BlockHeaterMulti.layout(pos).forEach(h::setBlock);
        h.setBlock(pos, ReactorBlocks.HEATER.get());
        var heater = h.getBlockEntity(pos, TileEntityFusionHeater.class);
        var tanks = fluids(h, pos, null);
        fill(h, tanks, ReactorFluids.DEUTERIUM.get(), 100);
        fill(h, tanks, ReactorFluids.TRITIUM.get(), 49);
        heater.setTemperature(TileEntityFusionHeater.PLASMA_TEMP + 1000000);
        h.runAfterDelay(6, () -> {
            h.assertTrue(heater.hasMultiBlock() && tanks.getAmountAsInt(0) == 100 && tanks.getAmountAsInt(1) == 49 && tanks.getAmountAsInt(2) == 0,
                    "a hot assembled preheater must wait for a complete 50 mB dose of each isotope");
            h.succeed();
        });
    }

    private static void fusionBoiler(GameTestHelper h) {
        h.setBlock(MACHINE, ReactorBlocks.ABSORBER.get());
        h.setBlock(MACHINE.east(), ReactorBlocks.HEATPIPE.get());
        h.setBlock(MACHINE.east(2), ReactorBlocks.BOILER.get());
        var absorber = h.getBlockEntity(MACHINE, TileEntityNeutronAbsorber.class);
        var boiler = h.getBlockEntity(MACHINE.east(2), TileEntityReactorBoiler.class);
        boiler.addLiquid(2000, Fluids.WATER);
        for (int i = 0; i < 25; i++) absorber.onNeutron(new EntityNeutron(h.getLevel(), h.absolutePos(MACHINE), Direction.EAST, NeutronType.FUSION), h.getLevel(), h.absolutePos(MACHINE));
        for (int tick = 1; tick <= 60; tick++) h.runAfterDelay(tick, () -> {
            for (int i = 0; i < 4; i++) absorber.onNeutron(new EntityNeutron(h.getLevel(), h.absolutePos(MACHINE), Direction.EAST, NeutronType.FUSION), h.getLevel(), h.absolutePos(MACHINE));
        });
        h.runAfterDelay(65, () -> {
            h.assertTrue(boiler.getInputFluidLevel() < 2000 && boiler.getReactorType() == ReactorType.FUSION, "absorbed fusion neutrons must conduct heat through a heat pipe and boil fusion steam; water=" + boiler.getInputFluidLevel() + ", temperature=" + boiler.getTemperature() + ", type=" + boiler.getReactorType());
            h.succeed();
        });
    }

    private static void solarOutput(GameTestHelper h) {
        h.setBlock(MACHINE, ReactorBlocks.SOLAR.get());
        var exchanger = h.getBlockEntity(MACHINE, TileEntitySolarExchanger.class);
        h.assertTrue(exchanger instanceof SodiumSolarOutput, "ReactorCraft solar exchanger must implement RotaryCraft's sodium return interface");
        var solar = (SodiumSolarOutput) exchanger;
        h.assertTrue(!solar.isActive(), "unpowered sodium return must be inactive");
        power(h, MACHINE);
        h.runAfterDelay(5, () -> {
            h.assertTrue(solar.isActive(), "creative coil must activate the sodium return through real shaft power");
            h.assertTrue(solar.receiveSodium(1200) == 1000 && solar.receiveSodium(1) == 0, "sodium return must report exactly its accepted capacity");
            var out = fluids(h, MACHINE, Direction.EAST);
            var warm = FluidResource.of(ReactorFluids.WARM_SODIUM.get());
            try (Transaction tx = Transaction.openRoot()) {
                h.assertTrue(out.extract(warm, 100, tx) == 100, "horizontal pipe must extract warm sodium");
            }
            h.assertTrue(exchanger.getFluidLevel() == 1000, "aborted sodium extraction must restore its tank");
            try (Transaction tx = Transaction.openRoot()) { out.extract(warm, 100, tx); tx.commit(); }
            h.assertTrue(exchanger.getFluidLevel() == 900 && solar.receiveSodium(200) == 100, "draining must free exactly the accepted sodium capacity");
            h.assertTrue(h.getLevel().getCapability(Capabilities.Fluid.BLOCK, h.absolutePos(MACHINE), Direction.UP) == null, "solar return exposes only horizontal fluid outputs");
            h.succeed();
        });
    }

    private static BlockEntitySolarTower field(GameTestHelper h) {
        BlockPos tower = new BlockPos(3, 3, 4);
        h.setBlock(tower, RotaryBlocks.SOLAR_TOWER.get());
        for (int x = 4; x <= 8; x++) for (int z = 1; z <= 7; z++) h.setBlock(new BlockPos(x, 3, z), RotaryBlocks.MIRROR.get());
        return h.getBlockEntity(tower, BlockEntitySolarTower.class);
    }

    private static void solarTower(GameTestHelper h) {
        var tower = field(h);
        BlockPos towerPos = new BlockPos(3, 3, 4);
        h.setBlock(towerPos.above(), ReactorBlocks.SOLARTOP.get());
        h.setBlock(towerPos.above(2), ReactorBlocks.SOLARTOP.get());
        var receiver = h.getBlockEntity(towerPos.above(), TileEntitySolarTop.class);
        h.assertTrue(receiver.isActive(), "two receiver blocks above a tower must enable sodium operation");
        receiver.setTemperature(1500);
        h.setBlock(towerPos.below(), ReactorBlocks.SOLAR.get());
        var output = h.getBlockEntity(towerPos.below(), TileEntitySolarExchanger.class);
        // The exchanger needs power from below; the tower itself must still produce its own power.
        power(h, towerPos.below());
        var input = fluids(h, towerPos, Direction.EAST);
        fill(h, input, ReactorFluids.SODIUM.get(), 1000);
        h.runAfterDelay(20, () -> {
            h.assertTrue(tower.getArraySize() == 35 && tower.getCurrentPower() > 0 && tower.omega == BlockEntitySolarTower.GENOMEGA_SODIUM,
                    "real mirrors and a hot receiver must generate sodium shaft power above Y=0; mirrors=" + tower.getArraySize() + ", power=" + tower.getCurrentPower());
            h.assertTrue(output.getFluidLevel() > 0 && output.getContainedFluid() == ReactorFluids.WARM_SODIUM.get(), "generated sodium must reach the real ReactorCraft return exchanger");
            h.assertTrue(input.getAmountAsInt(0) + output.getFluidLevel() == 1000, "tower-to-exchanger sodium transfer must conserve the whole loop inventory");
            h.succeed();
        });
    }

    private static void solarMirrors(GameTestHelper h) {
        var tower = field(h);
        h.runAfterDelay(5, () -> {
            var plant = tower.getPlant();
            h.assertTrue(plant.towerCount() == 1 && plant.mirrorCount() == 35 && plant.getTowerMultiplier() == 1, "contiguous mirrors and tower must form a shared solar plant");
            var mirror = h.getBlockEntity(new BlockPos(4, 3, 4), BlockEntityMirror.class);
            h.assertTrue(mirror.getPlant() == plant && plant.getAimingPositionForMirror(mirror).equals(h.absolutePos(new BlockPos(3, 3, 4))), "mirrors must aim at their assigned tower's top");
            h.setBlock(new BlockPos(4, 3, 4), Blocks.AIR);
            h.assertTrue(tower.getPlant() == null, "removing a mirror must safely invalidate the remaining plant");
            h.succeed();
        });
    }

    private static void solarWater(GameTestHelper h) {
        var tower = field(h);
        var input = fluids(h, new BlockPos(3, 3, 4), Direction.EAST);
        fill(h, input, Fluids.WATER, 4000);
        h.runAfterDelay(5, () -> {
            h.assertTrue(tower.omega == BlockEntitySolarTower.GENOMEGA && tower.getCurrentPower() > 0 && input.getAmountAsInt(0) < 4000,
                    "a mirror field must generate ordinary water-mode power without a sodium receiver");
            h.succeed();
        });
    }
}
