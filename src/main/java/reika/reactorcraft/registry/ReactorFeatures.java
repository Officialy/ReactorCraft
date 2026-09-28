package reika.reactorcraft.registry;

import com.mojang.serialization.MapCodec;
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

    public static final DeferredRegister<MapCodec<? extends Feature>> FEATURES =
            DeferredRegister.create(Registries.FEATURE_TYPE, ReactorCraft.MODID);

    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<ReactorOreFeature>> ORE =
            FEATURES.register("ore", () -> ReactorOreFeature.CODEC);

    private ReactorFeatures() {}
}
