package reika.reactorcraft.data;

import java.lang.reflect.Field;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.google.gson.JsonObject;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ItemModelOutput;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
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

import reika.dragonapi.base.BlockMultiBlock;
import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.blocks.BlockReactorMachineModelled;
import reika.reactorcraft.blocks.multi.BlockSolenoidCasing;
import reika.reactorcraft.blocks.multi.BlockSolenoidCasing.SolenoidPart;
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

    // BER-rendered machine blocks whose ITEM icon should use the 3D model via the
    // reactorcraft:machine special renderer (see ReactorMachineItemRenderer). Keyed by block path.
    private static final Set<String> MACHINE_ITEM_MODELS = Set.of(
            "control_rod", "toroid_magnet", "solenoid_magnet", "steam_grate", "condenser", "turbine_core",
            "waste_storage", "electrolyzer", "solar_exchanger");

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

        Set<Item> blockItemsHandled = new HashSet<>();

        // Assembled solenoid casings render invisible (legacy: formed meta swapped to solenoid_10, a
        // fully-transparent sprite -- the completed solenoid hides its casing so the coil shows). One
        // shared model for every casing block's FORMED=true state; emitted once to avoid a duplicate.
        Identifier solenoidFormedModelId = Identifier.fromNamespaceAndPath(ReactorCraft.MODID, "block/solenoid_formed");
        modelOut.accept(solenoidFormedModelId, () -> cubeAllModel("reactorcraft:block/multi/solenoid_10"));
        MultiVariant solenoidFormedVariant = new MultiVariant(WeightedList.of(new Variant(solenoidFormedModelId)));

        for (var holder : ReactorBlocks.BLOCKS.getEntries()) {
            Block block = holder.get();

            if (block instanceof reika.reactorcraft.blocks.BlockSteam) {
                // Steam renders as a translucent cube (legacy render pass 1). A plain cube_all would land
                // on the solid/cutout layer and read as an opaque block; emit a cube model with
                // render_type translucent so the steam texture's alpha shows as wispy gas. No item (steam
                // is registerNoItem).
                Identifier steamModelId = ModelLocationUtils.getModelLocation(block);
                String steamTex = blockTexture(block).sprite().toString();
                modelOut.accept(steamModelId, () -> {
                    JsonObject root = new JsonObject();
                    root.addProperty("parent", "minecraft:block/cube_all");
                    root.addProperty("render_type", "minecraft:translucent");
                    JsonObject tex = new JsonObject();
                    tex.addProperty("all", steamTex);
                    root.add("textures", tex);
                    return root;
                });
                blockStateOut.accept(MultiVariantGenerator.dispatch(block,
                        new MultiVariant(WeightedList.of(new Variant(steamModelId)))));
                continue;
            }

            if (block instanceof BlockReactorMachineModelled) {
                // BER-rendered machine: render NOTHING in the chunk mesh (the BlockEntityRenderer draws
                // the real model). A cube_all here would put a solid steel cube inside the BER — wrong
                // for non-cube shapes like the toroid ring. Emit an empty in-world model (particle texture
                // only, for break/place FX); the held/inventory item still gets a visible cube_all.
                Identifier emptyModelId = ModelLocationUtils.getModelLocation(block);
                String particle = blockTexture(block).sprite().toString();
                modelOut.accept(emptyModelId, () -> {
                    JsonObject root = new JsonObject();
                    JsonObject tex = new JsonObject();
                    tex.addProperty("particle", particle);
                    root.add("textures", tex);
                    return root;
                });
                blockStateOut.accept(MultiVariantGenerator.dispatch(block,
                        new MultiVariant(WeightedList.of(new Variant(emptyModelId)))));

                Item asItem = block.asItem();
                if (asItem != Items.AIR) {
                    Identifier itemModelId = ModelTemplates.CUBE_ALL.create(
                            ModelLocationUtils.getModelLocation(asItem),
                            TextureMapping.cube(blockTexture(block)), modelOut);
                    String path = BuiltInRegistries.BLOCK.getKey(block).getPath();
                    if (MACHINE_ITEM_MODELS.contains(path)) {
                        // Route the icon through the BER model (3D) instead of the cube_all placeholder.
                        itemModelOut.accept(asItem, ItemModelUtils.specialModel(itemModelId,
                                new reika.reactorcraft.renders.item.ReactorMachineItemRenderer.Unbaked(path)));
                    } else {
                        itemModelOut.accept(asItem, ItemModelUtils.plainModel(itemModelId));
                    }
                    blockItemsHandled.add(asItem);
                }
                continue;
            }

            if (block instanceof BlockSolenoidCasing casing) {
                // Each solenoid-casing part is its own block now. Legacy getTextureIndex picked one of
                // solenoid_0..11 per face + neighbour connectivity (a connected-textures scheme); we
                // can't reproduce the neighbour-aware selection statically, but we give each part its
                // representative top/bottom + side sprite (the non-connected default of that table).
                // FORMED=false shows this opaque casing; FORMED=true swaps to the invisible
                // solenoid_formed model so the assembled solenoid hides its casing (legacy behaviour).
                SolenoidPart part = casing.getPart();
                String name = BuiltInRegistries.BLOCK.getKey(block).getPath();
                String[] tex = casingTex(part);
                Identifier partModelId = Identifier.fromNamespaceAndPath(ReactorCraft.MODID, "block/" + name);
                String endTex = "reactorcraft:block/multi/solenoid_" + tex[0];
                String sideTex = "reactorcraft:block/multi/solenoid_" + tex[1];
                modelOut.accept(partModelId, () -> columnModel(endTex, sideTex));
                MultiVariant partVariant = new MultiVariant(WeightedList.of(new Variant(partModelId)));

                if (part == SolenoidPart.SHELL) {
                    // The hub has no connectivity in the legacy table -- normal datagen dispatch.
                    PropertyDispatch<MultiVariant> dispatch = PropertyDispatch
                            .initial(BlockMultiBlock.FORMED)
                            .generate(formed -> formed ? solenoidFormedVariant : partVariant);
                    blockStateOut.accept(MultiVariantGenerator.dispatch(block).with(dispatch));
                }
                // Every other casing part is connectivity-textured in the legacy getTextureIndex table,
                // so its blockstate ships as STATIC JSON using DragonAPI's dragonapi:connected_axis
                // custom model (see scripts/gen_solenoid_ct_blockstates.py) -- datagen's Variant codec
                // can't express custom model types. The part model above still backs the ITEM icon.

                Item asItem = block.asItem();
                if (asItem != Items.AIR) {
                    itemModelOut.accept(asItem, ItemModelUtils.plainModel(partModelId));
                    blockItemsHandled.add(asItem);
                }
                continue;
            }

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
    // or fall back to "block/steel". Multi-texture machines ("_#" variants) use the base #0 sprite.
    private static final Map<String, String> BLOCK_TEX = new HashMap<>();
    static {
        // ores
        BLOCK_TEX.put("pitchblende_ore", "block/ore/pitchblende");
        BLOCK_TEX.put("end_pitchblende_ore", "block/ore/endblende");
        BLOCK_TEX.put("cadmium_ore", "block/ore/cadmium");
        BLOCK_TEX.put("indium_ore", "block/ore/indium");
        BLOCK_TEX.put("silver_ore", "block/ore/silver");
        BLOCK_TEX.put("ammonium_ore", "block/ore/ammonium");
        BLOCK_TEX.put("calcite_ore", "block/ore/calcite");
        BLOCK_TEX.put("magnetite_ore", "block/ore/magnetite");
        BLOCK_TEX.put("thorium_ore", "block/ore/thorium");
        // storage
        BLOCK_TEX.put("graphite_block", "block/mat/graphite");
        BLOCK_TEX.put("calcite_block", "block/mat/calcite");
        BLOCK_TEX.put("lodestone_block", "block/mat/lodestone");
        // single-texture machines (registry id differs from texture name)
        BLOCK_TEX.put("control_rod", "block/control");
        BLOCK_TEX.put("reactor_cpu", "block/cpu");
        BLOCK_TEX.put("neutron_absorber", "block/absorber");
        BLOCK_TEX.put("breeder_core", "block/breeder");
        BLOCK_TEX.put("heat_exchanger", "block/exchanger");
        BLOCK_TEX.put("fusion_heater", "block/heater");
        BLOCK_TEX.put("fusion_injector", "block/injector");
        BLOCK_TEX.put("synthesizer", "block/synthesizer");
        BLOCK_TEX.put("tritizer", "block/tritizer");
        BLOCK_TEX.put("waste_container", "block/wastecontainer");
        BLOCK_TEX.put("neutron_reflector", "block/reflector");
        BLOCK_TEX.put("fuel_dump", "block/fueldump");
        BLOCK_TEX.put("fuel_rod", "block/fuel_0");
        BLOCK_TEX.put("coolant_cell", "block/coolant_0");
        BLOCK_TEX.put("reactor_boiler", "block/boiler_0");
        BLOCK_TEX.put("thorium_core", "block/thorium_0");
        BLOCK_TEX.put("sodium_boiler", "block/sodiumboiler_0");
        BLOCK_TEX.put("pebble_bed", "block/pebblebed_0");
        BLOCK_TEX.put("co2_heater", "block/co2heater_0");
        BLOCK_TEX.put("turbine_meter", "block/turbinemeter_0");
        BLOCK_TEX.put("waste_decayer", "block/wastedecayer_0");
        // fluid-ish technical blocks
        BLOCK_TEX.put("thorium_fuel", "block/fluid/lifbe_fuel");
        BLOCK_TEX.put("steam", "block/steam");
        // lines: were falling through to the generic steel fallback (same as every unmapped
        // machine), giving the steam line and heat pipe identical, thematically blank item icons.
        BLOCK_TEX.put("steam_line", "block/steam");
        BLOCK_TEX.put("heat_pipe", "block/fluid/sodiumhot");
    }

    private static Material blockTexture(Block block) {
        String id = BuiltInRegistries.BLOCK.getKey(block).getPath();
        String path = BLOCK_TEX.get(id);
        if (path == null) {
            if (id.endsWith("_fluorite_ore")) // fluorite ores: <color>_fluorite_ore -> ore/fluorite_<color>
                path = "block/ore/fluorite_" + id.substring(0, id.length() - "_fluorite_ore".length());
            else // BER-rendered machines / unmapped: a generic steel casing so they aren't missing-texture
                path = "block/steel";
        }
        return new Material(Identifier.fromNamespaceAndPath(ReactorCraft.MODID, path));
    }

    /**
     * { end (top/bottom), side } solenoid_N sprite index per casing part -- legacy getTextureIndex,
     * non-connected default. The spoke (hysteresis rod) uses solenoid_8 on all four lateral faces (the
     * rod winding, so it reads as four strips) and solenoid_9 on top/bottom, matching the legacy
     * side>1 vs side<2 selection for meta 4; it is rotationally symmetric, so no orientation is needed.
     */
    private static String[] casingTex(SolenoidPart part) {
        return switch (part) {
            case FACE, EDGE -> new String[]{"3", "3"};
            case WALL -> new String[]{"3", "11"};
            case WALL_EDGE -> new String[]{"3", "2"};
            case SPOKE -> new String[]{"9", "8"};
            // Legacy meta 5: side>1 -> solenoid_6 (lateral), side<2 -> solenoid_3 (top/bottom).
            // Was previously emitted swapped (end=6/side=3).
            case SHELL -> new String[]{"3", "6"};
        };
    }

    /** {@code cube_column} model JSON with the given end (top/bottom) and side sprites. */
    private static JsonObject columnModel(String endTex, String sideTex) {
        JsonObject root = new JsonObject();
        root.addProperty("parent", "minecraft:block/cube_column");
        JsonObject tex = new JsonObject();
        tex.addProperty("end", endTex);
        tex.addProperty("side", sideTex);
        root.add("textures", tex);
        return root;
    }

    /** {@code cube_all} model JSON with the given sprite on every face. */
    private static JsonObject cubeAllModel(String allTex) {
        JsonObject root = new JsonObject();
        root.addProperty("parent", "minecraft:block/cube_all");
        JsonObject tex = new JsonObject();
        tex.addProperty("all", allTex);
        root.add("textures", tex);
        return root;
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
                .filter(h -> h.getKey().identifier().getNamespace().equals(ReactorCraft.MODID))
                // Connectivity-textured solenoid casings ship STATIC blockstates (dragonapi:connected_axis
                // custom model, not expressible in datagen) -- exclude them from the must-have-a-generated-
                // blockstate validation. The hub (SHELL) keeps its datagen blockstate.
                .filter(h -> !(h.value() instanceof BlockSolenoidCasing c) || c.getPart() == SolenoidPart.SHELL);
    }

    @Override
    protected Stream<? extends Holder<Item>> getKnownItems() {
        return BuiltInRegistries.ITEM.listElements()
                .filter(h -> h.getKey().identifier().getNamespace().equals(ReactorCraft.MODID));
    }
}
