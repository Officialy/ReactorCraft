package reika.reactorcraft.data;

import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.registry.ReactorOreType;
import reika.reactorcraft.world.ReactorOreConfig;
import reika.reactorcraft.world.ReactorOreFeature;

import java.util.List;

/**
 * 26.2 worldgen {@link RegistrySetBuilder} for ReactorCraft ores. Emits one configured feature per
 * {@link ReactorOreType} (carrying the vein size / count and the 26.2-remapped Y band — deeper
 * indium/thorium below the old 0 floor, magnetite up the taller mountains) and a matching placed
 * feature with no placement modifiers, because {@link reika.reactorcraft.world.ReactorOreFeature}
 * scatters within the chunk itself (the 1.7.10 {@code BasicReactorOreGenerator} behaviour). Biome
 * targeting (pitchblende oceans/rivers, ammonium nether, endblende end) is applied separately by
 * {@link ReactorBiomeModifierProvider} since {@code RegistrySetBuilder} cannot resolve biome tags.
 */
public final class ReactorWorldGenProvider {

    private ReactorWorldGenProvider() {}

    /** 26.2-remapped [minY, maxY] per ore (plan Y-range table); falls back to the original band. */
    static int[] yBand(ReactorOreType ore) {
        return switch (ore) {
            case INDIUM -> new int[]{-32, 16};
            case MAGNETITE -> new int[]{60, 292};
            default -> new int[]{ore.minY, ore.maxY};
        };
    }

    static int dimType(ReactorOreType ore) {
        return switch (ore.dimension) {
            case NETHER -> 1;
            case END -> 2;
            default -> 0;
        };
    }

    static ReactorOreConfig config(ReactorOreType ore) {
        int[] y = yBand(ore);
        return new ReactorOreConfig(ore.getBlock(), ore.perChunk, ore.veinSize, y[0], y[1],
                dimType(ore), ore == ReactorOreType.FLUORITE, ore == ReactorOreType.AMMONIUM, ore.name());
    }

    public static RegistrySetBuilder buildRegistrySet() {
        RegistrySetBuilder builder = new RegistrySetBuilder();

        builder.add(Registries.FEATURE, bootstrap -> {
            for (ReactorOreType ore : ReactorOreType.list) {
                Identifier id = Identifier.fromNamespaceAndPath(ReactorCraft.MODID, ore.featureName());
                ResourceKey<Feature> key = ResourceKey.create(Registries.FEATURE, id);
                bootstrap.register(key, new ReactorOreFeature(config(ore)));
            }
        });

        builder.add(Registries.PLACED_FEATURE, bootstrap -> {
            var features = bootstrap.lookup(Registries.FEATURE);
            for (ReactorOreType ore : ReactorOreType.list) {
                Identifier id = Identifier.fromNamespaceAndPath(ReactorCraft.MODID, ore.featureName());
                ResourceKey<PlacedFeature> key = ResourceKey.create(Registries.PLACED_FEATURE, id);
                ResourceKey<Feature> featureKey = ResourceKey.create(Registries.FEATURE, id);
                PlacedFeature placed = new PlacedFeature(features.getOrThrow(featureKey), List.of());
                bootstrap.register(key, placed);
            }
        });

        return builder;
    }
}
