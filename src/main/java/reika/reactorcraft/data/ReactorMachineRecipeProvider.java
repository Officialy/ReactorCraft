package reika.reactorcraft.data;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.registry.FluoriteTypes;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/**
 * Emits the processor / centrifuge chemistry recipes ({@code ProcessorRecipe} / {@code CentrifugeRecipe})
 * as literal JSON, matching their {@code MapCodec} field layout. These cannot go through the normal
 * {@code RecipeProvider} path because constructing a {@link net.neoforged.neoforge.fluids.FluidStack}
 * during datagen triggers "Components not bound yet" (the fluid-stack analogue of the item-stack
 * datagen restriction the plan warns about). The values are transcribed verbatim from the 1.7.10
 * {@code TileEntityUProcessor.Processes.UF6} and {@code TileEntityCentrifuge.Centrifuging.UF6}.
 */
public final class ReactorMachineRecipeProvider implements DataProvider {

    private final PackOutput.PathProvider pathProvider;

    public ReactorMachineRecipeProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipe");
    }

    private static JsonObject fluid(String id, int amount) {
        JsonObject o = new JsonObject();
        o.addProperty("id", id);
        o.addProperty("amount", amount);
        return o;
    }

    private static JsonObject item(String id) {
        JsonObject o = new JsonObject();
        o.addProperty("id", id);
        return o;
    }

    private JsonObject processorUF6() {
        JsonObject json = new JsonObject();
        json.addProperty("type", ReactorCraft.MODID + ":processor");
        JsonArray catalyst = new JsonArray();
        for (FluoriteTypes f : FluoriteTypes.colorList)
            catalyst.add(ReactorCraft.MODID + ":" + f.getGemItemName());
        json.add("catalyst", catalyst);
        json.addProperty("input_item", ReactorCraft.MODID + ":uranium_ingot");
        json.add("input_fluid", fluid("minecraft:water", 250));
        json.add("intermediate_fluid", fluid(ReactorCraft.MODID + ":hydrofluoric_acid", 250));
        json.addProperty("intermediate_consumed", 125);
        json.add("output_fluid", fluid(ReactorCraft.MODID + ":uranium_hexafluoride", 1000));
        json.addProperty("intermediate_time", 80);
        json.addProperty("output_time", 400);
        return json;
    }

    private JsonObject centrifugeUF6() {
        JsonObject json = new JsonObject();
        json.addProperty("type", ReactorCraft.MODID + ":centrifuge");
        json.add("input", fluid(ReactorCraft.MODID + ":uranium_hexafluoride", 50));
        json.add("output_a", item(ReactorCraft.MODID + ":fuel_dust"));
        json.add("output_b", item(ReactorCraft.MODID + ":depleted_dust"));
        json.addProperty("chance_a_over_b", 9.0);
        json.addProperty("min_speed", 262144);
        json.addProperty("speed_factor", 1);
        return json;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        ImmutableList.Builder<CompletableFuture<?>> futures = ImmutableList.builder();
        futures.add(save(cache, "processor/uf6", processorUF6()));
        futures.add(save(cache, "centrifuge/uf6", centrifugeUF6()));
        return CompletableFuture.allOf(futures.build().toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<?> save(CachedOutput cache, String path, JsonObject json) {
        Path file = pathProvider.json(Identifier.fromNamespaceAndPath(ReactorCraft.MODID, path));
        return DataProvider.saveStable(cache, json, file);
    }

    @Override
    public String getName() {
        return "ReactorCraft Machine Recipes";
    }
}
