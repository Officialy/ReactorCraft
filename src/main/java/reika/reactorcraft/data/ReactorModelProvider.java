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
import net.minecraft.client.resources.model.sprite.Material;
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
            Identifier blockModelId = ModelTemplates.CUBE_ALL.create(block, TextureMapping.cube(blockTexture(block)), modelOut);
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

    // Registry block id -> texture path under assets/reactorcraft/textures/ (the 1.7.10 art lives in
    // blocks/{ore,mat,...} with old names). Visible blocks (ores/storage/fluorite) map to their real
    // texture; BER-rendered machines whose only art is a tileentity model use a single side texture
    // or fall back to "blocks/steel". Multi-texture machines ("_#" variants) use the base #0 sprite.
    private static final java.util.Map<String, String> BLOCK_TEX = new java.util.HashMap<>();
    static {
        // ores
        BLOCK_TEX.put("pitchblende_ore", "blocks/ore/pitchblende");
        BLOCK_TEX.put("end_pitchblende_ore", "blocks/ore/endblende");
        BLOCK_TEX.put("cadmium_ore", "blocks/ore/cadmium");
        BLOCK_TEX.put("indium_ore", "blocks/ore/indium");
        BLOCK_TEX.put("silver_ore", "blocks/ore/silver");
        BLOCK_TEX.put("ammonium_ore", "blocks/ore/ammonium");
        BLOCK_TEX.put("calcite_ore", "blocks/ore/calcite");
        BLOCK_TEX.put("magnetite_ore", "blocks/ore/magnetite");
        BLOCK_TEX.put("thorium_ore", "blocks/ore/thorium");
        // storage
        BLOCK_TEX.put("graphite_block", "blocks/mat/graphite");
        BLOCK_TEX.put("calcite_block", "blocks/mat/calcite");
        BLOCK_TEX.put("lodestone_block", "blocks/mat/lodestone");
        // single-texture machines (registry id differs from texture name)
        BLOCK_TEX.put("control_rod", "blocks/control");
        BLOCK_TEX.put("reactor_cpu", "blocks/cpu");
        BLOCK_TEX.put("neutron_absorber", "blocks/absorber");
        BLOCK_TEX.put("breeder_core", "blocks/breeder");
        BLOCK_TEX.put("heat_exchanger", "blocks/exchanger");
        BLOCK_TEX.put("fusion_heater", "blocks/heater");
        BLOCK_TEX.put("fusion_injector", "blocks/injector");
        BLOCK_TEX.put("synthesizer", "blocks/synthesizer");
        BLOCK_TEX.put("tritizer", "blocks/tritizer");
        BLOCK_TEX.put("waste_container", "blocks/wastecontainer");
        BLOCK_TEX.put("neutron_reflector", "blocks/reflector");
        BLOCK_TEX.put("fuel_dump", "blocks/fueldump");
        BLOCK_TEX.put("fuel_rod", "blocks/fuel_0");
        BLOCK_TEX.put("coolant_cell", "blocks/coolant_0");
        BLOCK_TEX.put("reactor_boiler", "blocks/boiler_0");
        BLOCK_TEX.put("thorium_core", "blocks/thorium_0");
        BLOCK_TEX.put("sodium_boiler", "blocks/sodiumboiler_0");
        BLOCK_TEX.put("pebble_bed", "blocks/pebblebed_0");
        BLOCK_TEX.put("co2_heater", "blocks/co2heater_0");
        BLOCK_TEX.put("turbine_meter", "blocks/turbinemeter_0");
        BLOCK_TEX.put("waste_decayer", "blocks/wastedecayer_0");
        // fluid-ish technical blocks
        BLOCK_TEX.put("thorium_fuel", "blocks/fluid/lifbe_fuel");
        BLOCK_TEX.put("steam", "blocks/steam");
    }

    private static Material blockTexture(Block block) {
        String id = BuiltInRegistries.BLOCK.getKey(block).getPath();
        String path = BLOCK_TEX.get(id);
        if (path == null) {
            if (id.endsWith("_fluorite_ore")) // fluorite ores: <color>_fluorite_ore -> ore/fluorite_<color>
                path = "blocks/ore/fluorite_" + id.substring(0, id.length() - "_fluorite_ore".length());
            else // BER-rendered machines / unmapped: a generic steel casing so they aren't missing-texture
                path = "blocks/steel";
        }
        return new Material(Identifier.fromNamespaceAndPath(ReactorCraft.MODID, path));
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
