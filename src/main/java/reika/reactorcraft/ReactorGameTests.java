package reika.reactorcraft;

import java.util.List;
import java.util.function.Consumer;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import reika.reactorcraft.entities.EntityNeutron;
import reika.reactorcraft.tileentities.TileEntityGasDuct;
import reika.reactorcraft.blocks.BlockReactorMachine;
import reika.reactorcraft.registry.ReactorBlocks;
import reika.reactorcraft.registry.ReactorFuel;
import reika.reactorcraft.registry.ReactorItems;
import reika.reactorcraft.registry.ReactorFluids;
import reika.reactorcraft.data.ReactorItemTagsProvider;
import reika.reactorcraft.tileentities.processing.TileEntitySynthesizer.FluidSynthesis;
import reika.reactorcraft.registry.WorkingFluid;
import reika.reactorcraft.tileentities.fission.TileEntityFuelRod;
import reika.reactorcraft.tileentities.fission.TileEntityWaterCell;
import reika.reactorcraft.tileentities.fission.TileEntityWaterCell.LiquidStates;
import reika.reactorcraft.tileentities.fission.TileEntityReactorBoiler;
import reika.reactorcraft.tileentities.powergen.TileEntitySteamLine;
import reika.reactorcraft.tileentities.powergen.TileEntitySteamGrate;
import reika.reactorcraft.tileentities.powergen.TileEntityTurbineCore;
import reika.rotarycraft.data.RoCTestStructureProvider;
import reika.rotarycraft.base.blocks.BlockRotaryCraftMachine;
import reika.rotarycraft.blockentities.transmission.BlockEntityGearbox;
import reika.rotarycraft.modinterface.conversion.TileEntityDynamo;
import reika.rotarycraft.registry.RotaryBlocks;
import reika.rotarycraft.blockentities.storage.BlockEntityReservoir;

/** In-world processing, steam and reactor checks; all generating types are covered by ReactorTypeGameTests. */
public final class ReactorGameTests {

    private ReactorGameTests() {}

    public static final DeferredRegister<MapCodec<? extends GameTestInstance>> TEST_INSTANCE_TYPES =
            DeferredRegister.create(Registries.TEST_INSTANCE_TYPE, ReactorCraft.MODID);

    static {
        TEST_INSTANCE_TYPES.register("direct", () -> DirectInstance.CODEC);
    }

    public static void onRegisterGameTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
                Identifier.fromNamespaceAndPath(ReactorCraft.MODID, "default"),
                new TestEnvironmentDefinition.AllOf(List.of()));
        register(event, environment, "audit_inventory_sides_and_reload", 40, ReactorGameTests::auditInventorySidesAndReload);
        register(event, environment, "audit_legacy_item_components", 40, ReactorGameTests::auditLegacyItemComponents);
        register(event, environment, "steam_line_holds_back_water", 40,
                ReactorGameTests::steamLineHoldsBackWater);
        register(event, environment, "uranium_fuel_crafting_and_burnup", 80,
                ReactorGameTests::uraniumFuelCraftingAndBurnup);
        register(event, environment, "fuel_rod_fissions_and_boils_water", 220,
                ReactorGameTests::fuelRodFissionsAndBoilsWater);
        register(event, environment, "boiler_steam_spins_turbine", 140,
                ReactorGameTests::boilerSteamSpinsTurbine);
        register(event, environment, "electrolyzer_fluid_capability_transactions", 40,
                ReactorGameTests::electrolyzerFluidCapabilityTransactions);
        register(event, environment, "water_cell_draws_reservoir_capability", 60,
                ReactorGameTests::waterCellDrawsReservoirCapability);
        register(event, environment, "gas_duct_fills_rotary_reservoir", 80,
                ReactorGameTests::gasDuctFillsRotaryReservoir);
        register(event, environment, "ammonia_synthesis_ingredients", 40,
                ReactorGameTests::ammoniaSynthesisIngredients);
        ReactorTypeGameTests.register(event, environment);
        ReactorContentGameTests.register(event, environment);
    }

    private static void auditInventorySidesAndReload(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        helper.setBlock(pos, ReactorBlocks.FUEL.get());
        TileEntityFuelRod rod = helper.getBlockEntity(pos, TileEntityFuelRod.class);
        var level = helper.getLevel();
        var above = level.getCapability(Capabilities.Item.BLOCK, helper.absolutePos(pos), Direction.UP);
        var below = level.getCapability(Capabilities.Item.BLOCK, helper.absolutePos(pos), Direction.DOWN);
        var side = level.getCapability(Capabilities.Item.BLOCK, helper.absolutePos(pos), Direction.EAST);
        var fuel = net.neoforged.neoforge.transfer.item.ItemResource.of(ReactorItems.FUEL_ROD.toStack());
        try (var transaction = Transaction.openRoot()) {
            helper.assertTrue(above.insert(net.neoforged.neoforge.transfer.item.ItemResource.of(net.minecraft.world.item.Items.COAL), 1, transaction) == 0, "invalid items cannot enter a fuel core");
            helper.assertTrue(side.insert(fuel, 1, transaction) == 0, "fuel cannot enter the side of a fuel core");
            helper.assertTrue(above.insert(fuel, 1, transaction) == 1, "valid fuel must enter from above");
            transaction.commit();
        }
        try (var transaction = Transaction.openRoot()) {
            helper.assertTrue(below.extract(fuel, 1, transaction) == 0, "unspent fuel must not be extracted");
        }
        rod.setItem(0, ReactorItems.DEPLETED_FUEL.toStack());
        var depleted = net.neoforged.neoforge.transfer.item.ItemResource.of(ReactorItems.DEPLETED_FUEL.toStack());
        try (var transaction = Transaction.openRoot()) {
            helper.assertTrue(above.extract(depleted, 1, transaction) == 0, "depleted fuel cannot leave through the input face");
            helper.assertTrue(below.extract(depleted, 1, transaction) == 1, "depleted fuel must leave through the output face");
        }
        helper.assertTrue(!rod.getItem(0).isEmpty(), "aborted extraction must restore inventory");
        var handler = rod.getItemHandler();
        var tag = rod.saveWithoutMetadata(level.registryAccess());
        rod.clearContent();
        rod.loadWithComponents(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING, level.registryAccess(), tag));
        helper.assertTrue(handler == rod.getItemHandler() && rod.getItem(0).is(ReactorItems.DEPLETED_FUEL.get()), "load must restore contents without replacing cached handlers");
        try (var transaction = Transaction.openRoot()) {
            helper.assertTrue(below.extract(depleted, 1, transaction) == 1, "previously bound capability must still see restored contents");
            transaction.commit();
        }
        helper.assertTrue(rod.getItem(0).isEmpty(), "committed extraction must remove the item");
        helper.succeed();
    }

    private static void auditLegacyItemComponents(GameTestHelper helper) {
        ItemStack fuel = ReactorItems.FUEL_ROD.toStack();
        fuel.set(net.minecraft.core.component.DataComponents.DAMAGE, 17);
        reika.dragonapi.interfaces.LegacyItemData.migrate(fuel);
        helper.assertTrue(fuel.getDamageValue() == 17 && !fuel.has(net.minecraft.core.component.DataComponents.DAMAGE)
                && fuel.getOrDefault(reika.reactorcraft.registry.ReactorDataComponents.FUEL_BURNUP.get(), -1) == 17, "legacy burnup must migrate without losing its value");
        ItemStack magnet = ReactorItems.MAGNET_ITEM.toStack();
        magnet.set(net.minecraft.core.component.DataComponents.DAMAGE, 5);
        reika.dragonapi.interfaces.LegacyItemData.migrate(magnet);
        helper.assertTrue(magnet.getDamageValue() == 5 && !magnet.has(net.minecraft.core.component.DataComponents.DAMAGE), "legacy magnet charge must migrate");
        ItemStack waste = ReactorItems.WASTE_ITEM.toStack();
        waste.set(net.minecraft.core.component.DataComponents.DAMAGE, 1000);
        reika.dragonapi.interfaces.LegacyItemData.migrate(waste);
        helper.assertTrue(waste.getDamageValue() == 1000 && reika.reactorcraft.items.ItemNuclearWaste.identity(1000).equals(waste.get(reika.reactorcraft.registry.ReactorDataComponents.WASTE_IDENTITY.get())), "legacy waste must gain a stable named identity");
        BlockPos pos = new BlockPos(3, 1, 3);
        helper.setBlock(pos, RotaryBlocks.VACUUM.get());
        var vacuum = helper.getBlockEntity(pos, reika.rotarycraft.blockentities.BlockEntityVacuum.class);
        var inventory = vacuum.getItemHandler();
        ItemStack oldFuel = ReactorItems.FUEL_ROD.toStack();
        oldFuel.set(net.minecraft.core.component.DataComponents.DAMAGE, 23);
        inventory.setStackInSlot(0, oldFuel);
        var saved = vacuum.saveWithoutMetadata(helper.getLevel().registryAccess());
        inventory.setStackInSlot(0, ItemStack.EMPTY);
        vacuum.loadWithComponents(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), saved));
        helper.assertTrue(inventory == vacuum.getItemHandler()
                && inventory.getStackInSlot(0).getOrDefault(reika.reactorcraft.registry.ReactorDataComponents.FUEL_BURNUP.get(), -1) == 23
                && !inventory.getStackInSlot(0).has(net.minecraft.core.component.DataComponents.DAMAGE), "Rotary inventory loads must also migrate Reactor items");
        helper.succeed();
    }

    private static void ammoniaSynthesisIngredients(GameTestHelper helper) {
        var ammonia = FluidSynthesis.AMMONIA;
        helper.assertTrue(FluidSynthesis.list.contains(ammonia)
                        && ammonia.input == Fluids.WATER
                        && ammonia.output == ReactorFluids.AMMONIA.get(),
                "the ammonia fluid synthesis recipe must initialize and remain registered");
        helper.assertTrue(ReactorItems.LIME.toStack().is(ReactorItemTagsProvider.QUICKLIME_DUST)
                        && ReactorItems.AMMONIUM_DUST.toStack().is(ReactorItemTagsProvider.AMMONIUM_DUST),
                "ReactorCraft dusts must publish the common tags used by ammonia synthesis");
        helper.assertTrue(ammonia.usesItem(ReactorItems.LIME.toStack())
                        && ammonia.usesItem(ReactorItems.AMMONIUM_DUST.toStack())
                        && !ammonia.getAForDisplay().isEmpty()
                        && !ammonia.getBForDisplay().isEmpty(),
                "both legacy ore alternatives and their fallback items must be usable and displayable");
        helper.succeed();
    }

    /**
     * 1.7.10 made every ReactorCraft line and machine block {@code Material.iron}, which blocks movement, so flowing water never
     * replaced it. 26.3 decides that from block tags (see {@code LegacyMotionTags}); this pins the tags
     * and the behaviour they produce.
     */
    private static void steamLineHoldsBackWater(GameTestHelper helper) {
        BlockPos target = new BlockPos(3, 1, 3);
        helper.setBlock(target, ReactorBlocks.STEAMLINE.get());
        var state = helper.getLevel().getBlockState(helper.absolutePos(target));
        helper.assertTrue(state.is(net.minecraft.tags.BlockTags.BLOCKS_MOTION)
                        && !state.is(net.minecraft.tags.BlockTags.WASHED_AWAY_BY_FLUIDS),
                "steam line must block motion and not be washed away by fluids");
        helper.setBlock(target.west(), net.minecraft.world.level.block.Blocks.WATER);
        helper.runAfterDelay(30, () -> {
            helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(target)).is(ReactorBlocks.STEAMLINE.get()),
                    "flowing water must not wash away a steam line, found "
                            + helper.getLevel().getBlockState(helper.absolutePos(target)));
            helper.succeed();
        });
    }

    private static void gasDuctFillsRotaryReservoir(GameTestHelper helper) {
        BlockPos ductPos = new BlockPos(3, 1, 4);
        BlockPos reservoirPos = ductPos.east();
        helper.setBlock(ductPos, ReactorBlocks.GASPIPE.get());
        helper.setBlock(reservoirPos, RotaryBlocks.RESERVOIR.get());
        BlockEntityReservoir reservoir = helper.getBlockEntity(reservoirPos, BlockEntityReservoir.class);
        reservoir.isCovered = true;
        TileEntityGasDuct gasDuct = helper.getBlockEntity(ductPos, TileEntityGasDuct.class);
        var level = helper.getLevel();
        var duct = level.getCapability(Capabilities.Fluid.BLOCK, helper.absolutePos(ductPos), Direction.UP);
        var reservoirInput = level.getCapability(Capabilities.Fluid.BLOCK,
                helper.absolutePos(reservoirPos), Direction.WEST);
        helper.assertTrue(duct != null && reservoirInput != null,
                "gas duct and RotaryCraft reservoir must expose fluid capabilities");
        FluidResource deuterium = FluidResource.of(ReactorFluids.DEUTERIUM.get());
        try (Transaction transaction = Transaction.openRoot()) {
            helper.assertTrue(duct.insert(deuterium, 1000, transaction) == 1000,
                    "gas duct must accept deuterium");
            transaction.commit();
        }
        helper.startSequence().thenIdle(40).thenExecute(() -> {
            helper.assertTrue(reservoir.getFluidLevel() > 0,
                    "gas duct did not move deuterium into the RotaryCraft reservoir: duct="
                            + duct.getAmountAsInt(0) + ", connected=" + gasDuct.isConnectedDirectly(Direction.EAST)
                            + ", reservoir=" + reservoir.getFluidLevel());
            helper.assertTrue(duct.getAmountAsInt(0) + reservoirInput.getAmountAsInt(0) == 1000,
                    "gas-duct-to-reservoir transfer must conserve deuterium");
        }).thenSucceed();
    }

    private static void waterCellDrawsReservoirCapability(GameTestHelper helper) {
        BlockPos cellPos = new BlockPos(3, 1, 4);
        BlockPos reservoirPos = cellPos.above();
        helper.setBlock(cellPos, ReactorBlocks.COOLANT.get());
        helper.setBlock(reservoirPos, RotaryBlocks.RESERVOIR.get());
        TileEntityWaterCell cell = helper.getBlockEntity(cellPos, TileEntityWaterCell.class);
        var reservoirInput = helper.getLevel().getCapability(Capabilities.Fluid.BLOCK,
                helper.absolutePos(reservoirPos), Direction.NORTH);
        helper.assertTrue(reservoirInput != null, "reservoir must expose its horizontal input");
        try (Transaction tx = Transaction.openRoot()) {
            helper.assertTrue(reservoirInput.insert(FluidResource.of(Fluids.WATER), 1000, tx) == 1000,
                    "reservoir must accept one water bucket");
            tx.commit();
        }
        helper.startSequence().thenIdle(5).thenExecute(() -> {
            helper.assertTrue(cell.getLiquidState() == LiquidStates.WATER,
                    "water cell did not draw water from the reservoir's bottom capability");
            helper.assertTrue(reservoirInput.getAmountAsInt(0) == 0,
                    "water cell must debit exactly one bucket from the reservoir");
        }).thenSucceed();
    }

    private static void electrolyzerFluidCapabilityTransactions(GameTestHelper helper) {
        BlockPos relative = new BlockPos(3, 1, 4);
        helper.setBlock(relative, ReactorBlocks.ELECTROLYZER.get());
        BlockPos pos = helper.absolutePos(relative);
        var level = helper.getLevel();
        var input = level.getCapability(Capabilities.Fluid.BLOCK, pos, Direction.NORTH);
        var upper = level.getCapability(Capabilities.Fluid.BLOCK, pos, Direction.UP);
        var lower = level.getCapability(Capabilities.Fluid.BLOCK, pos, Direction.DOWN);
        helper.assertTrue(input != null && upper != null && lower != null,
                "electrolyzer must expose horizontal input and vertical outputs");
        FluidResource heavyWater = FluidResource.of(ReactorFluids.getLegacyFluid("rc heavy water"));
        try (Transaction tx = Transaction.openRoot()) {
            helper.assertTrue(input.insert(heavyWater, 200, tx) == 200,
                    "horizontal input must accept heavy water");
        }
        helper.assertTrue(input.getAmountAsInt(2) == 0,
                "aborted insertion must restore the input tank");
        try (Transaction tx = Transaction.openRoot()) {
            helper.assertTrue(input.insert(heavyWater, 200, tx) == 200,
                    "horizontal input must accept heavy water on commit");
            tx.commit();
        }
        helper.assertTrue(input.getAmountAsInt(2) == 200,
                "committed insertion must be stored in input tank 2");
        try (Transaction tx = Transaction.openRoot()) {
            helper.assertTrue(upper.insert(heavyWater, 100, tx) == 0
                            && lower.insert(heavyWater, 100, tx) == 0
                            && input.extract(heavyWater, 100, tx) == 0,
                    "output faces must reject insertion and input face must reject extraction");
        }
        helper.succeed();
    }

    private static void uraniumFuelCraftingAndBurnup(GameTestHelper helper) {
        ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE,
                Identifier.fromNamespaceAndPath(ReactorCraft.MODID, "fuel"));
        Recipe<?> loaded = helper.getLevel().getServer().getRecipeManager().byKey(key).orElseThrow().value();
        helper.assertTrue(loaded instanceof CraftingRecipe, "enriched fuel rod recipe must be craftable");
        CraftingRecipe recipe = (CraftingRecipe) loaded;
        CraftingInput grid = CraftingInput.of(2, 2, List.of(
                ReactorItems.FUEL_DUST.toStack(), ReactorItems.FUEL_DUST.toStack(),
                ReactorItems.FUEL_DUST.toStack(), ReactorItems.FUEL_DUST.toStack()));
        helper.assertTrue(recipe.matches(grid, helper.getLevel()),
                "four centrifuged fuel dusts did not match the fuel rod recipe");
        ItemStack rods = recipe.assemble(grid);
        helper.assertTrue(rods.is(ReactorItems.FUEL_ROD.get()) && rods.getCount() == 2,
                "fuel dusts must craft two fresh uranium rods, got " + rods);

        ItemStack next = ReactorFuel.URANIUM.getFissionProduct(rods);
        helper.assertTrue(next.is(ReactorItems.FUEL_ROD.get()) && next.getDamageValue() == 1,
                "uranium rod did not advance one burnup stage");
        ItemStack spent = ReactorFuel.URANIUM.getFissionProduct(ReactorItems.FUEL.getStackOfMetadata(99));
        helper.assertTrue(spent.is(ReactorItems.DEPLETED_FUEL.get()),
                "uranium rod at the last stage did not become depleted fuel");
        ItemStack plutoniumEnd = ReactorFuel.PLUTONIUM.getFissionProduct(
                ReactorItems.PLUTONIUM.getStackOfMetadata(99));
        helper.assertTrue(plutoniumEnd.isEmpty(),
                "fully burnt plutonium must leave the fuel slot instead of looping at stage 99");
        helper.succeed();
    }

    private static void fuelRodFissionsAndBoilsWater(GameTestHelper helper) {
        BlockPos fuelPos = new BlockPos(3, 1, 4);
        BlockPos boilerPos = fuelPos.east();
        helper.setBlock(fuelPos, ReactorBlocks.FUEL.get());
        helper.setBlock(boilerPos, ReactorBlocks.BOILER.get());
        TileEntityFuelRod rod = helper.getBlockEntity(fuelPos, TileEntityFuelRod.class);
        TileEntityReactorBoiler boiler = helper.getBlockEntity(boilerPos, TileEntityReactorBoiler.class);
        rod.setItem(0, ReactorItems.FUEL.getStackOf());
        boiler.addLiquid(2000, Fluids.WATER);

        helper.startSequence().thenIdle(8).thenExecute(() -> {
            helper.assertTrue(rod.isFissile(), "fresh uranium fuel did not reach the rod's active slot");
            int initial = rod.getTemperature();
            EntityNeutron neutron = new EntityNeutron(helper.getLevel(), helper.absolutePos(fuelPos),
                    Direction.NORTH, EntityNeutron.NeutronType.FISSION);
            for (int attempts = 0; attempts < 300 && rod.getTemperature() < 350; attempts++)
                rod.onNeutron(neutron, helper.getLevel(), helper.absolutePos(fuelPos));
            helper.assertTrue(rod.getTemperature() >= 350 && rod.getTemperature() > initial,
                    "fuel rod did not fission and gain heat from fission neutrons: temperature="
                            + rod.getTemperature());
        }).thenIdle(120).thenExecute(() -> {
            helper.assertTrue(boiler.getSteam() > 0 && boiler.getInputFluidLevel() < 2000,
                    "adjacent fission boiler did not turn water into steam: steam="
                            + boiler.getSteam() + ", water=" + boiler.getInputFluidLevel()
                            + ", temperature=" + boiler.getTemperature());
        }).thenSucceed();
    }

    private static void boilerSteamSpinsTurbine(GameTestHelper helper) {
        BlockPos boilerPos = new BlockPos(3, 1, 4);
        BlockPos linePos = boilerPos.above();
        BlockPos gratePos = linePos.east();
        BlockPos turbinePos = gratePos.above(2);
        BlockPos gearboxPos = turbinePos.east();
        BlockPos dynamoPos = gearboxPos.east();
        helper.setBlock(boilerPos, ReactorBlocks.BOILER.get());
        helper.setBlock(linePos, ReactorBlocks.STEAMLINE.get());
        helper.setBlock(gratePos, ReactorBlocks.GRATE.get());
        helper.setBlock(turbinePos, ReactorBlocks.TURBINECORE.get().defaultBlockState()
                .setValue(BlockReactorMachine.FACING, Direction.EAST));
        helper.setBlock(gearboxPos, RotaryBlocks.BEDROCK_GEARBOX_2x.get().defaultBlockState()
                .setValue(BlockRotaryCraftMachine.FACING, Direction.EAST));
        helper.setBlock(dynamoPos, RotaryBlocks.ROTATIONAL_DYNAMO.get().defaultBlockState()
                .setValue(BlockRotaryCraftMachine.FACING, Direction.EAST));
        TileEntityReactorBoiler boiler = helper.getBlockEntity(boilerPos, TileEntityReactorBoiler.class);
        TileEntitySteamLine line = helper.getBlockEntity(linePos, TileEntitySteamLine.class);
        TileEntitySteamGrate grate = helper.getBlockEntity(gratePos, TileEntitySteamGrate.class);
        TileEntityTurbineCore turbine = helper.getBlockEntity(turbinePos, TileEntityTurbineCore.class);
        BlockEntityGearbox gearbox = helper.getBlockEntity(gearboxPos, BlockEntityGearbox.class);
        TileEntityDynamo dynamo = helper.getBlockEntity(dynamoPos, TileEntityDynamo.class);
        boiler.addLiquid(8000, Fluids.WATER);
        boiler.setTemperature(500); // isolate steam transport from the fission heat test above
        turbine.addLubricant(5000);
        helper.startSequence().thenIdle(60).thenExecute(() -> {
            helper.assertTrue(boiler.getInputFluidLevel() < 8000,
                    "heated boiler did not consume water");
            helper.assertTrue(line.getWorkingFluid() == WorkingFluid.WATER || grate.getSteam() > 0
                            || turbine.getRenderOmega() > 0,
                    "boiler steam did not enter the steam line and grate");
            helper.assertTrue(turbine.getOmega() > 0 && turbine.getPower() > 0,
                    "steam grate did not spin a lubricated turbine: omega=" + turbine.getRenderOmega()
                            + ", torque=" + turbine.getTorque()
                            + ", line=" + line.getSteam() + ", grate=" + grate.getSteam());
            helper.assertTrue(gearbox.power > 0 && gearbox.omega == turbine.getOmega() / 2,
                    "RotaryCraft gearbox did not accept turbine shaft output: gearbox omega="
                            + gearbox.omega + ", power=" + gearbox.power + ", turbine omega="
                            + turbine.getOmega() + ", power=" + turbine.getPower());
            helper.assertTrue(dynamo.power > 0 && dynamo.getGeneratedUnitsPerTick() > 0,
                    "Rotational Dynamo did not convert steam turbine shaft power: shaft="
                            + dynamo.power + ", RF/t=" + dynamo.getGeneratedUnitsPerTick());
        }).thenSucceed();
    }

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment,
                                 String name, int maxTicks, Consumer<GameTestHelper> body) {
        TestData<Holder<TestEnvironmentDefinition<?>>> data =
                new TestData<>(environment, RoCTestStructureProvider.ARENA, maxTicks, 0, true, Rotation.NONE);
        event.registerTest(Identifier.fromNamespaceAndPath(ReactorCraft.MODID, name),
                new DirectInstance(data, body));
    }

    private static final class DirectInstance extends GameTestInstance {
        static final MapCodec<DirectInstance> CODEC =
                TestData.CODEC.xmap(data -> new DirectInstance(data, helper -> {}), instance -> instance.info);

        private final TestData<Holder<TestEnvironmentDefinition<?>>> info;
        private final Consumer<GameTestHelper> body;

        DirectInstance(TestData<Holder<TestEnvironmentDefinition<?>>> info, Consumer<GameTestHelper> body) {
            super(info);
            this.info = info;
            this.body = body;
        }

        @Override public void run(GameTestHelper helper) { body.accept(helper); }
        @Override public MapCodec<? extends GameTestInstance> codec() { return CODEC; }
        @Override protected MutableComponent typeDescription() { return Component.literal("reactorcraft direct test"); }
    }
}
