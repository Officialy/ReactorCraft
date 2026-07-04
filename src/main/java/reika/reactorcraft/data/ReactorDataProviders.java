package reika.reactorcraft.data;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import reika.reactorcraft.ReactorCraft;

/**
 * 26.2 datagen entry point for ReactorCraft. Client handler emits language + models; server handler
 * emits loot, the ore worldgen {@link net.minecraft.core.RegistrySetBuilder} (configured + placed
 * features), biome modifiers, and recipes (smelting/crafting + processor/centrifuge JSON + the
 * cross-mod RotaryCraft recipes). Tag and advancement providers are tracked as follow-ups.
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
        event.createProvider(ReactorBlockTagsProvider::new);
        event.createProvider(ReactorLootProvider::new);
        event.createDatapackRegistryObjects(ReactorWorldGenProvider.buildRegistrySet());
        event.createProvider(ReactorBiomeModifierProvider::new);
        event.createProvider(ReactorRecipeProvider::new);
        event.createProvider(ReactorMachineRecipeProvider::new);
    }
}
