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
        tag(DEPLETED_URANIUM)
                .add(ReactorItems.DEPLETED_FUEL.getKey())
                .add(ReactorItems.DEPLETED_PELLET.getKey());
        tag(QUICKLIME_DUST).add(ReactorItems.LIME.getKey());
        tag(AMMONIUM_DUST).add(ReactorItems.AMMONIUM_DUST.getKey());
    }
}
