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
import reika.reactorcraft.models.ModelBigTurbine;
import reika.reactorcraft.models.ModelCentrifuge;
import reika.reactorcraft.models.ModelCondenser;
import reika.reactorcraft.models.ModelDiffuser;
import reika.reactorcraft.models.ModelControl;
import reika.reactorcraft.models.ModelElectrolyzer;
import reika.reactorcraft.models.ModelExchanger;
import reika.reactorcraft.models.ModelFlywheel;
import reika.reactorcraft.models.ModelGenerator;
import reika.reactorcraft.models.ModelGasCollector;
import reika.reactorcraft.models.ModelHeavyPump;
import reika.reactorcraft.models.ModelMagnet;
import reika.reactorcraft.models.ModelMiniTurbine;
import reika.reactorcraft.models.ModelProcessor;
import reika.reactorcraft.models.ModelSolarTop;
import reika.reactorcraft.models.ModelReactorPump;
import reika.reactorcraft.models.ModelSolarExchanger;
import reika.reactorcraft.models.ModelSolenoid;
import reika.reactorcraft.models.ModelSteamGrate;
import reika.reactorcraft.models.ModelTurbine;
import reika.reactorcraft.models.ModelWasteStorage;
import reika.reactorcraft.renders.RenderBigTurbine;
import reika.reactorcraft.renders.RenderCentrifuge;
import reika.reactorcraft.renders.RenderCondenser;
import reika.reactorcraft.renders.RenderSteamDiffuser;
import reika.reactorcraft.renders.RenderControl;
import reika.reactorcraft.renders.RenderElectrolyzer;
import reika.reactorcraft.renders.RenderExchanger;
import reika.reactorcraft.renders.RenderFusionMarker;
import reika.reactorcraft.renders.RenderGasCollector;
import reika.reactorcraft.renders.RenderHeavyPump;
import reika.reactorcraft.renders.RenderMagnet;
import reika.reactorcraft.renders.RenderMiniTurbine;
import reika.reactorcraft.renders.RenderProcessor;
import reika.reactorcraft.renders.RenderSolarTop;
import reika.reactorcraft.renders.RenderReactorPump;
import reika.reactorcraft.renders.RenderSolarExchanger;
import reika.reactorcraft.renders.RenderSolenoid;
import reika.reactorcraft.renders.RenderSteamGrate;
import reika.reactorcraft.renders.RenderTurbine;
import reika.reactorcraft.renders.RenderTurbineWheel;
import reika.reactorcraft.renders.RenderGenerator;
import reika.reactorcraft.renders.RenderWasteStorage;

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
	public static final ModelLayerLocation WASTE_STORAGE = layer("waste_storage");
	public static final ModelLayerLocation ELECTROLYZER = layer("electrolyzer");
	public static final ModelLayerLocation SOLAR_EXCHANGER = layer("solar_exchanger");
	public static final ModelLayerLocation HEAVY_PUMP = layer("heavy_pump");
	public static final ModelLayerLocation CENTRIFUGE = layer("centrifuge");
	public static final ModelLayerLocation PROCESSOR = layer("processor");
	public static final ModelLayerLocation REACTOR_PUMP = layer("reactor_pump");
	public static final ModelLayerLocation EXCHANGER = layer("exchanger");
	public static final ModelLayerLocation GAS_COLLECTOR = layer("gas_collector");
	public static final ModelLayerLocation FLYWHEEL = layer("flywheel");
	public static final ModelLayerLocation GENERATOR = layer("generator");
	public static final ModelLayerLocation MINI_TURBINE = layer("mini_turbine");
	public static final ModelLayerLocation STEAM_DIFFUSER = layer("steam_diffuser");
	public static final ModelLayerLocation SOLAR_TOP = layer("solar_top");
	// One layer per turbine multiblock stage (0..MAX_STAGE): the blade box size is baked per stage so a
	// row of cores renders as one tapered turbine. RenderTurbine bakes all and picks by getStage().
	public static final ModelLayerLocation[] TURBINE_STAGES = new ModelLayerLocation[reika.reactorcraft.models.ModelTurbine.MAX_STAGE + 1];
	static {
		for (int i = 0; i < TURBINE_STAGES.length; i++)
			TURBINE_STAGES[i] = layer("turbine_stage_" + i);
	}
	// Same idea for the high-pressure (big) turbine, which has its own max stage (6).
	public static final ModelLayerLocation[] BIG_TURBINE_STAGES = new ModelLayerLocation[reika.reactorcraft.models.ModelBigTurbine.MAX_STAGE + 1];
	static {
		for (int i = 0; i < BIG_TURBINE_STAGES.length; i++)
			BIG_TURBINE_STAGES[i] = layer("big_turbine_stage_" + i);
	}

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
		event.registerBlockEntityRenderer(ReactorBlockEntities.BIGTURBINE.get(), RenderBigTurbine::new);
		event.registerBlockEntityRenderer(ReactorBlockEntities.MINITURBINE.get(), RenderMiniTurbine::new);
		event.registerBlockEntityRenderer(ReactorBlockEntities.DIFFUSER.get(), RenderSteamDiffuser::new);
		event.registerBlockEntityRenderer(ReactorBlockEntities.SOLARTOP.get(), RenderSolarTop::new);
		event.registerBlockEntityRenderer(ReactorBlockEntities.STORAGE.get(), RenderWasteStorage::new);
		event.registerBlockEntityRenderer(ReactorBlockEntities.ELECTROLYZER.get(), RenderElectrolyzer::new);
		event.registerBlockEntityRenderer(ReactorBlockEntities.SOLAR.get(), RenderSolarExchanger::new);
		event.registerBlockEntityRenderer(ReactorBlockEntities.FLUIDEXTRACTOR.get(), RenderHeavyPump::new);
		event.registerBlockEntityRenderer(ReactorBlockEntities.CENTRIFUGE.get(), RenderCentrifuge::new);
		event.registerBlockEntityRenderer(ReactorBlockEntities.PROCESSOR.get(), RenderProcessor::new);
		event.registerBlockEntityRenderer(ReactorBlockEntities.PUMP.get(), RenderReactorPump::new);
		event.registerBlockEntityRenderer(ReactorBlockEntities.EXCHANGER.get(), RenderExchanger::new);
		event.registerBlockEntityRenderer(ReactorBlockEntities.COLLECTOR.get(), RenderGasCollector::new);
		event.registerBlockEntityRenderer(ReactorBlockEntities.FLYWHEEL.get(), RenderTurbineWheel::new);
		event.registerBlockEntityRenderer(ReactorBlockEntities.GENERATOR.get(), RenderGenerator::new);

		// Fluid ducts: the connected pipe + fluid tube (legacy DuctRenderer, ported). Empty in-world model
		// (BlockReactorDuct is a BlockReactorMachineModelled), so the BER draws the whole pipe from the BE's
		// connection cache — grey shell when empty, translucent fluid tube when carrying fluid.
		event.registerBlockEntityRenderer(ReactorBlockEntities.GASPIPE.get(), reika.reactorcraft.renders.ReactorPipeRenderer::new);
		event.registerBlockEntityRenderer(ReactorBlockEntities.MAGNETPIPE.get(), reika.reactorcraft.renders.ReactorPipeRenderer::new);
		event.registerBlockEntityRenderer(ReactorBlockEntities.WASTEPIPE.get(), reika.reactorcraft.renders.ReactorPipeRenderer::new);

		// Lines (steam line, heat pipe): connected square pipe textured with waterline.png (legacy
		// RenderWaterLine), heat pipe tinted warm. Same empty-model + BER-draws-everything approach.
		event.registerBlockEntityRenderer(ReactorBlockEntities.STEAMLINE.get(), reika.reactorcraft.renders.ReactorLineRenderer::new);
		event.registerBlockEntityRenderer(ReactorBlockEntities.HEATPIPE.get(), reika.reactorcraft.renders.ReactorLineRenderer::new);

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
		event.registerLayerDefinition(WASTE_STORAGE, ModelWasteStorage::createLayer);
		event.registerLayerDefinition(ELECTROLYZER, ModelElectrolyzer::createLayer);
		event.registerLayerDefinition(SOLAR_EXCHANGER, ModelSolarExchanger::createLayer);
		event.registerLayerDefinition(HEAVY_PUMP, ModelHeavyPump::createLayer);
		event.registerLayerDefinition(CENTRIFUGE, ModelCentrifuge::createLayer);
		event.registerLayerDefinition(PROCESSOR, ModelProcessor::createLayer);
		event.registerLayerDefinition(REACTOR_PUMP, ModelReactorPump::createLayer);
		event.registerLayerDefinition(EXCHANGER, ModelExchanger::createLayer);
		event.registerLayerDefinition(GAS_COLLECTOR, ModelGasCollector::createLayer);
		event.registerLayerDefinition(FLYWHEEL, ModelFlywheel::createLayer);
		event.registerLayerDefinition(GENERATOR, ModelGenerator::createLayer);
		event.registerLayerDefinition(MINI_TURBINE, ModelMiniTurbine::createLayer);
		event.registerLayerDefinition(STEAM_DIFFUSER, ModelDiffuser::createLayer);
		event.registerLayerDefinition(SOLAR_TOP, ModelSolarTop::createLayer);
		for (int i = 0; i < TURBINE_STAGES.length; i++) {
			final int s = i;
			event.registerLayerDefinition(TURBINE_STAGES[s], () -> ModelTurbine.createLayer(s));
		}
		for (int i = 0; i < BIG_TURBINE_STAGES.length; i++) {
			final int s = i;
			event.registerLayerDefinition(BIG_TURBINE_STAGES[s], () -> ModelBigTurbine.createLayer(s));
		}
	}

	private ReactorModelLayers() {}
}
