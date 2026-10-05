package reika.reactorcraft.data;

import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import reika.reactorcraft.ReactorCraft;

/**
 * 26.2 datagen entry point for ReactorCraft. Client handler emits language + models; server handler
 * emits loot, the ore worldgen {@link net.minecraft.core.RegistrySetBuilder} (configured + placed
 * features), biome modifiers, and recipes (smelting/crafting + processor/centrifuge JSON + the
 * cross-mod RotaryCraft recipes), tags and the original achievement catalog as advancements.
 */
@EventBusSubscriber(modid = ReactorCraft.MODID)
public final class ReactorDataProviders {

    private ReactorDataProviders() {}

    @SubscribeEvent
    public static void onGatherClient(GatherDataEvent.Client event) {
        event.createProvider(output -> new ReactorLang(output, "en_us"));
        event.createProvider(ReactorModelProvider::new);
    }

    @SubscribeEvent
    public static void onGatherServer(GatherDataEvent.Server event) {
        event.createProvider(ReactorBiomeTagsProvider::new);
        event.createProvider(ReactorBlockTagsProvider::new);
        event.createProvider(ReactorItemTagsProvider::new);
        event.createReloadableRegistryObjects(new RegistrySetBuilder()
                .add(ReactorRecipeProvider.bootstrap())
                .add(Registries.LOOT_TABLE, new ReactorLootProvider())
                .add(Registries.ADVANCEMENT, new ReactorAdvancementProvider()));
        event.createWorldRegistryObjects(ReactorWorldGenProvider.buildRegistrySet());
        event.createProvider(ReactorBiomeModifierProvider::new);
        event.createProvider(ReactorMachineRecipeProvider::new);
    }
}
