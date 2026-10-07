package reika.reactorcraft.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.advancements.Advancement;
import net.minecraft.core.registries.MultiRegistryBootstrap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;

import reika.dragonapi.libraries.mathsci.Isotopes.ElementGroup;
import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.auxiliary.WasteManager;
import reika.reactorcraft.auxiliary.WasteManager.WasteChance;
import reika.reactorcraft.registry.CraftingItems;
import reika.reactorcraft.registry.FluoriteTypes;
import reika.reactorcraft.blocks.multi.BlockHeaterMulti;
import reika.reactorcraft.blocks.multi.BlockInjectorMulti;
import reika.reactorcraft.registry.MatBlocks;
import reika.reactorcraft.registry.ReactorBlocks;
import reika.reactorcraft.registry.ReactorItems;
import reika.reactorcraft.registry.ReactorFluids;
import reika.reactorcraft.registry.ReactorOreType;
import reika.rotarycraft.auxiliary.recipemanagers.CentrifugeRecipe;
import reika.rotarycraft.auxiliary.recipemanagers.CentrifugeRecipe.ChancedOutput;
import reika.rotarycraft.auxiliary.recipemanagers.CompactorRecipe;
import reika.rotarycraft.auxiliary.recipemanagers.CrystallizerRecipe;
import reika.rotarycraft.auxiliary.recipemanagers.FrictionHeaterRecipe;
import reika.rotarycraft.auxiliary.recipemanagers.GrinderRecipe;
import reika.rotarycraft.auxiliary.recipemanagers.PulseFurnaceRecipe;
import reika.rotarycraft.auxiliary.recipemanagers.ShapelessBlastFurnaceRecipe;
import reika.rotarycraft.registry.RotaryBlocks;
import reika.rotarycraft.registry.RotaryItems;

/**
 * Recipe datagen for the ore→fuel slice, transcribed FAITHFULLY from the 1.7.10
 * {@code ReactorRecipes} (smelting/crafting) and the processor/centrifuge chemistry enums:
 * <ul>
 *   <li>Smelting: every ore block → its product (per-ore XP), every fluorite ore → its gem,
 *       calcite → lime. ({@code ReactorRecipes.addSmelting})</li>
 *   <li>Crafting: tank, canister part, control rod, empty fluid canister, and the 2×2
 *       fuel-dust / depleted-dust compaction. ({@code addCrafting} / {@code addItems})</li>
 *   <li>Processor / centrifuge: the {@code ProcessorRecipe.UF6} and {@code CentrifugeRecipe.UF6}
 *       custom recipes, written as JSON so the values live in data not a hardcoded enum.</li>
 *   <li>Cross-mod (RotaryCraft recipe types, emitted under the reactorcraft namespace because
 *       RotaryCraft has no dependency on ReactorCraft): Cd+In+Ag alloy in the blast furnace,
 *       coal-dust→graphite in the friction heater, uranium/emerald grinding.
 *       ({@code ReactorRecipes.addRCInterface} / {@code addCrafting})</li>
 * </ul>
 * The processor/centrifuge machine block crafting recipes are deferred with their (not-yet-ported)
 * {@code BlockReactorTile} blocks — those result items don't exist yet.
 */
public final class ReactorRecipeProvider {

    private ReactorRecipeProvider() {
    }

    public static MultiRegistryBootstrap bootstrap() {
        return RecipeProvider.asBootstrap(Recipes::new);
    }

    private static final class Recipes extends RecipeProvider {
        private final RecipeOutput out;

        Recipes(BootstrapContext<Recipe<?>> recipes, BootstrapContext<Advancement> advancements) {
            super(recipes, advancements);
            this.out = this.output;
        }

        @Override
        protected void buildRecipes() {
            smelting();
            crafting();
            machineCrafting();
            multiblockCrafting();
            crossMod();
        }

        // The fusion preheater / plasma injector / solenoid casing part recipes, transcribed from the
        // original addMultiblocks(). "insulCore" upstream is the thermal insulation core block itself.
        private void multiblockCrafting() {
            var core = ReactorBlocks.HEATER_CASINGS.get(BlockHeaterMulti.HeaterPart.CORE).get();
            var steel = RotaryItems.HSLA_STEEL_INGOT.get();

            shaped(RecipeCategory.MISC, ReactorBlocks.HEATER_CASINGS.get(BlockHeaterMulti.HeaterPart.LENS).get())
                    .define('B', RotaryBlocks.BLASTGLASS.get()).define('S', RotaryItems.TUNGSTEN_INGOT.get()).define('L', RotaryItems.LENS.get())
                    .pattern("SBS").pattern("BLB").pattern("SBS")
                    .unlockedBy("has_lens", has(RotaryItems.LENS.get())).save(out);
            shaped(RecipeCategory.MISC, core)
                    .define('W', ItemTags.WOOL).define('S', steel)
                    .pattern("WWW").pattern("WSW").pattern("WWW")
                    .unlockedBy("has_steel", has(steel)).save(out);
            shaped(RecipeCategory.MISC, ReactorBlocks.HEATER_CASINGS.get(BlockHeaterMulti.HeaterPart.CORNER).get())
                    .define('O', core).define('S', steel)
                    .pattern("SOS").pattern("OSO").pattern("SOS")
                    .unlockedBy("has_core", has(core)).save(out);
            shaped(RecipeCategory.MISC, ReactorBlocks.HEATER_CASINGS.get(BlockHeaterMulti.HeaterPart.EDGE).get())
                    .define('O', core).define('S', steel)
                    .pattern("OSO").pattern("SSS").pattern("OSO")
                    .unlockedBy("has_core", has(core)).save(out);
            shaped(RecipeCategory.MISC, ReactorBlocks.HEATER_CASINGS.get(BlockHeaterMulti.HeaterPart.FACE).get())
                    .define('O', core).define('S', steel)
                    .pattern("SSS").pattern("SOS").pattern("SSS")
                    .unlockedBy("has_core", has(core)).save(out);

            injectorPart(BlockInjectorMulti.InjectorPart.BASE, core, "WWW", "HHH", "MMM", false);
            injectorPart(BlockInjectorMulti.InjectorPart.LOWER_CORNER, core, "MWM", "MHM", "MMM", false);
            injectorPart(BlockInjectorMulti.InjectorPart.SIDE_PANEL, core, "WMW", "MHM", "WMW", false);
            injectorPart(BlockInjectorMulti.InjectorPart.TOP, core, "MMM", "HHH", "WWW", false);
            injectorPart(BlockInjectorMulti.InjectorPart.UPPER_CORNER, core, "MMM", "MHM", "MWM", false);
            injectorPart(BlockInjectorMulti.InjectorPart.INDUCTION_COIL, core, "MWM", "HHH", "MWM", true);
            injectorPart(BlockInjectorMulti.InjectorPart.COLUMN, core, "MWM", "MHM", "MWM", false);
            injectorPart(BlockInjectorMulti.InjectorPart.HYSTERESIS_CORE, core, "HWH", "WMW", "HWH", false);

            solenoidPart(ReactorBlocks.FERROMAGNETIC_BASE.get(), b -> b
                    .define('S', ReactorItems.crafting(CraftingItems.FERROINGOT))
                    .pattern("SSS").pattern("SSS").pattern("SSS"));
            solenoidPart(ReactorBlocks.MAGNETIC_LINKAGE.get(), b -> b
                    .define('S', ReactorItems.crafting(CraftingItems.FERROINGOT)).define('B', RotaryItems.HSLA_STEEL_INGOT.get())
                    .pattern("SSS").pattern("SBS").pattern("SSS"));
            // Central (fully-charged magnet) and auxiliary (one-below-max magnet) permanent-magnet
            // casings differ only by the magnet's damage-value charge (legacy ReactorRecipes: central =
            // maxMagnet = charge 7, auxiliary = weakerMagnet = charge 6). Plain Ingredient is charge-blind,
            // so match the DAMAGE component with a NeoForge DataComponentIngredient (partial match on the
            // exact charge). Both feed off the compactor-charged magnet (see magnetCompactor()).
            int maxCharge = ReactorItems.MAGNET.getNumberMetadatas() - 1;
            solenoidPart(ReactorBlocks.CENTRAL_MAGNET.get(), b -> b
                    .define('M', DataComponentIngredient.of(false, reika.reactorcraft.registry.ReactorDataComponents.MAGNET_CHARGE.get(), maxCharge, ReactorItems.MAGNET_ITEM.get()))
                    .define('S', ReactorItems.crafting(CraftingItems.MAGNETIC))
                    .pattern("SSS").pattern("MMM").pattern("SSS"));
            solenoidPart(ReactorBlocks.AUXILIARY_MAGNET.get(), b -> b
                    .define('M', DataComponentIngredient.of(false, reika.reactorcraft.registry.ReactorDataComponents.MAGNET_CHARGE.get(), maxCharge - 1, ReactorItems.MAGNET_ITEM.get()))
                    .define('S', ReactorItems.crafting(CraftingItems.MAGNETIC))
                    .pattern("SSS").pattern("MMM").pattern("SSS"));
            solenoidPart(ReactorBlocks.HYSTERESIS_ROD.get(), b -> b
                    .define('M', ReactorItems.crafting(CraftingItems.FERROINGOT)).define('S', ReactorItems.crafting(CraftingItems.HYSTERESIS))
                    .pattern("SSS").pattern("MMM").pattern("SSS"));
            solenoidPart(ReactorBlocks.SOLENOID_HUB.get(), b -> b
                    .define('W', ReactorItems.crafting(CraftingItems.WIRE)).define('P', ReactorItems.crafting(CraftingItems.MAGNETIC))
                    .define('S', ReactorItems.crafting(CraftingItems.FERROINGOT))
                    .pattern("SSS").pattern("WPW").pattern("SSS"));
        }

        private void injectorPart(BlockInjectorMulti.InjectorPart part, ItemLike core, String r1, String r2, String r3, boolean wireH) {
            var b = shaped(RecipeCategory.MISC, ReactorBlocks.INJECTOR_CASINGS.get(part).get())
                    .define('H', wireH ? ReactorItems.crafting(CraftingItems.WIRE) : ReactorItems.crafting(CraftingItems.HYSTERESIS))
                    .define('M', ReactorItems.crafting(CraftingItems.MAGNETIC))
                    .define('W', core)
                    .pattern(r1).pattern(r2).pattern(r3);
            b.unlockedBy("has_magnetic", has(ReactorItems.crafting(CraftingItems.MAGNETIC))).save(out);
        }

        private void solenoidPart(ItemLike result, java.util.function.UnaryOperator<net.minecraft.data.recipes.ShapedRecipeBuilder> shape) {
            shape.apply(shaped(RecipeCategory.MISC, result))
                    .unlockedBy("has_ferro", has(ReactorItems.crafting(CraftingItems.FERROINGOT))).save(out);
        }

        // The original addMachines() recipes. The old "gated on unported RotaryCraft items" list is
        // resolved — every legacy ingredient now exists under a port name: basepanel→HSLA_PLATE,
        // pipe→FLUID_PIPE block, prop→PROPELLER_BLADE, shaftitem→HSLA_SHAFT, pcb→CIRCUIT_BOARD,
        // gearunit→HSLA_STEEL_GEAR_2x, silumin→ALUMINUM_ALLOY_INGOT, bedingot→BEDROCK_ALLOY_INGOT,
        // igniter→IGNITION_UNIT, cooling fin→RotaryBlocks.COOLING_FIN, gearbox parts→*_GEAR_16x /
        // *_SHAFT_CORE. Pipe output counts were DifficultyEffects.PIPECRAFT.getInt() upstream; fixed 8.
        private void machineCrafting() {
            shaped(RecipeCategory.MISC, ReactorBlocks.FUEL.get())
                    .define('S', RotaryItems.HSLA_STEEL_INGOT.get()).define('H', Items.HOPPER)
                    .define('P', RotaryItems.HSLA_PLATE.get()).define('C', ReactorItems.crafting(CraftingItems.CANISTER))
                    .pattern("SHS").pattern("PCP").pattern("SCS")
                    .unlockedBy("has_canister_part", has(ReactorItems.crafting(CraftingItems.CANISTER))).save(out);

            shaped(RecipeCategory.MISC, ReactorBlocks.CONTROL.get())
                    .define('S', RotaryItems.HSLA_STEEL_INGOT.get()).define('G', RotaryItems.HSLA_STEEL_GEAR_2x.get())
                    .define('R', ReactorItems.crafting(CraftingItems.ROD)).define('P', RotaryItems.HSLA_PLATE.get())
                    .pattern("SGS").pattern("RRR").pattern("PPP")
                    .unlockedBy("has_rod", has(ReactorItems.crafting(CraftingItems.ROD)))
                    // the CraftingItems.ROD component recipe already owns "reactorcraft:control_rod"
                    .save(out, ReactorCraft.MODID + ":control_rod_block");

            shaped(RecipeCategory.MISC, ReactorBlocks.COOLANT.get())
                    .define('S', RotaryItems.HSLA_STEEL_INGOT.get()).define('P', RotaryBlocks.FLUID_PIPE.get())
                    .define('G', Items.GLASS).define('R', RotaryBlocks.RESERVOIR.get())
                    .pattern("SPS").pattern("GRG").pattern("SPS")
                    .unlockedBy("has_reservoir", has(RotaryBlocks.RESERVOIR.get())).save(out);

            shaped(RecipeCategory.MISC, ReactorBlocks.TURBINECORE.get())
                    .define('B', RotaryItems.PROPELLER_BLADE.get()).define('C', ReactorItems.crafting(CraftingItems.TURBCORE))
                    .pattern("BBB").pattern("BCB").pattern("BBB")
                    .unlockedBy("has_turbine_core_part", has(ReactorItems.crafting(CraftingItems.TURBCORE))).save(out);

            shaped(RecipeCategory.MISC, ReactorBlocks.STEAMLINE.get(), 3)
                    .define('N', ItemTags.WOOL).define('P', RotaryBlocks.FLUID_PIPE.get())
                    .pattern("NPN").pattern("NPN").pattern("NPN")
                    .unlockedBy("has_pipe", has(RotaryBlocks.FLUID_PIPE.get())).save(out);

            shaped(RecipeCategory.MISC, ReactorBlocks.FLUIDEXTRACTOR.get())
                    .define('P', RotaryItems.HSLA_PLATE.get()).define('p', RotaryBlocks.FLUID_PIPE.get())
                    .define('G', Items.GLASS).define('I', RotaryItems.IMPELLER.get())
                    .define('S', RotaryItems.HSLA_SHAFT.get())
                    .pattern("PpP").pattern("GIG").pattern("PSP")
                    .unlockedBy("has_impeller", has(RotaryItems.IMPELLER.get())).save(out);

            shaped(RecipeCategory.MISC, ReactorBlocks.CENTRIFUGE.get())
                    .define('B', RotaryItems.BEDROCK_ALLOY_INGOT.get()).define('P', RotaryItems.HSLA_PLATE.get())
                    .define('S', RotaryItems.HSLA_STEEL_INGOT.get()).define('G', RotaryItems.BEDROCK_ALLOY_GEAR_16x.get())
                    .pattern("SPS").pattern("B B").pattern("PGP")
                    .unlockedBy("has_bedrock_ingot", has(RotaryItems.BEDROCK_ALLOY_INGOT.get())).save(out);

            shaped(RecipeCategory.MISC, ReactorBlocks.PROCESSOR.get())
                    .define('O', ReactorItems.crafting(CraftingItems.TANK)).define('M', RotaryItems.MIXER.get())
                    .define('P', RotaryBlocks.FLUID_PIPE.get())
                    .pattern("POP").pattern("OMO")
                    .unlockedBy("has_tank", has(ReactorItems.crafting(CraftingItems.TANK))).save(out);

            shaped(RecipeCategory.MISC, ReactorBlocks.WASTECONTAINER.get())
                    .define('S', RotaryItems.HSLA_STEEL_INGOT.get()).define('C', RotaryBlocks.COOLING_FIN.get())
                    .define('c', Items.CHEST)
                    .pattern("SCS").pattern("CcC").pattern("SCS")
                    .unlockedBy("has_cooling_fin", has(RotaryBlocks.COOLING_FIN.get())).save(out);

            shaped(RecipeCategory.MISC, ReactorBlocks.BOILER.get())
                    .define('r', RotaryBlocks.RESERVOIR.get()).define('P', RotaryItems.HSLA_PLATE.get())
                    .define('S', RotaryItems.HSLA_STEEL_INGOT.get())
                    .pattern("SPS").pattern("PrP").pattern("SPS")
                    .unlockedBy("has_reservoir", has(RotaryBlocks.RESERVOIR.get())).save(out);

            shaped(RecipeCategory.MISC, ReactorBlocks.CONDENSER.get())
                    .define('F', RotaryBlocks.COOLING_FIN.get()).define('S', RotaryItems.HSLA_STEEL_INGOT.get())
                    .define('P', RotaryItems.HSLA_PLATE.get()).define('p', RotaryBlocks.FLUID_PIPE.get())
                    .define('R', RotaryBlocks.RESERVOIR.get())
                    .pattern("SPS").pattern("pRp").pattern("FFF")
                    .unlockedBy("has_cooling_fin", has(RotaryBlocks.COOLING_FIN.get())).save(out);

            shaped(RecipeCategory.MISC, ReactorBlocks.GRATE.get())
                    .define('p', RotaryItems.HSLA_PLATE.get()).define('P', RotaryBlocks.FLUID_PIPE.get())
                    .define('S', RotaryItems.HSLA_STEEL_INGOT.get()).define('I', Items.IRON_BARS)
                    .pattern("SIS").pattern("p p").pattern("SPS")
                    .unlockedBy("has_plate", has(RotaryItems.HSLA_PLATE.get())).save(out);

            shaped(RecipeCategory.MISC, ReactorBlocks.PUMP.get())
                    .define('P', RotaryItems.HSLA_PLATE.get()).define('g', Items.GLASS_PANE)
                    .define('p', RotaryBlocks.FLUID_PIPE.get()).define('C', RotaryItems.COMPRESSOR.get())
                    .define('s', RotaryItems.HSLA_SHAFT.get())
                    .pattern("PpP").pattern("gCg").pattern("PsP")
                    .unlockedBy("has_compressor", has(RotaryItems.COMPRESSOR.get())).save(out);

            shaped(RecipeCategory.MISC, ReactorBlocks.SYNTHESIZER.get())
                    .define('S', RotaryItems.HSLA_STEEL_INGOT.get()).define('M', RotaryItems.MIXER.get())
                    .define('p', RotaryItems.HSLA_PLATE.get()).define('h', RotaryItems.IGNITION_UNIT.get())
                    .pattern("SpS").pattern("pMp").pattern("ShS")
                    .unlockedBy("has_mixer", has(RotaryItems.MIXER.get())).save(out);

            shaped(RecipeCategory.MISC, ReactorBlocks.SODIUMBOILER.get())
                    .define('b', ReactorBlocks.BOILER.get()).define('i', Items.IRON_INGOT)
                    .pattern(" i ").pattern("ibi").pattern(" i ")
                    .unlockedBy("has_boiler", has(ReactorBlocks.BOILER.get())).save(out);

            shaped(RecipeCategory.MISC, ReactorBlocks.BREEDER.get())
                    .define('P', RotaryItems.HSLA_PLATE.get()).define('S', RotaryItems.HSLA_STEEL_INGOT.get())
                    .define('C', ReactorBlocks.FUEL.get())
                    .pattern("SPS").pattern("PCP").pattern("SPS")
                    .unlockedBy("has_fuel_core", has(ReactorBlocks.FUEL.get())).save(out);

            shaped(RecipeCategory.MISC, ReactorBlocks.TRITIZER.get())
                    .define('G', RotaryBlocks.BLASTGLASS.get()).define('P', RotaryBlocks.FLUID_PIPE.get())
                    .define('S', RotaryItems.HSLA_STEEL_INGOT.get())
                    .pattern("SPS").pattern("GPG").pattern("SPS")
                    .unlockedBy("has_blastglass", has(RotaryBlocks.BLASTGLASS.get())).save(out);

            // 'S' = legacy ItemStacks.silumin. The port consolidated the aluminum line: silumin (the
            // Al-Si alloy) is ALUMINUM_ALLOY_INGOT — see RoCRecipeProvider's SILUMIN↔ALUMINUM_ALLOY_INGOT
            // mapping + its silicon-dust blast recipe (the legacy silumin recipe).
            shaped(RecipeCategory.MISC, ReactorBlocks.WASTEDECAYER.get())
                    .define('H', Items.HOPPER).define('G', RotaryBlocks.BLASTGLASS.get())
                    .define('P', ReactorBlocks.WASTECONTAINER.get()).define('S', RotaryItems.ALUMINUM_ALLOY_INGOT.get())
                    .pattern("SHS").pattern("GPG").pattern("SHS")
                    .unlockedBy("has_waste_container", has(ReactorBlocks.WASTECONTAINER.get())).save(out);

            shaped(RecipeCategory.MISC, ReactorBlocks.ELECTROLYZER.get())
                    .define('P', RotaryBlocks.FLUID_PIPE.get()).define('B', RotaryItems.HSLA_PLATE.get())
                    .define('S', RotaryItems.HSLA_STEEL_INGOT.get()).define('R', RotaryBlocks.RESERVOIR.get())
                    .pattern("SPS").pattern("PRP").pattern("BPB")
                    .unlockedBy("has_reservoir", has(RotaryBlocks.RESERVOIR.get())).save(out);

            shaped(RecipeCategory.MISC, ReactorBlocks.EXCHANGER.get())
                    .define('P', RotaryBlocks.FLUID_PIPE.get()).define('I', RotaryItems.IMPELLER.get())
                    .define('G', Items.GOLD_INGOT).define('F', RotaryBlocks.COOLING_FIN.get())
                    .pattern("FPF").pattern("GIG").pattern("FPF")
                    .unlockedBy("has_cooling_fin", has(RotaryBlocks.COOLING_FIN.get())).save(out);

            shaped(RecipeCategory.MISC, ReactorBlocks.STORAGE.get())
                    .define('S', RotaryItems.HSLA_STEEL_INGOT.get()).define('P', RotaryItems.HSLA_PLATE.get())
                    .define('C', Items.CHEST)
                    .pattern("SPS").pattern("PCP").pattern("SPS")
                    .unlockedBy("has_plate", has(RotaryItems.HSLA_PLATE.get())).save(out);

            shaped(RecipeCategory.MISC, ReactorBlocks.CPU.get())
                    .define('S', RotaryItems.HSLA_PLATE.get()).define('C', RotaryItems.CIRCUIT_BOARD.get())
                    .define('G', RotaryItems.HSLA_STEEL_GEAR_2x.get())
                    .pattern("SCS").pattern("CGC").pattern("SCS")
                    .unlockedBy("has_circuit_board", has(RotaryItems.CIRCUIT_BOARD.get())).save(out);

            shaped(RecipeCategory.MISC, ReactorBlocks.SOLENOID.get())
                    .define('S', RotaryItems.HSLA_STEEL_INGOT.get()).define('P', RotaryItems.HSLA_PLATE.get())
                    .define('M', ReactorItems.crafting(CraftingItems.MAGNETIC)).define('C', ReactorItems.crafting(CraftingItems.MAGNETCORE))
                    .define('I', ReactorItems.crafting(CraftingItems.FERROINGOT)).define('G', RotaryItems.TUNGSTEN_ALLOY_GEAR_16x.get())
                    .pattern("SPS").pattern("MCM").pattern("IGI")
                    .unlockedBy("has_magnet_core", has(ReactorItems.crafting(CraftingItems.MAGNETCORE))).save(out);

            shaped(RecipeCategory.MISC, ReactorBlocks.CO2HEATER.get())
                    .define('b', ReactorBlocks.BOILER.get()).define('i', RotaryItems.HSLA_PLATE.get())
                    .pattern(" i ").pattern("ibi").pattern(" i ")
                    .unlockedBy("has_boiler", has(ReactorBlocks.BOILER.get()))
                    .save(out, ReactorCraft.MODID + ":co2_heater_from_boiler");

            shaped(RecipeCategory.MISC, ReactorBlocks.PEBBLEBED.get())
                    .define('H', Items.HOPPER).define('P', RotaryItems.HSLA_PLATE.get())
                    .define('S', RotaryItems.HSLA_STEEL_INGOT.get()).define('C', ReactorBlocks.FUEL.get())
                    .pattern("SHS").pattern("PCP").pattern("SHS")
                    .unlockedBy("has_fuel_core", has(ReactorBlocks.FUEL.get())).save(out);

            shaped(RecipeCategory.MISC, ReactorBlocks.COLLECTOR.get())
                    .define('p', RotaryBlocks.FLUID_PIPE.get()).define('P', RotaryItems.HSLA_PLATE.get())
                    .define('S', RotaryItems.HSLA_STEEL_INGOT.get())
                    .pattern(" p ").pattern("SpS").pattern("PpP")
                    .unlockedBy("has_pipe", has(RotaryBlocks.FLUID_PIPE.get())).save(out);

            shaped(RecipeCategory.MISC, ReactorBlocks.TURBINEMETER.get())
                    .define('S', RotaryItems.HSLA_STEEL_INGOT.get()).define('r', Items.REDSTONE)
                    .define('P', RotaryItems.HSLA_PLATE.get()).define('G', Items.GLOWSTONE)
                    .define('C', RotaryItems.CIRCUIT_BOARD.get())
                    .pattern("SrS").pattern("PGP").pattern("PCP")
                    .unlockedBy("has_circuit_board", has(RotaryItems.CIRCUIT_BOARD.get())).save(out);

            shaped(RecipeCategory.MISC, ReactorBlocks.BIGTURBINE.get())
                    .define('B', RotaryItems.PROPELLER_BLADE.get()).define('C', ReactorBlocks.TURBINECORE.get())
                    .pattern("BBB").pattern("BCB").pattern("BBB")
                    .unlockedBy("has_turbine", has(ReactorBlocks.TURBINECORE.get())).save(out);

            shaped(RecipeCategory.MISC, ReactorBlocks.FLYWHEEL.get())
                    .define('B', RotaryBlocks.HSLA_STEEL_BLOCK.get()).define('S', RotaryItems.HSLA_SHAFT.get())
                    .define('T', RotaryItems.TUNGSTEN_ALLOY_SHAFT_CORE.get())
                    .pattern("BBB").pattern("STS").pattern("BBB")
                    .unlockedBy("has_shaft_core", has(RotaryItems.TUNGSTEN_ALLOY_SHAFT_CORE.get())).save(out);

            shaped(RecipeCategory.MISC, ReactorBlocks.DIFFUSER.get())
                    .define('B', RotaryItems.HSLA_PLATE.get()).define('D', RotaryItems.DIFFUSER.get())
                    .define('P', RotaryBlocks.FLUID_PIPE.get())
                    .pattern("BBB").pattern("DPD").pattern("BBB")
                    .unlockedBy("has_diffuser", has(RotaryItems.DIFFUSER.get())).save(out);

            // 'a' = legacy ItemStacks.silumin → ALUMINUM_ALLOY_INGOT (port's silumin; see above).
            shaped(RecipeCategory.MISC, ReactorBlocks.THORIUM.get())
                    .define('t', RotaryItems.TUNGSTEN_INGOT.get()).define('a', RotaryItems.ALUMINUM_ALLOY_INGOT.get())
                    .define('P', RotaryItems.HSLA_PLATE.get()).define('S', RotaryItems.HSLA_STEEL_INGOT.get())
                    .define('C', ReactorBlocks.FUEL.get())
                    .pattern("aSa").pattern("PCP").pattern("tPt")
                    .unlockedBy("has_fuel_core", has(ReactorBlocks.FUEL.get())).save(out);

            shaped(RecipeCategory.MISC, ReactorBlocks.FUELDUMP.get())
                    .define('b', Items.IRON_BARS).define('p', RotaryBlocks.FLUID_PIPE.get())
                    .define('P', RotaryBlocks.BEDROCK_PIPE.get()).define('B', RotaryItems.HSLA_PLATE.get())
                    .define('I', RotaryItems.IMPELLER.get())
                    .pattern("pIp").pattern("BPB").pattern("pbp")
                    .unlockedBy("has_bedrock_pipe", has(RotaryBlocks.BEDROCK_PIPE.get())).save(out);

            shaped(RecipeCategory.MISC, ReactorBlocks.SOLARTOP.get())
                    .define('a', ReactorItems.crafting(CraftingItems.ALLOY)).define('t', RotaryItems.TUNGSTEN_INGOT.get())
                    .define('c', RotaryItems.CONDENSER.get()).define('P', RotaryItems.HSLA_PLATE.get())
                    .define('s', RotaryItems.HSLA_STEEL_INGOT.get())
                    .pattern("aPa").pattern("tct").pattern("sPs")
                    .unlockedBy("has_condenser", has(RotaryItems.CONDENSER.get())).save(out);

            shaped(RecipeCategory.MISC, ReactorBlocks.SOLAR.get())
                    .define('p', RotaryBlocks.FLUID_PIPE.get()).define('E', ReactorBlocks.EXCHANGER.get())
                    .define('P', RotaryItems.HSLA_PLATE.get()).define('s', RotaryItems.HSLA_STEEL_INGOT.get())
                    .pattern("sPs").pattern("pEp").pattern("sPs")
                    .unlockedBy("has_exchanger", has(ReactorBlocks.EXCHANGER.get())).save(out);

            shaped(RecipeCategory.MISC, ReactorBlocks.GASPIPE.get(), 8)
                    .define('C', Items.TERRACOTTA).define('G', Items.GLASS)
                    .pattern("CGC").pattern("CGC").pattern("CGC")
                    .unlockedBy("has_terracotta", has(Items.TERRACOTTA)).save(out);

            shaped(RecipeCategory.MISC, ReactorBlocks.MAGNETPIPE.get(), 8)
                    .define('C', Items.GOLD_INGOT).define('G', RotaryBlocks.BLASTGLASS.get())
                    .pattern("CGC").pattern("CGC").pattern("CGC")
                    .unlockedBy("has_blastglass", has(RotaryBlocks.BLASTGLASS.get())).save(out);

            shaped(RecipeCategory.MISC, ReactorBlocks.WASTEPIPE.get(), 8)
                    .define('C', ReactorBlocks.matBlock(MatBlocks.CONCRETE)).define('b', Items.IRON_BARS).define('G', Items.GLASS)
                    .pattern("CbC").pattern("CGC").pattern("CbC")
                    .unlockedBy("has_concrete", has(ReactorBlocks.matBlock(MatBlocks.CONCRETE))).save(out);

            shaped(RecipeCategory.MISC, ReactorBlocks.HEATPIPE.get(), 6)
                    .define('N', ItemTags.WOOL).define('P', Items.GOLD_INGOT)
                    .pattern(" NP").pattern("NPN").pattern("PN ")
                    .unlockedBy("has_gold", has(Items.GOLD_INGOT)).save(out);

            shaped(RecipeCategory.MISC, ReactorBlocks.MAGNET.get())
                    .define('H', ReactorItems.crafting(CraftingItems.HYSTERESISRING))
                    .define('M', ReactorItems.crafting(CraftingItems.MAGNETCORE))
                    .define('C', ReactorItems.crafting(CraftingItems.COOLANT))
                    .pattern("MCM").pattern("CHC").pattern("MCM")
                    .unlockedBy("has_magnet_core", has(ReactorItems.crafting(CraftingItems.MAGNETCORE))).save(out);

            shaped(RecipeCategory.MISC, ReactorBlocks.HEATER.get())
                    .define('M', ReactorItems.crafting(CraftingItems.FERROINGOT)).define('P', RotaryBlocks.BLASTGLASS.get())
                    .pattern("MPM").pattern("P P").pattern("MPM")
                    .unlockedBy("has_ferro", has(ReactorItems.crafting(CraftingItems.FERROINGOT))).save(out);

            shaped(RecipeCategory.MISC, ReactorBlocks.INJECTOR.get())
                    .define('P', ReactorBlocks.MAGNETPIPE.get()).define('M', ReactorItems.crafting(CraftingItems.MAGNETIC))
                    .pattern("PMP").pattern("M M").pattern("PMP")
                    .unlockedBy("has_magnetic_pipe", has(ReactorBlocks.MAGNETPIPE.get())).save(out);

            shaped(RecipeCategory.MISC, ReactorBlocks.ABSORBER.get())
                    .define('C', RotaryBlocks.HSLA_STEEL_BLOCK.get()).define('P', ReactorItems.DEPLETED_FUEL.get())
                    .pattern(" P ").pattern("PCP").pattern(" P ")
                    .unlockedBy("has_depleted", has(ReactorItems.DEPLETED_FUEL.get())).save(out);
            shaped(RecipeCategory.MISC, ReactorBlocks.ABSORBER.get())
                    .define('C', RotaryBlocks.HSLA_STEEL_BLOCK.get()).define('P', ReactorItems.DEPLETED_PELLET.get())
                    .pattern("PPP").pattern("PCP").pattern("PPP")
                    .unlockedBy("has_depleted_pellet", has(ReactorItems.DEPLETED_PELLET.get()))
                    .save(out, ReactorCraft.MODID + ":neutron_absorber_from_pellet");

            shaped(RecipeCategory.MISC, ReactorBlocks.REFLECTOR.get())
                    .define('G', ReactorItems.crafting(CraftingItems.GRAPHITE)).define('S', RotaryBlocks.HSLA_STEEL_BLOCK.get())
                    .pattern("GGG").pattern("GSG").pattern("GGG")
                    .unlockedBy("has_graphite", has(ReactorItems.crafting(CraftingItems.GRAPHITE))).save(out);

            shaped(RecipeCategory.MISC, ReactorBlocks.GENERATOR.get())
                    .define('G', ReactorItems.crafting(CraftingItems.WIRE)).define('R', Items.REDSTONE)
                    .define('F', ReactorItems.crafting(CraftingItems.MAGNETCORE))
                    .pattern("RGR").pattern("GFG").pattern("RGR")
                    .unlockedBy("has_wire", has(ReactorItems.crafting(CraftingItems.WIRE))).save(out);

            shaped(RecipeCategory.MISC, ReactorBlocks.MARKER.get())
                    .define('F', ReactorItems.fluorite(FluoriteTypes.BLUE)).define('R', Items.REDSTONE_TORCH)
                    .pattern("F").pattern("R")
                    .unlockedBy("has_fluorite", has(ReactorItems.fluorite(FluoriteTypes.BLUE))).save(out);
        }

        private void smelting() {
            for (ReactorOreType ore : ReactorOreType.list) {
                if (ore == ReactorOreType.FLUORITE)
                    continue;
                smelt("smelt_" + ore.featureName(), ore.getBlock().asItem(), ore.getProduct(), ore.xp);
            }
            for (FluoriteTypes f : FluoriteTypes.colorList) {
                smelt("smelt_" + f.getOreBlockName(),
                        ReactorBlocks.fluoriteOre(f).asItem(),
                        ReactorItems.fluorite(f), 0.4F);
            }
            smelt("smelt_calcite_to_lime", ReactorItems.CALCITE.get(), ReactorItems.LIME.get(), 0.2F);
        }

        private void smelt(String id, ItemLike input, ItemLike output, float xp) {
            SimpleCookingRecipeBuilder.smelting(Ingredient.of(input), RecipeCategory.MISC,
                            CookingBookCategory.MISC, output.asItem(), xp, 200)
                    .unlockedBy("has_input", has(input))
                    .save(out, ReactorCraft.MODID + ":" + id);
        }

        private void crafting() {
            // Hazmat suit — radiation-shielding fabric armour (legacy ReactorRecipes hazmat set).
            var fabric = ReactorItems.crafting(CraftingItems.FABRIC);
            shaped(RecipeCategory.COMBAT, ReactorItems.HAZMAT_HELMET.get())
                    .define('F', fabric).pattern("FFF").pattern("F F")
                    .unlockedBy("has_fabric", has(fabric)).save(out);
            shaped(RecipeCategory.COMBAT, ReactorItems.HAZMAT_CHESTPLATE.get())
                    .define('F', fabric).pattern("F F").pattern("FFF").pattern("FFF")
                    .unlockedBy("has_fabric", has(fabric)).save(out);
            shaped(RecipeCategory.COMBAT, ReactorItems.HAZMAT_LEGGINGS.get())
                    .define('F', fabric).pattern("FFF").pattern("F F").pattern("F F")
                    .unlockedBy("has_fabric", has(fabric)).save(out);
            shaped(RecipeCategory.COMBAT, ReactorItems.HAZMAT_BOOTS.get())
                    .define('F', fabric).pattern("F F").pattern("F F")
                    .unlockedBy("has_fabric", has(fabric)).save(out);

            // Coil-charged radiation tools (legacy: " r ","sSs","sgs" and " sp","sbs","ss ").
            shaped(RecipeCategory.TOOLS, ReactorItems.REMOTE_CONTROL.get())
                    .define('S', RotaryItems.HSLA_STEEL_INGOT.get()).define('E', Items.ENDER_PEARL)
                    .define('B', Items.STONE_BUTTON).define('C', RotaryItems.CIRCUIT_BOARD.get())
                    .define('P', RotaryItems.HSLA_PLATE.get())
                    .pattern("SES").pattern("BCB").pattern("BPB")
                    .unlockedBy("has_circuit", has(RotaryItems.CIRCUIT_BOARD.get())).save(out);

            shaped(RecipeCategory.TOOLS, ReactorItems.GEIGER_COUNTER.get())
                    .define('r', RotaryItems.RADAR_UNIT.get()).define('s', RotaryItems.HSLA_STEEL_INGOT.get())
                    .define('S', RotaryItems.SCREEN.get()).define('g', RotaryItems.HSLA_STEEL_GEAR.get())
                    .pattern(" r ").pattern("sSs").pattern("sgs")
                    .unlockedBy("has_screen", has(RotaryItems.SCREEN.get())).save(out);
            shaped(RecipeCategory.TOOLS, ReactorItems.RADIATION_CLEANER.get())
                    .define('b', Items.WATER_BUCKET).define('s', RotaryItems.HSLA_STEEL_INGOT.get())
                    .define('p', RotaryBlocks.FLUID_PIPE.get())
                    .pattern(" sp").pattern("sbs").pattern("ss ")
                    .unlockedBy("has_steel", has(RotaryItems.HSLA_STEEL_INGOT.get())).save(out);
            // Magnetic Ore Finder (legacy: "L L","S S","SSS", L=lodestone, S=steel).
            shaped(RecipeCategory.TOOLS, ReactorItems.IRON_FINDER.get())
                    .define('L', ReactorItems.LODESTONE.get()).define('S', RotaryItems.HSLA_STEEL_INGOT.get())
                    .pattern("L L").pattern("S S").pattern("SSS")
                    .unlockedBy("has_lodestone", has(ReactorItems.LODESTONE.get())).save(out);

            shaped(RecipeCategory.MISC, ReactorItems.crafting(CraftingItems.TANK))
                    .define('O', RotaryBlocks.BLASTGLASS.get())
                    .pattern("OOO").pattern("O O").pattern("OOO")
                    .unlockedBy("has_blast_glass", has(RotaryBlocks.BLASTGLASS.get()))
                    .save(out);

            shaped(RecipeCategory.MISC, ReactorItems.crafting(CraftingItems.CANISTER))
                    .define('S', ReactorItems.crafting(CraftingItems.ALLOY))
                    .define('C', Items.CHEST)
                    .pattern(" S ").pattern("SCS").pattern(" S ")
                    .unlockedBy("has_alloy", has(ReactorItems.crafting(CraftingItems.ALLOY)))
                    .save(out);

            shaped(RecipeCategory.MISC, ReactorItems.crafting(CraftingItems.ROD))
                    .define('S', RotaryItems.HSLA_STEEL_INGOT.get())
                    .define('A', ReactorItems.crafting(CraftingItems.ALLOY))
                    .define('C', ReactorItems.crafting(CraftingItems.GRAPHITE))
                    .pattern("SAS").pattern("ACA").pattern("SAS")
                    .unlockedBy("has_alloy", has(ReactorItems.crafting(CraftingItems.ALLOY)))
                    .save(out, ReactorCraft.MODID + ":control_rod");

            shaped(RecipeCategory.MISC, ReactorItems.CANISTER.get(), 16)
                    .define('g', Items.GLASS)
                    .define('i', Items.IRON_INGOT)
                    .pattern(" i ").pattern("igi").pattern(" i ")
                    .unlockedBy("has_iron", has(Items.IRON_INGOT))
                    .save(out);

            shaped(RecipeCategory.MISC, ReactorItems.FUEL_ROD.get(), 2)
                    .define('d', ReactorItems.FUEL_DUST.get())
                    .pattern("dd").pattern("dd")
                    .unlockedBy("has_fuel_dust", has(ReactorItems.FUEL_DUST.get()))
                    .save(out);

            shaped(RecipeCategory.MISC, ReactorItems.DEPLETED_FUEL.get(), 2)
                    .define('d', ReactorItems.DEPLETED_DUST.get())
                    .pattern("dd").pattern("dd")
                    .unlockedBy("has_depleted_dust", has(ReactorItems.DEPLETED_DUST.get()))
                    .save(out);

            // Breeder fuel — a fuel rod ringed by depleted fuel (ReactorRecipes.addItems:
            // " D ","DED"," D " → BREEDERFUEL x4, D=DEPLETED, E=FUEL). Fresh (damage 0) result.
            shaped(RecipeCategory.MISC, ReactorItems.BREEDER_FUEL.get(), 4)
                    .define('D', ReactorItems.DEPLETED_FUEL.get())
                    .define('E', ReactorItems.FUEL_ROD.get())
                    .pattern(" D ").pattern("DED").pattern(" D ")
                    .unlockedBy("has_fuel_rod", has(ReactorItems.FUEL_ROD.get()))
                    .save(out);

            // Reactor handbook (ReactorRecipes.addItems: "RSR","PPP","PPP", R=fluorite, S=steel, P=paper).
            shaped(RecipeCategory.MISC, ReactorItems.REACTOR_BOOK.get())
                    .define('R', ReactorItems.fluorite(FluoriteTypes.WHITE))
                    .define('S', RotaryItems.HSLA_STEEL_INGOT.get())
                    .define('P', Items.PAPER)
                    .pattern("RSR").pattern("PPP").pattern("PPP")
                    .unlockedBy("has_fluorite", has(ReactorItems.fluorite(FluoriteTypes.WHITE)))
                    .save(out);

            matAndComponentCrafting();
        }

        // Material-block compaction + the crafting-table component recipes from the original
        // ReactorRecipes.addCrafting/addMisc.
        private void matAndComponentCrafting() {
            // Coolant pack — the formerly pipe-gated recipe (pipe → FLUID_PIPE block, like the machines).
            shaped(RecipeCategory.MISC, ReactorItems.crafting(CraftingItems.COOLANT))
                    .define('S', RotaryItems.HSLA_STEEL_INGOT.get())
                    .define('P', Items.GLASS_PANE)
                    .define('p', RotaryBlocks.FLUID_PIPE.get())
                    .pattern("SPS").pattern("S S").pattern("SpS")
                    .unlockedBy("has_pipe", has(RotaryBlocks.FLUID_PIPE.get()))
                    .save(out);

            // Steam scrubber (formerly pipe-gated).
            shaped(RecipeCategory.MISC, ReactorBlocks.matBlock(MatBlocks.SCRUBBER))
                    .define('I', Items.IRON_BARS)
                    .define('W', ItemTags.WOOL)
                    .define('P', RotaryBlocks.FLUID_PIPE.get())
                    .pattern("IWI").pattern("WPW").pattern("IWI")
                    .unlockedBy("has_pipe", has(RotaryBlocks.FLUID_PIPE.get()))
                    .save(out);

            // Concrete: clay + sand + gravel + water bucket -> 4.
            shapeless(RecipeCategory.MISC, ReactorBlocks.matBlock(MatBlocks.CONCRETE), 4)
                    .requires(Items.CLAY).requires(Items.SAND).requires(Items.GRAVEL).requires(Items.WATER_BUCKET)
                    .unlockedBy("has_clay", has(Items.CLAY))
                    .save(out);

            // Leather from lime + rotten flesh (ReactorRecipes.addMisc: shapeless(leather, lime, rotten_flesh)).
            // Explicit id so the vanilla result does not claim the minecraft:leather recipe id.
            shapeless(RecipeCategory.MISC, Items.LEATHER)
                    .requires(ReactorItems.LIME.get())
                    .requires(Items.ROTTEN_FLESH)
                    .unlockedBy("has_lime", has(ReactorItems.LIME.get()))
                    .save(out, ReactorCraft.MODID + ":leather_from_lime");

            blockCompaction("calcite_block", ReactorBlocks.matBlock(MatBlocks.CALCITE), ReactorItems.CALCITE.get());
            blockCompaction("graphite_block", ReactorBlocks.matBlock(MatBlocks.GRAPHITE), ReactorItems.crafting(CraftingItems.GRAPHITE));
            blockCompaction("lodestone_block", ReactorBlocks.matBlock(MatBlocks.LODESTONE), ReactorItems.LODESTONE.get());

            // Ferromagnetic plate -> magnetic (x3).
            shaped(RecipeCategory.MISC, ReactorItems.crafting(CraftingItems.MAGNETIC), 3)
                    .define('S', ReactorItems.crafting(CraftingItems.FERROINGOT))
                    .pattern("SSS")
                    .unlockedBy("has_ferro", has(ReactorItems.crafting(CraftingItems.FERROINGOT)))
                    .save(out);

            ringRecipe(CraftingItems.MAGNETCORE, CraftingItems.MAGNETIC);
            ringRecipe(CraftingItems.HYSTERESISRING, CraftingItems.HYSTERESIS);

            shaped(RecipeCategory.MISC, ReactorItems.crafting(CraftingItems.WIRE), 2)
                    .define('G', Items.GOLD_INGOT)
                    .pattern("  G").pattern(" G ").pattern("G  ")
                    .unlockedBy("has_gold", has(Items.GOLD_INGOT))
                    .save(out);

            // Count was DifficultyEffects.PARTCRAFT.getInt() upstream (easy 6 / med 3 / hard 2); fixed 2
            // to stay consistent with the port's hard-tier choice for PIPECRAFT (fixed 8). The prior
            // value 1 matched no difficulty tier.
            shaped(RecipeCategory.MISC, ReactorItems.crafting(CraftingItems.HYSTERESIS), 2)
                    .define('I', Items.IRON_INGOT)
                    .define('S', RotaryItems.HSLA_STEEL_INGOT.get())
                    .pattern("ISI")
                    .unlockedBy("has_steel", has(RotaryItems.HSLA_STEEL_INGOT.get()))
                    .save(out);

            shaped(RecipeCategory.MISC, ReactorItems.crafting(CraftingItems.FABRIC), 3)
                    .define('D', ReactorItems.DEPLETED_FUEL.get())
                    .define('L', Items.LEATHER)
                    .pattern("LDL").pattern("LDL").pattern("LDL")
                    .unlockedBy("has_depleted", has(ReactorItems.DEPLETED_FUEL.get()))
                    .save(out);
            shaped(RecipeCategory.MISC, ReactorItems.crafting(CraftingItems.FABRIC))
                    .define('D', ReactorItems.DEPLETED_PELLET.get())
                    .define('L', Items.LEATHER)
                    .pattern("LDL").pattern("LDL").pattern("LDL")
                    .unlockedBy("has_depleted_pellet", has(ReactorItems.DEPLETED_PELLET.get()))
                    .save(out, ReactorCraft.MODID + ":radiation_fabric_from_pellet");

            shapeless(RecipeCategory.MISC, ReactorItems.crafting(CraftingItems.CARBIDEFLAKES))
                    .requires(RotaryItems.COAL_DUST.get())
                    .requires(RotaryItems.TUNGSTEN_FLAKES.get())
                    .unlockedBy("has_tungsten_flakes", has(RotaryItems.TUNGSTEN_FLAKES.get()))
                    .save(out);

            shaped(RecipeCategory.MISC, ReactorItems.crafting(CraftingItems.TURBCORE))
                    .define('C', ReactorItems.crafting(CraftingItems.CARBIDE))
                    .define('T', RotaryItems.COMPOUND_TURBINE.get())
                    .pattern("CCC").pattern("CTC").pattern("CCC")
                    .unlockedBy("has_carbide", has(ReactorItems.crafting(CraftingItems.CARBIDE)))
                    .save(out);

            for (FluoriteTypes f : FluoriteTypes.colorList) {
                blockCompaction(f.getColorName() + "_fluorite_block", ReactorBlocks.fluoriteBlock(f), ReactorItems.fluorite(f));
                shaped(RecipeCategory.MISC, ReactorBlocks.tritiumLamp(f))
                        .define('C', ReactorItems.fluorite(f))
                        .define('S', RotaryItems.HSLA_STEEL_INGOT.get())
                        .define('O', Items.OBSIDIAN)
                        .pattern("SCS").pattern("C C").pattern("SOS")
                        .unlockedBy("has_fluorite", has(ReactorItems.fluorite(f)))
                        .save(out);
            }
        }

        // 3x3 item -> block, plus the reverse block -> 9 items.
        private void blockCompaction(String id, ItemLike block, ItemLike item) {
            shaped(RecipeCategory.MISC, block)
                    .define('C', item)
                    .pattern("CCC").pattern("CCC").pattern("CCC")
                    .unlockedBy("has_material", has(item))
                    .save(out);
            shapeless(RecipeCategory.MISC, item, 9)
                    .requires(block)
                    .unlockedBy("has_block", has(block))
                    .save(out, ReactorCraft.MODID + ":" + id + "_uncraft");
        }

        // A hollow 3x3 ring of one component -> another (magnetic->core, hysteresis->ring).
        private void ringRecipe(CraftingItems result, CraftingItems part) {
            shaped(RecipeCategory.MISC, ReactorItems.crafting(result))
                    .define('C', ReactorItems.crafting(part))
                    .pattern("CCC").pattern("C C").pattern("CCC")
                    .unlockedBy("has_part", has(ReactorItems.crafting(part)))
                    .save(out);
        }

        // RotaryCraft-typed recipes referencing reactorcraft items (emitted in our namespace).
        private void crossMod() {
            List<Ingredient> alloyIns = new ArrayList<>();
            alloyIns.add(Ingredient.of(ReactorItems.CADMIUM_INGOT.get()));
            alloyIns.add(Ingredient.of(ReactorItems.INDIUM_INGOT.get()));
            alloyIns.add(Ingredient.of(ReactorItems.SILVER_INGOT.get()));
            accept("alloy", new ShapelessBlastFurnaceRecipe(
                    alloyIns, List.of(),
                    new ItemStackTemplate(ReactorItems.crafting(CraftingItems.ALLOY), 3),
                    1600F, 0F, 1.0F, 0, 0, 0, 3, true, false));

            // Ferromagnetic ingot — steel + iron + lodestone alloyed in the blast furnace @ 1200°.
            // (ReactorRecipes.addCrafting: addAlloyingRecipe(FERROINGOT, 1200, steel+iron+lodestone).
            // The commented-out crafting-table variant is the disabled path; the alloying one is live.)
            // This is FERROINGOT's only production recipe — without it the whole magnetic-parts line
            // (magnetic/magnetcore/hysteresis/solenoid/heater) had no source. The nickel-substituting
            // variant is ore-dict-gated (ingotNickel) and omitted like the other ore-dict alternates.
            List<Ingredient> ferroIns = new ArrayList<>();
            ferroIns.add(Ingredient.of(RotaryItems.HSLA_STEEL_INGOT.get()));
            ferroIns.add(Ingredient.of(Items.IRON_INGOT));
            ferroIns.add(Ingredient.of(ReactorItems.LODESTONE.get()));
            accept("ferromagnetic_ingot", new ShapelessBlastFurnaceRecipe(
                    ferroIns, List.of(),
                    new ItemStackTemplate(ReactorItems.crafting(CraftingItems.FERROINGOT)),
                    1200F, 0F, 1.0F, 0, 0, 0, 3, true, false));

            // Lime from an egg in the blast furnace @ 850° (ReactorRecipes.addSmelting:
            // addRecipe(lime, 850, shapeless(lime, egg))). Smelting calcite→lime is the primary source;
            // this is the secondary egg route.
            accept("lime_from_egg", new ShapelessBlastFurnaceRecipe(
                    List.of(Ingredient.of(Items.EGG)), List.of(),
                    new ItemStackTemplate(ReactorItems.LIME.get()),
                    850F, 0F, 1.0F, 0, 0, 0, 1, true, false));

            accept("graphite", new FrictionHeaterRecipe(
                    Ingredient.of(RotaryItems.COAL_DUST.get()),
                    new ItemStackTemplate(ReactorItems.crafting(CraftingItems.GRAPHITE)),
                    400F, 100));

            // Tungsten carbide flakes -> ingot in the RotaryCraft pulse-jet furnace (legacy ReactorRecipes:95,
            // RecipesPulseFurnace.addSmelting(carbideflakes, carbide) with no explicit temperature). The
            // 2-arg addSmelting derives its temperature from PulseJetRecipe(in,out) -> getDefaultMeltingTemp =
            // clamp(ItemMaterialController.getMeltingPoint(in)/2, 400, 850). carbideflakes carries NO material
            // data in any mod, so getMeltingPoint returns 0 and the temperature clamps to the 400 floor.
            // CARBIDE is the ONLY gate on TURBCORE -> TURBINECORE (the reactor steam turbine), so without this
            // the reactor->power path is uncraftable in survival.
            accept("carbide", new PulseFurnaceRecipe(
                    Ingredient.of(ReactorItems.crafting(CraftingItems.CARBIDEFLAKES)),
                    new ItemStackTemplate(ReactorItems.crafting(CraftingItems.CARBIDE)),
                    400F));

            accept("grinder/uranium_to_udust", new GrinderRecipe(
                    Ingredient.of(ReactorItems.URANIUM_INGOT.get()),
                    new ItemStackTemplate(ReactorItems.crafting(CraftingItems.UDUST))));

            accept("grinder/emerald_to_dust", new GrinderRecipe(
                    Ingredient.of(Items.EMERALD),
                    new ItemStackTemplate(ReactorItems.EMERALD_DUST.get())));

            // Nuclear-waste reprocessing entry point (legacy ReactorRecipes.addRCInterface:88):
            //   RecipesCrystallizer.addRecipe(FluidRegistry.getFluid("rc nuclear waste"), 50, wastedust)
            // — 50 mB of the reactor's nuclear-waste fluid freezes into one waste_dust in RotaryCraft's
            // Crystallizer. This is the ONLY producer of waste_dust and the head of the reprocessing loop
            // (waste_dust is then meant to be split into thorium/element-group byproducts by the centrifuge).
            // Emitted in the reactorcraft namespace with RotaryCraft's crystallizer recipe type, exactly
            // like the grinder/friction-heater cross-mod recipes above.
            accept("crystallizer/waste_dust", new CrystallizerRecipe(
                    BuiltInRegistries.FLUID.wrapAsHolder(ReactorFluids.WASTE.get()), 50,
                    BuiltInRegistries.ITEM.wrapAsHolder(ReactorItems.WASTE_DUST.get()), 1));

            wasteCentrifuge();

            magnetCompactor();
        }

        // The waste-reprocessing cascade in the RotaryCraft centrifuge (legacy ReactorRecipes 90-92):
        //   waste_dust                        -> chanced ELEMENT-GROUP waste  (WasteManager.getThoriumOutputs(true))
        //   group-waste (per ElementGroup g)  -> chanced ISOTOPE waste        (WasteManager.getThoriumGroupOutputs(g))
        // Both stages emit ReactorItems.WASTE_ITEM whose isotope/group identity rides the DAMAGE variant
        // (individual isotope = Isotopes.ordinal(); mixed group = 1000+g.ordinal()) — see ItemNuclearWaste.
        // Chances come from the ported WasteManager math as 0..100 percentages; RotaryCraft's
        // CentrifugeRecipe.rollItems uses 0..1, so divide by 100 (group chances then sum to ~1.0, and each
        // group's isotope chances sum to ~1.0). The group-waste INPUT is a specific WASTE_ITEM damage, which
        // a plain Ingredient cannot distinguish, so it is matched with a DAMAGE-component DataComponentIngredient
        // (exactly like the magnet central/auxiliary casings). Result damage rides an ItemStackTemplate
        // DataComponentPatch (never a live ItemStack — that NPEs at datagen, "Components not bound yet").
        private void wasteCentrifuge() {
            List<ChancedOutput> groupOuts = new ArrayList<>();
            for (WasteChance wc : WasteManager.getGroupChancesForDatagen())
                groupOuts.add(new ChancedOutput(wasteTemplate(wc.damage()), wc.chance()/100F));
            accept("centrifuge/waste_dust", new CentrifugeRecipe(
                    Ingredient.of(ReactorItems.WASTE_DUST.get()), groupOuts, Optional.empty()));

            for (ElementGroup g : ElementGroup.values()) {
                List<WasteChance> isotopes = WasteManager.getGroupIsotopeChancesForDatagen(g);
                if (isotopes.isEmpty())
                    continue;
                List<ChancedOutput> isoOuts = new ArrayList<>();
                for (WasteChance wc : isotopes)
                    isoOuts.add(new ChancedOutput(wasteTemplate(wc.damage()), wc.chance()/100F));
                Ingredient in = DataComponentIngredient.of(false, reika.reactorcraft.registry.ReactorDataComponents.WASTE_IDENTITY.get(), reika.reactorcraft.items.ItemNuclearWaste.identity(1000+g.ordinal()), ReactorItems.WASTE_ITEM.get());
                accept("centrifuge/group_"+g.name().toLowerCase(), new CentrifugeRecipe(in, isoOuts, Optional.empty()));
            }
        }

        // A WASTE_ITEM template carrying its isotope/group identity in the DAMAGE component patch.
        private static ItemStackTemplate wasteTemplate(int damage) {
            DataComponentPatch patch = DataComponentPatch.builder()
                    .set(reika.reactorcraft.registry.ReactorDataComponents.WASTE_IDENTITY.get(), reika.reactorcraft.items.ItemNuclearWaste.identity(damage))
                    .build();
            return new ItemStackTemplate(ReactorItems.WASTE_ITEM.get(), patch);
        }

        // The magnet charge chain in the RotaryCraft compactor (legacy ReactorRecipes lines 97-99):
        //   lodestone -> 2x magnet(charge 0)          @ 5000 kPa / 100 C
        //   magnet(charge i) -> 2x magnet(charge i+1) @ 10000*(1+i) kPa / 100 C   for i = 0..N-2
        // The compactor squeezes a 2x2 (4-item) input per operation, so 4 lodestone -> 2 magnet(0) and
        // 4 magnet(i) -> 2 magnet(i+1). The magnet is the ONLY source of the solenoid magnets, and the
        // compactor is its ONLY producer. Charge rides the stack DAMAGE component (ItemReactorMulti):
        // input charge is disambiguated by CompactorRecipe.inputDamage, output charge by outputDamage.
        // All required pressures (<= 70000 kPa) sit far below the coal chain's 550000, so any compactor
        // able to run the carbon chain makes magnets trivially — faithful to upstream.
        private void magnetCompactor() {
            Item magnet = ReactorItems.MAGNET_ITEM.get();
            compactor("magnet_lodestone", Ingredient.of(ReactorItems.LODESTONE.get()), magnet, 2, 5000, 100, -1, 0);
            int steps = ReactorItems.MAGNET.getNumberMetadatas() - 1;
            for (int i = 0; i < steps; i++)
                compactor("magnet_charge_" + i, Ingredient.of(magnet), magnet, 2, 10000 * (1 + i), 100, i, i + 1);
        }

        private void compactor(String name, Ingredient input, Item output, int count, int pressure,
                               int temperature, int inputDamage, int outputDamage) {
            accept("compactor/" + name, new CompactorRecipe(input, BuiltInRegistries.ITEM.wrapAsHolder(output),
                    count, pressure, temperature, inputDamage, outputDamage));
        }

        private void accept(String path, Recipe<?> recipe) {
            ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE,
                    Identifier.fromNamespaceAndPath(ReactorCraft.MODID, path));
            out.accept(key, recipe, null);
        }
    }
}
