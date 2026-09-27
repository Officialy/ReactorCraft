/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.modinterface.jei;

import java.util.List;
import java.util.stream.Collectors;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;

import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.auxiliary.recipe.CentrifugeRecipe;
import reika.reactorcraft.auxiliary.recipe.ProcessorRecipe;
import reika.reactorcraft.registry.ReactorBlocks;
import reika.reactorcraft.registry.ReactorRecipeTypes;
import reika.reactorcraft.modinterface.jei.ReactorProcessJEICategories.Electrolyzer;
import reika.reactorcraft.modinterface.jei.ReactorProcessJEICategories.Synthesizer;
import reika.reactorcraft.modinterface.jei.ReactorProcessJEICategories.Tritizer;
import reika.reactorcraft.tileentities.processing.TileEntityElectrolyzer.Electrolysis;
import reika.reactorcraft.tileentities.processing.TileEntitySynthesizer.FluidSynthesis;
import reika.reactorcraft.tileentities.processing.TileEntityTritizer.Reactions;

/**
 * JEI integration for ReactorCraft's datapack recipes and fixed machine process lists.
 */
@JeiPlugin
public class ReactorJEIPlugin implements IModPlugin {

    public static final Identifier UID = Identifier.fromNamespaceAndPath(ReactorCraft.MODID, "jei_plugin");
    private static IJeiRuntime runtime;
    private static RecipeMap registeredMap;
    private static List<ProcessorRecipe> registeredProcessor = List.of();
    private static List<CentrifugeRecipe> registeredCentrifuge = List.of();

    @Override
    public Identifier getPluginUid() { return UID; }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper gui = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new ProcessorCategory(gui),
                new CentrifugeCategory(gui),
                new Electrolyzer(gui),
                new Synthesizer(gui),
                new Tritizer(gui)
        );
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ReactorBlocks.PROCESSOR.get()), ProcessorCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ReactorBlocks.CENTRIFUGE.get()), CentrifugeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ReactorBlocks.ELECTROLYZER.get()), Electrolyzer.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ReactorBlocks.SYNTHESIZER.get()), Synthesizer.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ReactorBlocks.TRITIZER.get()), Tritizer.TYPE);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        try {
            registration.addRecipes(Electrolyzer.TYPE, java.util.Arrays.asList(Electrolysis.getRecipes()));
            registration.addRecipes(Synthesizer.TYPE, List.copyOf(FluidSynthesis.list));
            registration.addRecipes(Tritizer.TYPE, List.copyOf(Reactions.reactionList));

            MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
            RecipeMap recipes = ReactorRecipeSync.Client.getCurrentRecipes();
            if (recipes == null && server != null)
                recipes = server.getRecipeManager().recipeMap();
            if (recipes != null) {
                addDataRecipes(recipes, registration::addRecipes);
                registeredMap = recipes;
            }

        } catch (Throwable t) {
            ReactorCraft.LOGGER.error("Failed to register ReactorCraft JEI recipes", t);
        }
    }

    @FunctionalInterface
    private interface RecipeSink {
        <T> void add(RecipeType<T> type, List<T> recipes);
    }

    private static void addDataRecipes(RecipeMap recipes, RecipeSink sink) {
        List<ProcessorRecipe> processor = recipes.byType(ReactorRecipeTypes.PROCESSOR.get())
                .stream().map(RecipeHolder::value).collect(Collectors.toList());
        List<CentrifugeRecipe> centrifuge = recipes.byType(ReactorRecipeTypes.CENTRIFUGE.get())
                .stream().map(RecipeHolder::value).collect(Collectors.toList());
        sink.add(ProcessorCategory.TYPE, processor);
        sink.add(CentrifugeCategory.TYPE, centrifuge);
        registeredProcessor = processor;
        registeredCentrifuge = centrifuge;
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
        RecipeMap synced = ReactorRecipeSync.Client.getCurrentRecipes();
        if (synced != null && synced != registeredMap)
            onRecipeMapReceived(synced);
    }

    @Override
    public void onRuntimeUnavailable() {
        runtime = null;
        registeredMap = null;
        registeredProcessor = List.of();
        registeredCentrifuge = List.of();
    }

    static void onRecipeMapReceived(RecipeMap recipes) {
        if (runtime == null || recipes == registeredMap)
            return;
        try {
            IRecipeManager manager = runtime.getRecipeManager();
            manager.hideRecipes(ProcessorCategory.TYPE, registeredProcessor);
            manager.hideRecipes(CentrifugeCategory.TYPE, registeredCentrifuge);
            addDataRecipes(recipes, manager::addRecipes);
            registeredMap = recipes;
        } catch (Throwable t) {
            ReactorCraft.LOGGER.error("Failed to update ReactorCraft JEI recipes after datapack sync", t);
        }
    }

    // =========================================================================
    // Uranium Processor — two-step chemistry:
    //   input fluid + catalyst -> intermediate fluid; input item + intermediate -> output fluid
    // =========================================================================
    public static final class ProcessorCategory implements IRecipeCategory<ProcessorRecipe> {

        public static final RecipeType<ProcessorRecipe> TYPE =
                RecipeType.create(ReactorCraft.MODID, "processor", ProcessorRecipe.class);

        private final IDrawable icon;

        public ProcessorCategory(IGuiHelper gui) {
            this.icon = gui.createDrawableItemStack(new ItemStack(ReactorBlocks.PROCESSOR.get()));
        }

        @Override public RecipeType<ProcessorRecipe> getRecipeType() { return TYPE; }
        @Override public Component getTitle() { return Component.translatable("block.reactorcraft.uranium_processor"); }
        @Override public int getWidth()  { return 140; }
        @Override public int getHeight() { return 36; }
        @Override public IDrawable getIcon() { return icon; }

        @Override
        public void setRecipe(IRecipeLayoutBuilder builder, ProcessorRecipe recipe, IFocusGroup focuses) {
            builder.addSlot(RecipeIngredientRole.INPUT, 1, 9)
                   .addFluidStack(recipe.getInputFluid().getFluid(), recipe.getInputFluid().getAmount())
                   .addRichTooltipCallback((view, tooltip) -> tooltip.add(
                           Component.translatable("jei.reactorcraft.intermediate_time", recipe.getIntermediateTime())));
            builder.addSlot(RecipeIngredientRole.INPUT, 23, 9)
                   .addIngredients(recipe.getCatalyst());
            builder.addSlot(RecipeIngredientRole.INPUT, 45, 9)
                   .addIngredients(recipe.getInputItem());
            builder.addSlot(RecipeIngredientRole.INPUT, 78, 9)
                   .addFluidStack(recipe.getIntermediateFluid().getFluid(), recipe.getIntermediateFluid().getAmount())
                   .addRichTooltipCallback((view, tooltip) -> tooltip.add(
                           Component.translatable("jei.reactorcraft.intermediate_consumed",
                                   recipe.getIntermediateConsumed())));
            builder.addSlot(RecipeIngredientRole.OUTPUT, 122, 9)
                   .addFluidStack(recipe.getOutputFluid().getFluid(), recipe.getOutputFluid().getAmount())
                   .addRichTooltipCallback((view, tooltip) -> tooltip.add(
                           Component.translatable("jei.reactorcraft.output_time", recipe.getOutputTime())));
        }
    }

    // =========================================================================
    // Isotope Centrifuge — input fluid -> enriched (A) or depleted (B) by chance
    // =========================================================================
    public static final class CentrifugeCategory implements IRecipeCategory<CentrifugeRecipe> {

        public static final RecipeType<CentrifugeRecipe> TYPE =
                RecipeType.create(ReactorCraft.MODID, "centrifuge", CentrifugeRecipe.class);

        private final IDrawable icon;

        public CentrifugeCategory(IGuiHelper gui) {
            this.icon = gui.createDrawableItemStack(new ItemStack(ReactorBlocks.CENTRIFUGE.get()));
        }

        @Override public RecipeType<CentrifugeRecipe> getRecipeType() { return TYPE; }
        @Override public Component getTitle() { return Component.translatable("block.reactorcraft.isotope_centrifuge"); }
        @Override public int getWidth()  { return 98; }
        @Override public int getHeight() { return 36; }
        @Override public IDrawable getIcon() { return icon; }

        @Override
        public void setRecipe(IRecipeLayoutBuilder builder, CentrifugeRecipe recipe, IFocusGroup focuses) {
            builder.addSlot(RecipeIngredientRole.INPUT, 1, 9)
                   .addFluidStack(recipe.getInput().getFluid(), recipe.getInput().getAmount())
                   .addRichTooltipCallback((view, tooltip) -> tooltip.add(
                           Component.translatable("jei.reactorcraft.centrifuge_speed", recipe.getMinSpeed())));
            builder.addSlot(RecipeIngredientRole.OUTPUT, 58, 9)
                   .addItemStack(recipe.getOutputA())
                   .addRichTooltipCallback((view, tooltip) -> tooltip.add(
                           Component.translatable("jei.reactorcraft.output_chance",
                                   (int) recipe.getChanceOfAOverB())));
            ItemStack b = recipe.getOutputB();
            if (!b.isEmpty()) {
                builder.addSlot(RecipeIngredientRole.OUTPUT, 80, 9)
                       .addItemStack(b)
                       .addRichTooltipCallback((view, tooltip) -> tooltip.add(
                               Component.translatable("jei.reactorcraft.output_chance",
                                       (int) (100 - recipe.getChanceOfAOverB()))));
            }
        }
    }
}
