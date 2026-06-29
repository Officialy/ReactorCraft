/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.registry;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import reika.reactorcraft.ReactorCraft;

/**
 * Client render registration for ReactorCraft machine block-entity renderers (the ReactorCraft
 * analogue of {@code reika.rotarycraft.registry.RotaryModelLayers}). Each machine port adds: a
 * {@link ModelLayerLocation} constant here, a {@code registerLayerDefinition(LOC, XModel::createLayer)}
 * in {@link #registerLayerDefinitions}, and a {@code registerBlockEntityRenderer(BE, RenderX::new)} in
 * {@link #registerEntityRenderers}. Invoked from the ReactorCraft constructor, client dist only.
 */
public final class ReactorModelLayers {

	private static ModelLayerLocation layer(String name) {
		return new ModelLayerLocation(Identifier.fromNamespaceAndPath(ReactorCraft.MODID, name), "main");
	}

	// --- Machine model layers (one per ported machine model) ---
	// (added incrementally as each ModelX is ported)
	public static final ModelLayerLocation CONDENSER = layer("condenser");

	public static void init(IEventBus bus) {
		bus.addListener(ReactorModelLayers::registerEntityRenderers);
		bus.addListener(ReactorModelLayers::registerLayerDefinitions);
	}

	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		// First ReactorCraft BER: the fusion-marker tokamak build guide (pure line geometry, no model layer).
		event.registerBlockEntityRenderer(ReactorBlockEntities.MARKER.get(), reika.reactorcraft.renders.RenderFusionMarker::new);
		event.registerBlockEntityRenderer(ReactorBlockEntities.CONDENSER.get(), reika.reactorcraft.renders.RenderCondenser::new);
	}

	public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(CONDENSER, reika.reactorcraft.models.ModelCondenser::createLayer);
	}

	private ReactorModelLayers() {}
}
