package reika.reactorcraft.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/**
 * Configuration for {@link ReactorOreFeature}, one instance per ore type. Transcribes the gameplay
 * data the 1.7.10 {@code ReactorOres} enum carried per ore (vein size, veins-per-chunk, Y band) into
 * a datapack-serializable feature config. {@code dimType} selects the replaceable base-stone set
 * (0 = overworld stone/deepslate, 1 = nether netherrack, 2 = end stone); {@code randomFluorite}
 * makes each vein pick a random fluorite colour block (the 1.7.10 per-vein colour roll);
 * {@code needsLava} reproduces the ammonium lava-adjacency requirement.
 */
public record ReactorOreConfig(Block ore, int count, int size, int minY, int maxY, int dimType,
                               boolean randomFluorite, boolean needsLava) implements FeatureConfiguration {

    public static final MapCodec<ReactorOreConfig> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("ore").forGetter(ReactorOreConfig::ore),
            Codec.INT.fieldOf("count").forGetter(ReactorOreConfig::count),
            Codec.INT.fieldOf("size").forGetter(ReactorOreConfig::size),
            Codec.INT.fieldOf("min_y").forGetter(ReactorOreConfig::minY),
            Codec.INT.fieldOf("max_y").forGetter(ReactorOreConfig::maxY),
            Codec.INT.fieldOf("dim_type").forGetter(ReactorOreConfig::dimType),
            Codec.BOOL.fieldOf("random_fluorite").forGetter(ReactorOreConfig::randomFluorite),
            Codec.BOOL.fieldOf("needs_lava").forGetter(ReactorOreConfig::needsLava)
    ).apply(inst, ReactorOreConfig::new));
}
