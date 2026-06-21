package reika.reactorcraft.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

import reika.reactorcraft.registry.FluoriteTypes;
import reika.reactorcraft.registry.ReactorBlocks;

/**
 * Ports the per-chunk vein scatter of the 1.7.10 {@code BasicReactorOreGenerator}: for one ore type
 * (carried by {@link ReactorOreConfig}) it drops {@code count} veins inside the chunk, each an
 * ellipsoidal blob of {@code size} blocks (the vanilla {@code WorldGenMinable} algorithm) within the
 * configured Y band, replacing the dimension's base stone. Biome restriction (pitchblende oceans,
 * ammonium nether, endblende end) is applied by the biome modifiers that reference the placed
 * feature, so it is intentionally absent here.
 */
public class ReactorOreFeature extends Feature<ReactorOreConfig> {

    public ReactorOreFeature() {
        super(ReactorOreConfig.CODEC.codec());
    }

    @Override
    public boolean place(FeaturePlaceContext<ReactorOreConfig> context) {
        WorldGenLevel world = context.level();
        RandomSource rand = context.random();
        BlockPos origin = context.origin();
        ReactorOreConfig cfg = context.config();
        int baseX = origin.getX();
        int baseZ = origin.getZ();
        int span = cfg.maxY() - cfg.minY() + 1;
        boolean placedAny = false;
        for (int i = 0; i < cfg.count(); i++) {
            int x = baseX + rand.nextInt(16);
            int z = baseZ + rand.nextInt(16);
            int y = cfg.minY() + rand.nextInt(Math.max(1, span));
            if (cfg.needsLava() && !this.hasAdjacentLava(world, x, y, z))
                continue;
            BlockState state = this.veinState(cfg, rand);
            if (this.placeVein(world, rand, cfg, state, x, y, z))
                placedAny = true;
        }
        return placedAny;
    }

    private BlockState veinState(ReactorOreConfig cfg, RandomSource rand) {
        if (cfg.randomFluorite()) {
            FluoriteTypes color = FluoriteTypes.colorList[rand.nextInt(FluoriteTypes.colorList.length)];
            return ReactorBlocks.fluoriteOre(color).defaultBlockState();
        }
        return cfg.ore().defaultBlockState();
    }

    private boolean hasAdjacentLava(WorldGenLevel world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        for (Direction dir : Direction.values()) {
            if (world.getBlockState(pos.relative(dir)).is(Blocks.LAVA))
                return true;
        }
        return false;
    }

    private boolean canReplace(BlockState state, int dimType) {
        switch (dimType) {
            case 1:
                return state.is(Blocks.NETHERRACK) || state.is(BlockTags.BASE_STONE_NETHER);
            case 2:
                return state.is(Blocks.END_STONE);
            default:
                return state.is(BlockTags.STONE_ORE_REPLACEABLES) || state.is(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
        }
    }

    private boolean placeVein(WorldGenLevel world, RandomSource rand, ReactorOreConfig cfg, BlockState ore, int x, int y, int z) {
        int size = cfg.size();
        int placed = 0;
        float f = rand.nextFloat() * (float) Math.PI;
        double d0 = x + 8 + Mth.sin(f) * size / 8.0F;
        double d1 = x + 8 - Mth.sin(f) * size / 8.0F;
        double d2 = z + 8 + Mth.cos(f) * size / 8.0F;
        double d3 = z + 8 - Mth.cos(f) * size / 8.0F;
        double d4 = y + rand.nextInt(3) - 2;
        double d5 = y + rand.nextInt(3) - 2;
        for (int l = 0; l <= size; ++l) {
            double d6 = d0 + (d1 - d0) * l / size;
            double d7 = d4 + (d5 - d4) * l / size;
            double d8 = d2 + (d3 - d2) * l / size;
            double d9 = rand.nextDouble() * size / 16.0D;
            double d10 = (Mth.sin(l * (float) Math.PI / size) + 1.0F) * d9 + 1.0D;
            double d11 = (Mth.sin(l * (float) Math.PI / size) + 1.0F) * d9 + 1.0D;
            int i1 = Mth.floor(d6 - d10 / 2.0D);
            int j1 = Mth.floor(d7 - d11 / 2.0D);
            int k1 = Mth.floor(d8 - d10 / 2.0D);
            int l1 = Mth.floor(d6 + d10 / 2.0D);
            int i2 = Mth.floor(d7 + d11 / 2.0D);
            int j2 = Mth.floor(d8 + d10 / 2.0D);
            for (int dx = i1; dx <= l1; dx++) {
                double d12 = (dx + 0.5D - d6) / (d10 / 2.0D);
                if (d12 * d12 >= 1.0D)
                    continue;
                for (int dy = j1; dy <= i2; dy++) {
                    double d13 = (dy + 0.5D - d7) / (d11 / 2.0D);
                    if (d12 * d12 + d13 * d13 >= 1.0D)
                        continue;
                    for (int dz = k1; dz <= j2; dz++) {
                        double d14 = (dz + 0.5D - d8) / (d10 / 2.0D);
                        if (d12 * d12 + d13 * d13 + d14 * d14 >= 1.0D)
                            continue;
                        BlockPos pos = new BlockPos(dx, dy, dz);
                        if (this.canReplace(world.getBlockState(pos), cfg.dimType())) {
                            world.setBlock(pos, ore, 2);
                            placed++;
                        }
                    }
                }
            }
        }
        return placed > 0;
    }
}
