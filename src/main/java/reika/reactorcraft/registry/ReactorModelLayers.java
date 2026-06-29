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
	public static final ModelLayerLocation MAGNET = layer("magnet");
	public static final ModelLayerLocation SOLENOID = layer("solenoid");
	public static final ModelLayerLocation STEAM_GRATE = layer("steam_grate");

	public static void init(IEventBus bus) {
		bus.addListener(ReactorModelLayers::registerEntityRenderers);
		bus.addListener(ReactorModelLayers::registerLayerDefinitions);
	}

	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		// First ReactorCraft BER: the fusion-marker tokamak build guide (pure line geometry, no model layer).
		event.registerBlockEntityRenderer(ReactorBlockEntities.MARKER.get(), reika.reactorcraft.renders.RenderFusionMarker::new);
		event.registerBlockEntityRenderer(ReactorBlockEntities.CONDENSER.get(), reika.reactorcraft.renders.RenderCondenser::new);
		event.registerBlockEntityRenderer(ReactorBlockEntities.MAGNET.get(), reika.reactorcraft.renders.RenderMagnet::new);
		event.registerBlockEntityRenderer(ReactorBlockEntities.SOLENOID.get(), reika.reactorcraft.renders.RenderSolenoid::new);
		event.registerBlockEntityRenderer(ReactorBlockEntities.GRATE.get(), reika.reactorcraft.renders.RenderSteamGrate::new);

		// Entity renderers MUST be registered for every spawnable entity type or the client NPEs in
		// EntityRenderDispatcher.shouldRender the moment one spawns (e.g. a neutron burst when a fission
		// core is fuelled). The legacy billboard-quad effect renderers (RenderNeutron/Plasma/Fusion/
		// Radiation) aren't ported yet, so use NoopRenderer for now — the entities stay invisible but the
		// reactor logic (neutron fission, radiation, plasma) is fully server-side and unaffected.
		event.registerEntityRenderer(ReactorEntities.NEUTRON.get(), net.minecraft.client.renderer.entity.NoopRenderer::new);
		event.registerEntityRenderer(ReactorEntities.RADIATION.get(), net.minecraft.client.renderer.entity.NoopRenderer::new);
		event.registerEntityRenderer(ReactorEntities.PLASMA.get(), net.minecraft.client.renderer.entity.NoopRenderer::new);
		event.registerEntityRenderer(ReactorEntities.FUSION.get(), net.minecraft.client.renderer.entity.NoopRenderer::new);
		event.registerEntityRenderer(ReactorEntities.NUCLEARWASTE.get(), net.minecraft.client.renderer.entity.NoopRenderer::new);
	}

	public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(CONDENSER, reika.reactorcraft.models.ModelCondenser::createLayer);
		event.registerLayerDefinition(MAGNET, reika.reactorcraft.models.ModelMagnet::createLayer);
		event.registerLayerDefinition(SOLENOID, reika.reactorcraft.models.ModelSolenoid::createLayer);
		event.registerLayerDefinition(STEAM_GRATE, reika.reactorcraft.models.ModelSteamGrate::createLayer);
	}

	private ReactorModelLayers() {}
}
