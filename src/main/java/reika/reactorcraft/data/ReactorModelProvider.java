package reika.reactorcraft.data;

import java.lang.reflect.Field;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ItemModelOutput;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.registry.ReactorBlocks;
import reika.reactorcraft.registry.ReactorItems;

/**
 * Block / item model + blockstate provider. Uses the same reflective sink-access pattern as the
 * RotaryCraft / GeoStrata providers (vanilla keeps the single-block helpers private): every block
 * gets a {@code cube_all} model + single-variant blockstate, every item a flat {@code generated}
 * model. Textures themselves (block/&lt;name&gt;.png, item/&lt;name&gt;.png) are authored separately;
 * datagen only writes the model JSON.
 */
public class ReactorModelProvider extends ModelProvider {

    public ReactorModelProvider(PackOutput output) {
        super(output, ReactorCraft.MODID);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        Consumer<BlockModelDefinitionGenerator> blockStateOut;
        ItemModelOutput itemModelOut;
        BiConsumer<Identifier, ModelInstance> modelOut;
        try {
            Field bsf = BlockModelGenerators.class.getDeclaredField("blockStateOutput");
            bsf.setAccessible(true);
            blockStateOut = (Consumer<BlockModelDefinitionGenerator>) bsf.get(blockModels);

            Field imf = BlockModelGenerators.class.getDeclaredField("itemModelOutput");
            imf.setAccessible(true);
            itemModelOut = (ItemModelOutput) imf.get(blockModels);

            Field mof = BlockModelGenerators.class.getDeclaredField("modelOutput");
            mof.setAccessible(true);
            modelOut = (BiConsumer<Identifier, ModelInstance>) mof.get(blockModels);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "Failed to reflectively access BlockModelGenerators sinks — vanilla shape changed?", e);
        }

        java.util.Set<Item> blockItemsHandled = new java.util.HashSet<>();

        for (var holder : ReactorBlocks.BLOCKS.getEntries()) {
            Block block = holder.get();
            Identifier blockModelId = ModelTemplates.CUBE_ALL.create(block, TextureMapping.cube(block), modelOut);
            MultiVariant single = new MultiVariant(WeightedList.of(new Variant(blockModelId)));
            blockStateOut.accept(MultiVariantGenerator.dispatch(block, single));

            Item asItem = block.asItem();
            if (asItem != Items.AIR) {
                itemModelOut.accept(asItem, ItemModelUtils.plainModel(blockModelId));
                blockItemsHandled.add(asItem);
            }
        }

        for (var holder : ReactorBlocks.ITEMS.getEntries()) {
            Item item = holder.get();
            if (blockItemsHandled.contains(item)) continue;
            flatItem(item, modelOut, itemModelOut);
        }

        for (var holder : ReactorItems.ITEMS.getEntries()) {
            flatItem(holder.get(), modelOut, itemModelOut);
        }
    }

    private static void flatItem(Item item, BiConsumer<Identifier, ModelInstance> modelOut, ItemModelOutput itemModelOut) {
        Identifier itemModelId = ModelTemplates.FLAT_ITEM.create(
                ModelLocationUtils.getModelLocation(item),
                TextureMapping.layer0(item),
                modelOut);
        itemModelOut.accept(item, ItemModelUtils.plainModel(itemModelId));
    }

    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        return BuiltInRegistries.BLOCK.listElements()
                .filter(h -> h.getKey().identifier().getNamespace().equals(ReactorCraft.MODID));
    }

    @Override
    protected Stream<? extends Holder<Item>> getKnownItems() {
        return BuiltInRegistries.ITEM.listElements()
                .filter(h -> h.getKey().identifier().getNamespace().equals(ReactorCraft.MODID));
    }
}
