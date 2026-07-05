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
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.auxiliary.recipe.CentrifugeRecipe;
import reika.reactorcraft.auxiliary.recipe.ProcessorRecipe;
import reika.reactorcraft.registry.ReactorBlocks;
import reika.reactorcraft.registry.ReactorRecipeTypes;

/**
 * JEI integration for the ReactorCraft processing machines: the Uranium Processor's two-step
 * fluid chemistry and the Isotope Centrifuge's enrichment split.
 */
@JeiPlugin
public class ReactorJEIPlugin implements IModPlugin {

    public static final Identifier UID = Identifier.fromNamespaceAndPath(ReactorCraft.MODID, "jei_plugin");

    @Override
    public Identifier getPluginUid() { return UID; }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper gui = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new ProcessorCategory(gui),
                new CentrifugeCategory(gui)
        );
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ReactorBlocks.PROCESSOR.get()), ProcessorCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ReactorBlocks.CENTRIFUGE.get()), CentrifugeCategory.TYPE);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        try {
            MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
            if (server == null) return; // dedicated client — skip
            var rm = server.getRecipeManager();

            List<ProcessorRecipe> processor = rm.recipeMap()
                    .byType(ReactorRecipeTypes.PROCESSOR.get())
                    .stream().map(RecipeHolder::value).collect(Collectors.toList());
            registration.addRecipes(ProcessorCategory.TYPE, processor);

            List<CentrifugeRecipe> centrifuge = rm.recipeMap()
                    .byType(ReactorRecipeTypes.CENTRIFUGE.get())
                    .stream().map(RecipeHolder::value).collect(Collectors.toList());
            registration.addRecipes(CentrifugeCategory.TYPE, centrifuge);

        } catch (Throwable t) {
            ReactorCraft.LOGGER.error("Failed to register ReactorCraft JEI recipes", t);
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
                   .addFluidStack(recipe.getInputFluid().getFluid(), recipe.getInputFluid().getAmount());
            builder.addSlot(RecipeIngredientRole.INPUT, 23, 9)
                   .addIngredients(recipe.getCatalyst());
            builder.addSlot(RecipeIngredientRole.INPUT, 45, 9)
                   .addIngredients(recipe.getInputItem());
            builder.addSlot(RecipeIngredientRole.INPUT, 78, 9)
                   .addFluidStack(recipe.getIntermediateFluid().getFluid(), recipe.getIntermediateFluid().getAmount())
                   .addRichTooltipCallback((view, tooltip) -> tooltip.add(
                           Component.literal("Intermediate (" + recipe.getIntermediateConsumed() + " mB consumed per step)")));
            builder.addSlot(RecipeIngredientRole.OUTPUT, 122, 9)
                   .addFluidStack(recipe.getOutputFluid().getFluid(), recipe.getOutputFluid().getAmount());
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
                   .addFluidStack(recipe.getInput().getFluid(), recipe.getInput().getAmount());
            builder.addSlot(RecipeIngredientRole.OUTPUT, 58, 9)
                   .addItemStack(recipe.getOutputA())
                   .addRichTooltipCallback((view, tooltip) -> tooltip.add(
                           Component.literal((int) recipe.getChanceOfAOverB() + "% chance")));
            ItemStack b = recipe.getOutputB();
            if (!b.isEmpty()) {
                builder.addSlot(RecipeIngredientRole.OUTPUT, 80, 9)
                       .addItemStack(b)
                       .addRichTooltipCallback((view, tooltip) -> tooltip.add(
                               Component.literal((int) (100 - recipe.getChanceOfAOverB()) + "% chance")));
            }
        }
    }
}
