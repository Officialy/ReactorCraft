package reika.reactorcraft.data;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.registry.FluoriteTypes;
import reika.reactorcraft.registry.MatBlocks;
import reika.reactorcraft.registry.ReactorBlocks;
import reika.reactorcraft.registry.ReactorOreType;
import reika.dragonapi.libraries.level.LegacyMotionTags;

/**
 * Harvest tags for every block that mines with a tool. Without membership in {@code mineable/pickaxe}
 * (+ the correct tier) a {@code requiresCorrectToolForDrops()} block drops nothing in survival, so the
 * whole set is driven off that flag; the per-ore tier follows the original {@code ReactorOres} harvest
 * levels (0 = any, 1 = stone, 2 = iron). Ducts/lines and the technical blocks (steam, thorium fuel) set
 * no tool requirement and are correctly skipped.
 */
public class ReactorBlockTagsProvider extends BlockTagsProvider {

    public ReactorBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup, ReactorCraft.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for (var ore : ReactorOreType.list) {
            String material = ore == ReactorOreType.ENDBLENDE ? "pitchblende" : ore.name().toLowerCase(java.util.Locale.ROOT);
            var key = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK,
                    net.minecraft.resources.Identifier.fromNamespaceAndPath("c", "ores/" + material));
            if (ore == ReactorOreType.FLUORITE) {
                for (var color : FluoriteTypes.colorList)
                    tag(key).add(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getResourceKey(ReactorBlocks.fluoriteOre(color)).orElseThrow());
            } else tag(key).add(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getResourceKey(ore.getBlock()).orElseThrow());
            tag(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK,
                    net.minecraft.resources.Identifier.fromNamespaceAndPath("c", "ores"))).addTag(key);
        }
        // 26.3 made movement blocking, suffocation, fluid blocking and fluid washing tag-driven and
        // NeoForge tags no modded blocks; give every block its 26.2 behaviour (see LegacyMotionTags).
        var motionTag = tag(BlockTags.BLOCKS_MOTION_NO_LEAVES);
        var leafTag = tag(BlockTags.LEAVES);
        var washedTag = tag(BlockTags.WASHED_AWAY_BY_FLUIDS);
        // 1.7.10 ReactorBlocks#getBlockMaterial: rock for ores and materials, lava for corium, iron for everything else,
        // so every non-fluid block blocked movement, stopped fluids and was never washed away.
        LegacyMotionTags.classifyEntries(ReactorBlocks.BLOCKS.getEntries(), block -> !(block instanceof LiquidBlock),
                motionTag::add, leafTag::add, washedTag::add);

        Map<Block, Integer> tier = new HashMap<>();
        for (ReactorOreType ore : ReactorOreType.list) {
            if (ore == ReactorOreType.FLUORITE)
                continue;
            tier.put(ore.getBlock(), oreTier(ore));
        }
        for (FluoriteTypes f : FluoriteTypes.colorList) {
            tier.put(ReactorBlocks.fluoriteOre(f), 0);
            tier.put(ReactorBlocks.fluoriteBlock(f), 0);
        }
        for (MatBlocks m : MatBlocks.matList)
            tier.put(ReactorBlocks.matBlock(m), 1);

        var pickaxe = tag(BlockTags.MINEABLE_WITH_PICKAXE);
        var stone = tag(BlockTags.NEEDS_STONE_TOOL);
        var iron = tag(BlockTags.NEEDS_IRON_TOOL);

        for (var holder : ReactorBlocks.BLOCKS.getEntries()) {
            Block block = holder.get();
            if (!block.defaultBlockState().requiresCorrectToolForDrops())
                continue;
            pickaxe.add(holder.getKey());
            int t = tier.getOrDefault(block, 2); // machines / multiblock casings / corium default to iron
            if (t == 1)
                stone.add(holder.getKey());
            else if (t >= 2)
                iron.add(holder.getKey());
        }
    }

    private static int oreTier(ReactorOreType ore) {
        return switch (ore) {
            case CADMIUM, INDIUM, SILVER, THORIUM -> 2;
            case PITCHBLENDE, MAGNETITE, AMMONIUM, ENDBLENDE -> 1;
            case CALCITE -> 0;
            default -> 0;
        };
    }
}
