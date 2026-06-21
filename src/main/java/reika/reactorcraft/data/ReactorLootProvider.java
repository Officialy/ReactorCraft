package reika.reactorcraft.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import reika.reactorcraft.registry.FluoriteTypes;
import reika.reactorcraft.registry.ReactorBlocks;
import reika.reactorcraft.registry.ReactorItems;
import reika.reactorcraft.registry.ReactorOreType;

/**
 * Block loot tables, faithful to the 1.7.10 {@code ReactorOres.getOreDrop}: ores that yielded their
 * material directly (calcite, ammonium, magnetite) and the fluorite ores (which dropped their gem)
 * drop the product item; the smeltable ores (pitchblende, cadmium, indium, silver, thorium,
 * endblende) and the storage blocks drop themselves.
 */
public final class ReactorLootProvider extends LootTableProvider {

    public ReactorLootProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, Set.of(), List.of(
                new SubProviderEntry(Blocks::new, LootContextParamSets.BLOCK)
        ), registries);
    }

    private static final class Blocks extends BlockLootSubProvider {

        Blocks(HolderLookup.Provider registries) {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
        }

        @Override
        protected void generate() {
            Map<Block, ItemLike> materialDrops = new HashMap<>();
            for (ReactorOreType ore : ReactorOreType.list) {
                if (ore == ReactorOreType.FLUORITE)
                    continue;
                if (ore.dropsMaterial)
                    materialDrops.put(ore.getBlock(), ore.getProduct());
            }
            for (FluoriteTypes f : FluoriteTypes.colorList) {
                materialDrops.put(ReactorBlocks.fluoriteOre(f), ReactorItems.fluorite(f));
            }

            for (var holder : ReactorBlocks.BLOCKS.getEntries()) {
                Block block = holder.get();
                ItemLike drop = materialDrops.get(block);
                if (drop != null)
                    this.dropOther(block, drop);
                else
                    this.dropSelf(block);
            }
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            List<Block> blocks = new ArrayList<>();
            for (var holder : ReactorBlocks.BLOCKS.getEntries())
                blocks.add(holder.get());
            return blocks;
        }
    }
}
