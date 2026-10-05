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

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reika.dragonapi.instantiable.io.SoundLoader;
import reika.dragonapi.libraries.io.ReikaPacketHelper;
import reika.reactorcraft.auxiliary.MobEffectRadiation;
import reika.reactorcraft.command.SolenoidDebugCommand;
import reika.reactorcraft.guis.*;
import reika.reactorcraft.modinterface.jei.ReactorRecipeSync;
import reika.reactorcraft.registry.*;
import reika.rotarycraft.RotaryCraft;

@Mod(ReactorCraft.MODID)
public class ReactorCraft {

    public static final String MODID = "reactorcraft";

    /**
     * Legacy single-channel name for the ReikaPacketHelper bridge (see {@link ReactorPacketCore}).
     */
    public static final String packetChannel = "ReactorCraftData";
    public static final Logger LOGGER = LogManager.getLogger("ReactorCraft");
    protected static final SoundLoader sounds = new SoundLoader(ReactorSounds.class);
    private static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, MODID);

    public static final DeferredHolder<MobEffect, MobEffect> radiation = MOB_EFFECTS.register("radiation",
            () -> new MobEffectRadiation(MobEffectCategory.HARMFUL, 0x7FFF00));

    public static ReactorCraft instance;

    public ReactorCraft(IEventBus modEventBus, ModContainer modContainer) {
        instance = this;

        modContainer.registerConfig(ModConfig.Type.COMMON, ReactorOptions.SPEC);

        ReactorDataComponents.COMPONENTS.register(modEventBus);

        ReactorBlocks.BLOCKS.register(modEventBus);
        ReactorBlocks.ITEMS.register(modEventBus);
        ReactorItems.ITEMS.register(modEventBus);

        ReactorFluids.FLUID_TYPES.register(modEventBus);
        ReactorFluids.FLUIDS.register(modEventBus);

        ReactorRecipeTypes.RECIPE_TYPES.register(modEventBus);
        ReactorRecipeSync.register();
        ReactorRecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);
        ReactorGameTests.TEST_INSTANCE_TYPES.register(modEventBus);
        modEventBus.addListener(ReactorGameTests::onRegisterGameTests);
        modEventBus.addListener(reika.reactorcraft.container.CPUViewPayload::register);

        ReactorFeatures.FEATURES.register(modEventBus);
        ReactorBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ReactorEntities.ENTITIES.register(modEventBus);

        ReactorTabs.CREATIVE_MODE_TABS.register(modEventBus);

        ReactorSounds.SOUND_EVENTS.register(modEventBus);

        ReactorMenus.REGISTRY.register(modEventBus);

        // ReactorCraft isn't a DragonAPIMod, so the bridge registers under RotaryCraft's mod object
        // (channel string "ReactorCraftData" keeps it distinct). Same getOwnerMod precedent as the BERs.
        ReikaPacketHelper.registerPacketHandler(
                RotaryCraft.getInstance(), packetChannel, new ReactorPacketCore());

        MOB_EFFECTS.register(modEventBus);

        modEventBus.addListener(ReactorBlockEntities::registerCapabilities);
        modEventBus.addListener((FMLCommonSetupEvent e) ->
                e.enqueueWork(() -> {
                    ReactorTiles.loadMappings();
                    if (reika.rotarycraft.registry.ConfigRegistry.HANDBOOK.getState())
                        reika.dragonapi.auxiliary.trackers.PlayerFirstTimeTracker.addTracker(
                                new reika.reactorcraft.auxiliary.ReactorBookTracker());
                }));

        // V33a ReactorCraft.load: stored nuclear waste leaks radiation out of ME networks.
        if (reika.dragonapi.ModList.APPENG.isLoaded())
            modEventBus.addListener((FMLCommonSetupEvent e) -> e.enqueueWork(ReactorCraft::registerMESystemEffects));

        // RegisterCommandsEvent fires on the game bus, not the mod bus -- see DragonAPI.onRegisterCommandEvent
        // for the same pattern.
        NeoForge.EVENT_BUS.addListener(ReactorCraft::registerCommands);

        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            ReactorModelLayers.init(modEventBus);
            modEventBus.addListener(ReactorCraft::registerScreens);
            modEventBus.addListener(this::clientSetup);
        }
    }

    // A bare invokestatic: ReactorCraft's own verification never has to load the AE-typed effect class.
    private static void registerMESystemEffects() {
        reika.reactorcraft.modinterface.WasteMESystemEffect.register();
    }

    private static void registerCommands(final RegisterCommandsEvent event) {
        SolenoidDebugCommand.register(event.getDispatcher());
    }

    /**
     * Binds {@code AbstractContainerScreen}s to their {@link net.minecraft.world.inventory.MenuType}s.
     */
    private static void registerScreens(final RegisterMenuScreensEvent event) {
        event.register(ReactorMenus.NUCLEAR_CORE.get(),
                ScreenNuclearCore::new);
        event.register(ReactorMenus.CENTRIFUGE.get(),
                ScreenCentrifuge::new);
        event.register(ReactorMenus.WASTE_DECAYER.get(),
                ScreenWasteDecayer::new);
        event.register(ReactorMenus.WASTE_CONTAINER.get(),
                ScreenWasteContainer::new);
        event.register(ReactorMenus.PEBBLE_BED.get(),
                ScreenPebbleBed::new);
        event.register(ReactorMenus.THORIUM_CORE.get(),
                ScreenThoriumCore::new);
        event.register(ReactorMenus.SYNTHESIZER.get(),
                ScreenSynthesizer::new);
        event.register(ReactorMenus.PROCESSOR.get(),
                ScreenProcessor::new);
        event.register(ReactorMenus.ELECTROLYZER.get(),
                ScreenElectrolyzer::new);
        event.register(ReactorMenus.WASTE_STORAGE.get(),
                ScreenWasteStorage::new);
        event.register(ReactorMenus.CPU.get(),
                ScreenCPU::new);
    }

    public static ReactorCraft getInstance() {
        return instance;
    }

    public void clientSetup(final FMLClientSetupEvent event) {
        sounds.register();
    }

    /**
     * Original ReactorCraft beta lock — always false in the port.
     */
    public boolean isLocked() {
        return false;
    }
}
