package reika.reactorcraft.world;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.chunk.ChunkGenerator;
import reika.dragonapi.libraries.level.LegacyOreVeins;
import reika.reactorcraft.registry.FluoriteTypes;
import reika.reactorcraft.registry.ReactorBlocks;
import reika.reactorcraft.registry.ReactorOreType;
import reika.reactorcraft.registry.ReactorOptions;

/**
 * Original discrete scatter, density/options, one fluorite color per admitted chunk and lava siting.
 */
public record ReactorOreFeature(ReactorOreConfig config) implements Feature {
    public static final MapCodec<ReactorOreFeature> CODEC = ReactorOreConfig.CODEC.xmap(ReactorOreFeature::new, ReactorOreFeature::config);

    @Override
    public MapCodec<ReactorOreFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel world, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        ReactorOreConfig cfg = config;
        ReactorOreType type = cfg.settingsKey().isEmpty() ? null : ReactorOreType.valueOf(cfg.settingsKey());
        if (type != null && !ReactorOreSettings.enabled(type)) return false;
        int discrete = Math.max(1, ReactorOptions.DISCRETE.getValue());
        if (random.nextInt(discrete) != 0) return false;
        boolean rainbow = cfg.randomFluorite() && ReactorOptions.RAINBOW.getState();
        int passes = ReactorOreSettings.passes(cfg.count(), ReactorOptions.getOreMultiplier(), discrete, rainbow);
        BlockState state = cfg.ore().defaultBlockState();
        if (cfg.randomFluorite()) {
            FluoriteTypes color = FluoriteTypes.colorList[random.nextInt(FluoriteTypes.colorList.length)];
            state = ReactorBlocks.fluoriteOre(rainbow ? FluoriteTypes.WHITE : color).defaultBlockState();
        }
        boolean placed = false;
        for (int i = 0; i < passes; i++) {
            int x = origin.getX() + random.nextInt(16), z = origin.getZ() + random.nextInt(16);
            int y = cfg.minY() + random.nextInt(cfg.maxY() - cfg.minY() + 1);
            if (cfg.needsLava() && !LegacyOreVeins.adjacentLava(world, new BlockPos(x, y, z))) continue;
            placed |= LegacyOreVeins.place(world, random, state, cfg.size(), cfg.dimType(), x, y, z);
        }
        return placed;
    }
}
