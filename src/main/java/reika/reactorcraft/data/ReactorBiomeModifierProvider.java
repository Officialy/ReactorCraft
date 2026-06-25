package reika.reactorcraft.data;

import com.google.common.collect.ImmutableList;
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

    /**
     * The biome tags each ore generates in. A {@code HolderSet} JSON only accepts a SINGLE tag string
     * (or an explicit list of biome ids) — it cannot take an array of {@code #tag} strings — so an ore
     * spanning two tags (pitchblende: oceans + rivers) emits one biome modifier per tag.
     */
    private static String[] biomeTags(ReactorOreType ore) {
        switch (ore) {
            case PITCHBLENDE:
                return new String[]{"#minecraft:is_ocean", "#minecraft:is_river"};
            case AMMONIUM:
                return new String[]{"#minecraft:is_nether"};
            case ENDBLENDE:
                return new String[]{"#minecraft:is_end"};
            default:
                return new String[]{"#minecraft:is_overworld"};
        }
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        ImmutableList.Builder<CompletableFuture<?>> futures = ImmutableList.builder();
        for (ReactorOreType ore : ReactorOreType.list) {
            String featureId = Identifier.fromNamespaceAndPath(ReactorCraft.MODID, ore.featureName()).toString();
            String[] tags = biomeTags(ore);
            for (int i = 0; i < tags.length; i++) {
                String tag = tags[i];
                // unique modifier id per tag; the feature added is always the ore's placed feature
                String modName = tags.length > 1 ? ore.featureName() + "_" + i : ore.featureName();
                Path path = pathProvider.json(Identifier.fromNamespaceAndPath(ReactorCraft.MODID, modName));
                futures.add(CompletableFuture.supplyAsync(() -> {
                    JsonObject json = new JsonObject();
                    json.addProperty("type", "neoforge:add_features");
                    json.addProperty("biomes", tag);
                    json.addProperty("features", featureId);
                    json.addProperty("step", GenerationStep.Decoration.UNDERGROUND_ORES.getName());
                    return json;
                }).thenComposeAsync(encoded -> DataProvider.saveStable(cache, encoded, path)));
            }
        }
        return CompletableFuture.allOf(futures.build().toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "ReactorCraft Biome Modifiers";
    }
}
