package reika.reactorcraft.data;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.data.tags.TagsProvider;

public final class ReactorBiomeTagsProvider extends TagsProvider<Biome> {
    public static final TagKey<Biome> EXTRA_PITCHBLENDE=TagKey.create(Registries.BIOME,Identifier.fromNamespaceAndPath("reactorcraft","extra_pitchblende_biomes"));
    public ReactorBiomeTagsProvider(PackOutput output,CompletableFuture<HolderLookup.Provider> lookup) { super(output,Registries.BIOME,lookup); }
    @Override protected void addTags(HolderLookup.Provider provider) {
        tag(EXTRA_PITCHBLENDE).add(Biomes.MUSHROOM_FIELDS).addOptional(net.minecraft.resources.ResourceKey.create(Registries.BIOME,Identifier.fromNamespaceAndPath("chromaticraft","rainbow_forest")));
    }
}
