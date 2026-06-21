package reika.reactorcraft.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.auxiliary.recipe.CentrifugeRecipe;
import reika.reactorcraft.auxiliary.recipe.ProcessorRecipe;

public class ReactorRecipeSerializers {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, ReactorCraft.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ProcessorRecipe>> PROCESSOR =
            RECIPE_SERIALIZERS.register("processor",
                    () -> new RecipeSerializer<>(ProcessorRecipe.CODEC, ProcessorRecipe.STREAM_CODEC));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CentrifugeRecipe>> CENTRIFUGE =
            RECIPE_SERIALIZERS.register("centrifuge",
                    () -> new RecipeSerializer<>(CentrifugeRecipe.CODEC, CentrifugeRecipe.STREAM_CODEC));
}
