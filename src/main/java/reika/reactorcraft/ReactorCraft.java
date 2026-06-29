/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import reika.reactorcraft.registry.ReactorBlockEntities;
import reika.reactorcraft.registry.ReactorBlocks;
import reika.reactorcraft.registry.ReactorDataComponents;
import reika.reactorcraft.registry.ReactorEntities;
import reika.reactorcraft.registry.ReactorFeatures;
import reika.reactorcraft.registry.ReactorFluids;
import reika.reactorcraft.registry.ReactorItems;
import reika.reactorcraft.registry.ReactorRecipeSerializers;
import reika.reactorcraft.registry.ReactorRecipeTypes;
import reika.reactorcraft.registry.ReactorSounds;
import reika.reactorcraft.registry.ReactorTabs;
import reika.reactorcraft.registry.ReactorTiles;

@Mod(ReactorCraft.MODID)
public class ReactorCraft {

    public static final String MODID = "reactorcraft";

    /** Legacy single-channel name for the ReikaPacketHelper bridge (see {@link reika.reactorcraft.ReactorPacketCore}). */
    public static final String packetChannel = "ReactorCraftData";

    public static final Logger LOGGER = LogManager.getLogger("ReactorCraft");

    private static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, MODID);

    public static final DeferredHolder<MobEffect, MobEffect> radiation = MOB_EFFECTS.register("radiation",
            () -> new reika.reactorcraft.auxiliary.MobEffectRadiation(MobEffectCategory.HARMFUL, 0x7FFF00));

    public static ReactorCraft instance;

    public ReactorCraft(IEventBus modEventBus, ModContainer modContainer) {
        instance = this;

        ReactorDataComponents.COMPONENTS.register(modEventBus);

        ReactorBlocks.BLOCKS.register(modEventBus);
        ReactorBlocks.ITEMS.register(modEventBus);
        ReactorItems.ITEMS.register(modEventBus);

        ReactorFluids.FLUID_TYPES.register(modEventBus);
        ReactorFluids.FLUIDS.register(modEventBus);

        ReactorRecipeTypes.RECIPE_TYPES.register(modEventBus);
        ReactorRecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);

        ReactorFeatures.FEATURES.register(modEventBus);
        ReactorBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ReactorEntities.ENTITIES.register(modEventBus);

        ReactorTabs.CREATIVE_MODE_TABS.register(modEventBus);

        ReactorSounds.SOUND_EVENTS.register(modEventBus);

        reika.reactorcraft.registry.ReactorMenus.REGISTRY.register(modEventBus);

        // ReactorCraft isn't a DragonAPIMod, so the bridge registers under RotaryCraft's mod object
        // (channel string "ReactorCraftData" keeps it distinct). Same getOwnerMod precedent as the BERs.
        reika.dragonapi.libraries.io.ReikaPacketHelper.registerPacketHandler(
                reika.rotarycraft.RotaryCraft.getInstance(), packetChannel, new reika.reactorcraft.ReactorPacketCore());

        MOB_EFFECTS.register(modEventBus);

        modEventBus.addListener(ReactorBlockEntities::registerCapabilities);
        modEventBus.addListener((net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent e) ->
                e.enqueueWork(ReactorTiles::loadMappings));

        if (net.neoforged.fml.loading.FMLEnvironment.getDist() == net.neoforged.api.distmarker.Dist.CLIENT) {
            reika.reactorcraft.registry.ReactorModelLayers.init(modEventBus);
            modEventBus.addListener(ReactorCraft::registerScreens);
        }
    }

    /** Binds {@code AbstractContainerScreen}s to their {@link net.minecraft.world.inventory.MenuType}s. */
    private static void registerScreens(final net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event) {
        event.register(reika.reactorcraft.registry.ReactorMenus.NUCLEAR_CORE.get(),
                reika.reactorcraft.guis.ScreenNuclearCore::new);
        event.register(reika.reactorcraft.registry.ReactorMenus.CENTRIFUGE.get(),
                reika.reactorcraft.guis.ScreenCentrifuge::new);
        event.register(reika.reactorcraft.registry.ReactorMenus.WASTE_DECAYER.get(),
                reika.reactorcraft.guis.ScreenWasteDecayer::new);
        event.register(reika.reactorcraft.registry.ReactorMenus.WASTE_CONTAINER.get(),
                reika.reactorcraft.guis.ScreenWasteContainer::new);
        event.register(reika.reactorcraft.registry.ReactorMenus.PEBBLE_BED.get(),
                reika.reactorcraft.guis.ScreenPebbleBed::new);
        event.register(reika.reactorcraft.registry.ReactorMenus.THORIUM_CORE.get(),
                reika.reactorcraft.guis.ScreenThoriumCore::new);
        event.register(reika.reactorcraft.registry.ReactorMenus.SYNTHESIZER.get(),
                reika.reactorcraft.guis.ScreenSynthesizer::new);
        event.register(reika.reactorcraft.registry.ReactorMenus.PROCESSOR.get(),
                reika.reactorcraft.guis.ScreenProcessor::new);
        event.register(reika.reactorcraft.registry.ReactorMenus.ELECTROLYZER.get(),
                reika.reactorcraft.guis.ScreenElectrolyzer::new);
        event.register(reika.reactorcraft.registry.ReactorMenus.WASTE_STORAGE.get(),
                reika.reactorcraft.guis.ScreenWasteStorage::new);
        event.register(reika.reactorcraft.registry.ReactorMenus.CPU.get(),
                reika.reactorcraft.guis.ScreenCPU::new);
    }

    public static ReactorCraft getInstance() {
        return instance;
    }

    /** Original ReactorCraft beta lock — always false in the port. */
    public boolean isLocked() {
        return false;
    }
}
