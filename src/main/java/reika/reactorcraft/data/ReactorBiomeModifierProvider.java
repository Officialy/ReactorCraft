package reika.reactorcraft.data;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.GenerationStep;

import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.registry.ReactorOreType;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/**
 * Generates {@code data/reactorcraft/neoforge/biome_modifier/*.json} (literal JSON, as biome tags
 * don't resolve in {@code RegistrySetBuilder}). Each ore's placed feature is added at the
 * {@code UNDERGROUND_ORES} step to the biomes that match the 1.7.10 {@code ReactorOres.isValidBiome}
 * / dimension rules: pitchblende only oceans/rivers, ammonium only the nether, endblende only the
 * end, everything else the whole overworld.
 */
public final class ReactorBiomeModifierProvider implements DataProvider {

    private final PackOutput.PathProvider pathProvider;

    public ReactorBiomeModifierProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "neoforge/biome_modifier");
    }

    private static void biomes(JsonObject json, ReactorOreType ore) {
        switch (ore) {
            case PITCHBLENDE: {
                JsonArray tags = new JsonArray();
                tags.add("#minecraft:is_ocean");
                tags.add("#minecraft:is_river");
                json.add("biomes", tags);
                break;
            }
            case AMMONIUM:
                json.addProperty("biomes", "#minecraft:is_nether");
                break;
            case ENDBLENDE:
                json.addProperty("biomes", "#minecraft:is_end");
                break;
            default:
                json.addProperty("biomes", "#minecraft:is_overworld");
                break;
        }
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        ImmutableList.Builder<CompletableFuture<?>> futures = ImmutableList.builder();
        for (ReactorOreType ore : ReactorOreType.list) {
            Identifier id = Identifier.fromNamespaceAndPath(ReactorCraft.MODID, ore.featureName());
            Path path = pathProvider.json(id);
            futures.add(CompletableFuture.supplyAsync(() -> {
                JsonObject json = new JsonObject();
                json.addProperty("type", "neoforge:add_features");
                biomes(json, ore);
                json.addProperty("features", id.toString());
                json.addProperty("step", GenerationStep.Decoration.UNDERGROUND_ORES.getName());
                return json;
            }).thenComposeAsync(encoded -> DataProvider.saveStable(cache, encoded, path)));
        }
        return CompletableFuture.allOf(futures.build().toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "ReactorCraft Biome Modifiers";
    }
}
