/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 ******************************************************************************/
package reika.reactorcraft.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;

import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.renders.item.ReactorMachineItemRenderer;

/**
 * 26.2 client extension registrations. Registers the {@code reactorcraft:machine} special-model
 * renderer so BER-machine item JSONs can route their inventory/hand icon through the actual TESR
 * model (see {@link ReactorMachineItemRenderer}).
 */
@EventBusSubscriber(modid = ReactorCraft.MODID, value = Dist.CLIENT)
public final class ReactorClientExtensions {

    private ReactorClientExtensions() {}

    @SubscribeEvent
    public static void registerSpecialModelRenderers(RegisterSpecialModelRendererEvent event) {
        event.register(ReactorMachineItemRenderer.ID, ReactorMachineItemRenderer.Unbaked.MAP_CODEC);
    }
}
