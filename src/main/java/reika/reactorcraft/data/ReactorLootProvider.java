package reika.reactorcraft.data;

import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import reika.reactorcraft.registry.FluoriteTypes;
import reika.reactorcraft.registry.ReactorBlocks;
import reika.reactorcraft.registry.ReactorItems;
import reika.reactorcraft.registry.ReactorOreType;

import java.util.*;

/**
 * Block loot tables, faithful to the 1.7.10 {@code ReactorOres.getOreDrop}: ores that yielded their
 * material directly (calcite, ammonium, magnetite) and the fluorite ores (which dropped their gem)
 * drop the product item; the smeltable ores (pitchblende, cadmium, indium, silver, thorium,
 * endblende) and the storage blocks drop themselves.
 */
public final class ReactorLootProvider extends LootTableProvider {

    public ReactorLootProvider() {
        super(Set.of(), List.of(
                new SubProviderEntry(Blocks::new, LootContextParamSets.BLOCK)
        ));
    }

    private static final class Blocks extends BlockLootSubProvider {

        Blocks(LootTableSubProvider.Context context) {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags(), context);
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
                if (block.getLootTable().isEmpty())
                    continue;
                if (block == ReactorBlocks.AMMONIUM_ORE.get()) {
                    // Original drops the dust plus a piece of netherrack.
                    this.add(block, LootTable.lootTable()
                            .withPool(this.applyExplosionCondition(block, LootPool.lootPool().setRolls(ContextIntProviders.exactly(1))
                                    .add(LootItem.lootTableItem(ReactorItems.AMMONIUM_DUST.get()))))
                            .withPool(this.applyExplosionCondition(block, LootPool.lootPool().setRolls(ContextIntProviders.exactly(1))
                                    .add(LootItem.lootTableItem(net.minecraft.world.level.block.Blocks.NETHERRACK)))));
                    continue;
                }
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
