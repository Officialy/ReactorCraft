package reika.reactorcraft.modinterface.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.registry.ReactorBlocks;
import reika.reactorcraft.tileentities.processing.TileEntityElectrolyzer.Electrolysis;
import reika.reactorcraft.tileentities.processing.TileEntitySynthesizer.FluidSynthesis;
import reika.reactorcraft.tileentities.processing.TileEntityTritizer.Reactions;

/** JEI views backed by the existing fixed process lists in ReactorCraft's machines. */
public final class ReactorProcessJEICategories {
    private ReactorProcessJEICategories() {}

    private abstract static class ProcessCategory<T> implements IRecipeCategory<T> {
        private final RecipeType<T> type;
        private final Component title;
        private final IDrawable icon;

        ProcessCategory(IGuiHelper gui, RecipeType<T> type, Block block, String titleKey) {
            this.type = type;
            this.title = Component.translatable(titleKey);
            this.icon = gui.createDrawableItemStack(new ItemStack(block));
        }

        @Override public RecipeType<T> getRecipeType() { return type; }
        @Override public Component getTitle() { return title; }
        @Override public int getWidth() { return 126; }
        @Override public int getHeight() { return 42; }
        @Override public IDrawable getIcon() { return icon; }
    }

    public static final class Electrolyzer extends ProcessCategory<Electrolysis> {
        public static final RecipeType<Electrolysis> TYPE =
                RecipeType.create(ReactorCraft.MODID, "electrolyzer", Electrolysis.class);

        public Electrolyzer(IGuiHelper gui) {
            super(gui, TYPE, ReactorBlocks.ELECTROLYZER.get(), "block.reactorcraft.electrolyzer");
        }

        @Override
        public void setRecipe(IRecipeLayoutBuilder builder, Electrolysis recipe, IFocusGroup focuses) {
            if (!recipe.requiredFluid.isEmpty())
                builder.addSlot(RecipeIngredientRole.INPUT, 1, 12)
                        .addFluidStack(recipe.requiredFluid.getFluid(), recipe.requiredFluid.getAmount());
            if (recipe.hasItemRequirement())
                builder.addSlot(RecipeIngredientRole.INPUT, 25, 12)
                        .addItemStacks(java.util.List.copyOf(recipe.getItemListForDisplay()))
                        .addRichTooltipCallback((view, tooltip) -> tooltip.add(Component.translatable(
                                recipe.consumeItem ? "jei.reactorcraft.item_consumed" : "jei.reactorcraft.item_catalyst")));
            if (!recipe.upperOutput.isEmpty())
                builder.addSlot(RecipeIngredientRole.OUTPUT, 79, 1)
                        .addFluidStack(recipe.upperOutput.getFluid(), recipe.upperOutput.getAmount());
            if (!recipe.lowerOutput.isEmpty())
                builder.addSlot(RecipeIngredientRole.OUTPUT, 101, 20)
                        .addFluidStack(recipe.lowerOutput.getFluid(), recipe.lowerOutput.getAmount())
                        .addRichTooltipCallback((view, tooltip) -> {
                            if (recipe.requiredTemperature > 0)
                                tooltip.add(Component.translatable("jei.reactorcraft.min_temperature",
                                        recipe.requiredTemperature));
                        });
        }
    }

    public static final class Synthesizer extends ProcessCategory<FluidSynthesis> {
        public static final RecipeType<FluidSynthesis> TYPE =
                RecipeType.create(ReactorCraft.MODID, "synthesizer", FluidSynthesis.class);

        public Synthesizer(IGuiHelper gui) {
            super(gui, TYPE, ReactorBlocks.SYNTHESIZER.get(), "block.reactorcraft.synthesizer");
        }

        @Override
        public void setRecipe(IRecipeLayoutBuilder builder, FluidSynthesis recipe, IFocusGroup focuses) {
            builder.addSlot(RecipeIngredientRole.INPUT, 1, 12)
                    .addFluidStack(recipe.input, recipe.fluidConsumed);
            ItemStack a = recipe.getAForDisplay();
            ItemStack b = recipe.getBForDisplay();
            if (!a.isEmpty())
                builder.addSlot(RecipeIngredientRole.INPUT, 25, 12).addItemStack(a);
            if (!b.isEmpty())
                builder.addSlot(RecipeIngredientRole.INPUT, 47, 12).addItemStack(b);
            builder.addSlot(RecipeIngredientRole.OUTPUT, 101, 12)
                    .addFluidStack(recipe.output, recipe.fluidProduced)
                    .addRichTooltipCallback((view, tooltip) -> {
                        tooltip.add(Component.translatable("jei.reactorcraft.min_temperature", recipe.minTemp));
                        tooltip.add(Component.translatable("jei.reactorcraft.base_duration", recipe.baseDuration));
                    });
        }
    }

    public static final class Tritizer extends ProcessCategory<Reactions> {
        public static final RecipeType<Reactions> TYPE =
                RecipeType.create(ReactorCraft.MODID, "tritizer", Reactions.class);

        public Tritizer(IGuiHelper gui) {
            super(gui, TYPE, ReactorBlocks.TRITIZER.get(), "block.reactorcraft.tritizer");
        }

        @Override
        public void setRecipe(IRecipeLayoutBuilder builder, Reactions recipe, IFocusGroup focuses) {
            builder.addSlot(RecipeIngredientRole.INPUT, 1, 12)
                    .addFluidStack(recipe.input, recipe.amount);
            builder.addSlot(RecipeIngredientRole.OUTPUT, 101, 12)
                    .addFluidStack(recipe.output, recipe.amount)
                    .addRichTooltipCallback((view, tooltip) -> tooltip.add(Component.translatable(
                            "jei.reactorcraft.neutron_chance", recipe.chance)));
        }
    }
}
