package reika.reactorcraft.data;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.data.ItemTagsProvider;

import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.registry.ReactorItems;

/**
 * Item tags ReactorCraft publishes for other mods.
 *
 * <p>1.7.10 {@code ReactorCraft.java} registered the {@code depletedUranium} ore dictionary entry for
 * the depleted fuel rod and the old pellet, which is what RotaryCraft's depleted-uranium flywheel core
 * recipe keyed off. The modern equivalent is the {@code c:ingots/depleted_uranium} tag: RotaryCraft's
 * recipe references it without depending on ReactorCraft, and simply never matches when ReactorCraft
 * is absent — the same outcome as the legacy "skip the recipe if the oredict is empty" branch.
 */
public class ReactorItemTagsProvider extends ItemTagsProvider {

    public static final TagKey<Item> DEPLETED_URANIUM = TagKey.create(Registries.ITEM,
            Identifier.fromNamespaceAndPath("c", "ingots/depleted_uranium"));
    public static final TagKey<Item> QUICKLIME_DUST = TagKey.create(Registries.ITEM,
            Identifier.fromNamespaceAndPath("c", "dusts/quicklime"));
    public static final TagKey<Item> AMMONIUM_DUST = TagKey.create(Registries.ITEM,
            Identifier.fromNamespaceAndPath("c", "dusts/ammonium"));

    public ReactorItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup, ReactorCraft.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for (var ore : reika.reactorcraft.registry.ReactorOreType.list) {
            String material = ore == reika.reactorcraft.registry.ReactorOreType.ENDBLENDE ? "pitchblende" : ore.name().toLowerCase(java.util.Locale.ROOT);
            var key = common("ores/" + material);
            if (ore == reika.reactorcraft.registry.ReactorOreType.FLUORITE) {
                for (var color : reika.reactorcraft.registry.FluoriteTypes.colorList) {
                    tag(key).add(net.minecraft.core.registries.BuiltInRegistries.ITEM.getResourceKey(reika.reactorcraft.registry.ReactorBlocks.fluoriteOre(color).asItem()).orElseThrow());
                    tag(common("gems/fluorite")).add(net.minecraft.core.registries.BuiltInRegistries.ITEM.getResourceKey(ReactorItems.fluorite(color)).orElseThrow());
                }
                tag(common("gems")).addTag(common("gems/fluorite"));
            } else {
                tag(key).add(net.minecraft.core.registries.BuiltInRegistries.ITEM.getResourceKey(ore.getBlock().asItem()).orElseThrow());
                String product = switch (ore) {
                    case PITCHBLENDE, ENDBLENDE -> "ingots/uranium";
                    case CADMIUM, INDIUM, SILVER -> "ingots/" + material;
                    case CALCITE, MAGNETITE -> "gems/" + material;
                    case THORIUM, AMMONIUM -> "dusts/" + material;
                    default -> throw new IllegalStateException("Unhandled ore " + ore);
                };
                tag(common(product)).add(net.minecraft.core.registries.BuiltInRegistries.ITEM.getResourceKey(ore.getProduct()).orElseThrow());
                tag(common(product.split("/")[0])).addTag(common(product));
            }
            tag(common("ores")).addTag(key);
        }
        tag(DEPLETED_URANIUM)
                .add(ReactorItems.DEPLETED_FUEL.getKey())
                .add(ReactorItems.DEPLETED_PELLET.getKey());
        tag(QUICKLIME_DUST).add(ReactorItems.LIME.getKey());
        tag(AMMONIUM_DUST).add(ReactorItems.AMMONIUM_DUST.getKey());
    }

    private static TagKey<Item> common(String path) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", path));
    }
}
