package reika.reactorcraft.data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;

import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.registry.CraftingItems;
import reika.reactorcraft.registry.FluoriteTypes;
import reika.reactorcraft.registry.MatBlocks;
import reika.reactorcraft.registry.ReactorBlocks;
import reika.reactorcraft.registry.ReactorItems;
import reika.reactorcraft.registry.ReactorOreType;
import reika.rotarycraft.auxiliary.recipemanagers.FrictionHeaterRecipe;
import reika.rotarycraft.auxiliary.recipemanagers.GrinderRecipe;
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
public final class ReactorRecipeProvider extends RecipeProvider.Runner {

    public ReactorRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    public String getName() {
        return "ReactorCraft Recipes";
    }

    @Override
    protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput out) {
        return new Recipes(registries, out);
    }

    private static final class Recipes extends RecipeProvider {
        private final RecipeOutput out;

        Recipes(HolderLookup.Provider registries, RecipeOutput out) {
            super(registries, out);
            this.out = out;
        }

        @Override
        protected void buildRecipes() {
            smelting();
            crafting();
            machineCrafting();
            crossMod();
        }

        // The subset of the original addMachines() recipes whose ingredients all resolve in the current
        // registries. The rest are gated on unported RotaryCraft items (basepanel, pipe, prop, shaftitem,
        // pcb, gearunit, silumin, bedrock ingot, igniter, cooling fin, gearbox parts) — see datagen.md.
        // Pipe output counts were DifficultyEffects.PIPECRAFT.getInt() upstream; use a fixed 8 for now.
        private void machineCrafting() {
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

            matAndComponentCrafting();
        }

        // Material-block compaction + the crafting-table component recipes from the original
        // ReactorRecipes.addCrafting/addMisc. Machine-block recipes and the pipe-gated scrubber/coolant
        // recipes are deferred (see docs/issues/datagen.md).
        private void matAndComponentCrafting() {
            // Concrete: clay + sand + gravel + water bucket -> 4.
            shapeless(RecipeCategory.MISC, ReactorBlocks.matBlock(MatBlocks.CONCRETE), 4)
                    .requires(Items.CLAY).requires(Items.SAND).requires(Items.GRAVEL).requires(Items.WATER_BUCKET)
                    .unlockedBy("has_clay", has(Items.CLAY))
                    .save(out);

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

            shaped(RecipeCategory.MISC, ReactorItems.crafting(CraftingItems.HYSTERESIS))
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
                    1600F, 0F, 1.0F, 0, 0, 0));

            accept("graphite", new FrictionHeaterRecipe(
                    Ingredient.of(RotaryItems.COAL_DUST.get()),
                    new ItemStackTemplate(ReactorItems.crafting(CraftingItems.GRAPHITE)),
                    400F, 100));

            accept("grinder/uranium_to_udust", new GrinderRecipe(
                    Ingredient.of(ReactorItems.URANIUM_INGOT.get()),
                    new ItemStackTemplate(ReactorItems.crafting(CraftingItems.UDUST))));

            accept("grinder/emerald_to_dust", new GrinderRecipe(
                    Ingredient.of(Items.EMERALD),
                    new ItemStackTemplate(ReactorItems.EMERALD_DUST.get())));
        }

        private void accept(String path, Recipe<?> recipe) {
            ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE,
                    Identifier.fromNamespaceAndPath(ReactorCraft.MODID, path));
            out.accept(key, recipe, null);
        }
    }
}
