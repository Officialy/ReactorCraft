package reika.reactorcraft.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.auxiliary.recipe.CentrifugeRecipe;
import reika.reactorcraft.auxiliary.recipe.ProcessorRecipe;

public class ReactorRecipeTypes {

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, ReactorCraft.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<ProcessorRecipe>> PROCESSOR =
            RECIPE_TYPES.register("processor", () -> new RecipeType<ProcessorRecipe>() {});

    public static final DeferredHolder<RecipeType<?>, RecipeType<CentrifugeRecipe>> CENTRIFUGE =
            RECIPE_TYPES.register("centrifuge", () -> new RecipeType<CentrifugeRecipe>() {});
}
