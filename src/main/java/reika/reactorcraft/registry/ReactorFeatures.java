package reika.reactorcraft.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.world.ReactorOreConfig;
import reika.reactorcraft.world.ReactorOreFeature;

/**
 * Registers ReactorCraft's worldgen {@link Feature}s. A single {@link ReactorOreFeature} backs every
 * ore — the per-ore parameters live in the configured features ({@code ReactorWorldGenProvider})
 * and the biome targeting lives in the biome modifiers ({@code ReactorBiomeModifierProvider}).
 */
public final class ReactorFeatures {

    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, ReactorCraft.MODID);

    public static final DeferredHolder<Feature<?>, Feature<ReactorOreConfig>> ORE =
            FEATURES.register("ore", ReactorOreFeature::new);

    private ReactorFeatures() {}
}
