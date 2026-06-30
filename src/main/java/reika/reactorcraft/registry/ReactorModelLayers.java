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
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.entities.RenderNeutron;
import reika.reactorcraft.models.ModelCondenser;
import reika.reactorcraft.models.ModelControl;
import reika.reactorcraft.models.ModelMagnet;
import reika.reactorcraft.models.ModelSolenoid;
import reika.reactorcraft.models.ModelSteamGrate;
import reika.reactorcraft.models.ModelTurbine;
import reika.reactorcraft.renders.RenderCondenser;
import reika.reactorcraft.renders.RenderControl;
import reika.reactorcraft.renders.RenderFusionMarker;
import reika.reactorcraft.renders.RenderMagnet;
import reika.reactorcraft.renders.RenderSolenoid;
import reika.reactorcraft.renders.RenderSteamGrate;
import reika.reactorcraft.renders.RenderTurbine;

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
	public static final ModelLayerLocation CONTROL_ROD = layer("control_rod");
	public static final ModelLayerLocation TURBINE = layer("turbine");

	public static void init(IEventBus bus) {
		bus.addListener(ReactorModelLayers::registerEntityRenderers);
		bus.addListener(ReactorModelLayers::registerLayerDefinitions);
	}

	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		// First ReactorCraft BER: the fusion-marker tokamak build guide (pure line geometry, no model layer).
		event.registerBlockEntityRenderer(ReactorBlockEntities.MARKER.get(), RenderFusionMarker::new);
		event.registerBlockEntityRenderer(ReactorBlockEntities.CONDENSER.get(), RenderCondenser::new);
		event.registerBlockEntityRenderer(ReactorBlockEntities.MAGNET.get(), RenderMagnet::new);
		event.registerBlockEntityRenderer(ReactorBlockEntities.SOLENOID.get(), RenderSolenoid::new);
		event.registerBlockEntityRenderer(ReactorBlockEntities.GRATE.get(), RenderSteamGrate::new);
		event.registerBlockEntityRenderer(ReactorBlockEntities.CONTROL.get(), RenderControl::new);
		event.registerBlockEntityRenderer(ReactorBlockEntities.TURBINECORE.get(), RenderTurbine::new);

		// Fluid ducts: the connected pipe + fluid tube (legacy DuctRenderer, ported). Empty in-world model
		// (BlockReactorDuct is a BlockReactorMachineModelled), so the BER draws the whole pipe from the BE's
		// connection cache — grey shell when empty, translucent fluid tube when carrying fluid.
		event.registerBlockEntityRenderer(ReactorBlockEntities.GASPIPE.get(), reika.reactorcraft.renders.ReactorPipeRenderer::new);
		event.registerBlockEntityRenderer(ReactorBlockEntities.MAGNETPIPE.get(), reika.reactorcraft.renders.ReactorPipeRenderer::new);
		event.registerBlockEntityRenderer(ReactorBlockEntities.WASTEPIPE.get(), reika.reactorcraft.renders.ReactorPipeRenderer::new);

		// Entity renderers MUST be registered for every spawnable entity type or the client NPEs in
		// EntityRenderDispatcher.shouldRender the moment one spawns (e.g. a neutron burst when a fission
		// core is fuelled). The legacy billboard-quad effect renderers (RenderNeutron/Plasma/Fusion/
		// Radiation) aren't ported yet, so use NoopRenderer for now — the entities stay invisible but the
		// reactor logic (neutron fission, radiation, plasma) is fully server-side and unaffected.
		event.registerEntityRenderer(ReactorEntities.NEUTRON.get(), RenderNeutron::new);
		event.registerEntityRenderer(ReactorEntities.RADIATION.get(), NoopRenderer::new);
		event.registerEntityRenderer(ReactorEntities.PLASMA.get(), NoopRenderer::new);
		event.registerEntityRenderer(ReactorEntities.FUSION.get(), NoopRenderer::new);
		event.registerEntityRenderer(ReactorEntities.NUCLEARWASTE.get(), NoopRenderer::new);
	}

	public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(CONDENSER, ModelCondenser::createLayer);
		event.registerLayerDefinition(MAGNET, ModelMagnet::createLayer);
		event.registerLayerDefinition(SOLENOID, ModelSolenoid::createLayer);
		event.registerLayerDefinition(STEAM_GRATE, ModelSteamGrate::createLayer);
		event.registerLayerDefinition(CONTROL_ROD, ModelControl::createLayer);
		event.registerLayerDefinition(TURBINE, ModelTurbine::createLayer);
	}

	private ReactorModelLayers() {}
}
