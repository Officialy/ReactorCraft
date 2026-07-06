/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 ******************************************************************************/
package reika.reactorcraft.renders.item;

import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.models.ModelCentrifuge;
import reika.reactorcraft.models.ModelControl;
import reika.reactorcraft.models.ModelCondenser;
import reika.reactorcraft.models.ModelElectrolyzer;
import reika.reactorcraft.models.ModelExchanger;
import reika.reactorcraft.models.ModelGasCollector;
import reika.reactorcraft.models.ModelHeavyPump;
import reika.reactorcraft.models.ModelMagnet;
import reika.reactorcraft.models.ModelProcessor;
import reika.reactorcraft.models.ModelReactorPump;
import reika.reactorcraft.models.ModelSolarExchanger;
import reika.reactorcraft.models.ModelSolenoid;
import reika.reactorcraft.models.ModelSteamGrate;
import reika.reactorcraft.models.ModelTurbine;
import reika.reactorcraft.models.ModelWasteStorage;
import reika.reactorcraft.registry.ReactorModelLayers;

/**
 * 26.2 special-model renderer for the BER-rendered machine ITEMS (control rod, toroid/solenoid magnet,
 * steam grate, condenser, turbine). The block-in-world is drawn by the {@code ReactorTERenderer}; the
 * inventory/hand icon previously fell back to a flat steel cube because the block has an empty baked
 * model. This routes those items through {@code minecraft:special} (the modern replacement for the
 * removed {@code BlockEntityWithoutLevelRenderer}) so the icon shows the machine's actual 3D model.
 *
 * <p>Each item model JSON declares {@code "model": {"type":"minecraft:special","base":...,
 * "model":{"type":"reactorcraft:machine","machine":"<block_path>"}}}; bake() resolves the block path
 * to the right baked {@link reika.reactorcraft... model} + texture + draw call. Registration is in
 * {@link reika.reactorcraft.client.ReactorClientExtensions}; the JSONs are emitted by ReactorModelProvider.</p>
 */
public class ReactorMachineItemRenderer implements NoDataSpecialModelRenderer {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(ReactorCraft.MODID, "machine");

    /** A baked draw call for one machine: apply the model's own transform + render it at item defaults. */
    @FunctionalInterface
    private interface Draw {
        void render(PoseStack pose, VertexConsumer vc, int light);
    }

    // Was a flat 0.5 for every machine ("the wrong size, all too small"). Bumped the shared default
    // up to 0.65; solenoid_magnet overrides down to 0.3 (was reading "giant" at 0.5), toroid_magnet
    // keeps the original 0.5 and gets an extra -20 degree yaw ("rotate left a little") instead of a
    // size change. These are first-pass numbers, not measured against anything — expect another round.
    private static final float DEFAULT_SCALE = 0.65F;

    private final Draw draw;
    private final Identifier texture;
    private final float scale;
    private final float extraYawDeg;

    private ReactorMachineItemRenderer(Draw draw, Identifier texture, float scale, float extraYawDeg) {
        this.draw = draw;
        this.texture = texture;
        this.scale = scale;
        this.extraYawDeg = extraYawDeg;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        poseStack.pushPose();
        // Frame the block-scale model in the slot: shrink + the standard block-item GUI rotation, about
        // the block centre.
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(Axis.XP.rotationDegrees(30F));
        poseStack.mulPose(Axis.YP.rotationDegrees(225F + extraYawDeg));
        poseStack.translate(-0.5F, -0.5F, -0.5F);

        RenderType rt = RenderTypes.entitySolid(texture);
        PoseStack snap = new PoseStack();
        snap.last().set(poseStack.last());
        collector.submitCustomGeometry(poseStack, rt, (p, vc) -> draw.render(snap, vc, lightCoords));
        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        // Default block-cube extents — good enough for the item highlight box.
        output.accept(new Vector3f(0F, 0F, 0F));
        output.accept(new Vector3f(1F, 1F, 1F));
    }

    /** Apply the shared ReactorTERenderer in-world base transform so the model is oriented as authored. */
    private static void baseTransform(PoseStack pose) {
        pose.translate(0.0, 2.0, 1.0);
        pose.scale(1.0F, -1.0F, -1.0F);
        pose.translate(0.5, 0.5, 0.5);
    }

    private static Identifier tex(String name) {
        return Identifier.fromNamespaceAndPath(ReactorCraft.MODID, "textures/tileentity/" + name + ".png");
    }

    /** Codec-backed unbaked form. Resolved at bake() against the live model layers. */
    public record Unbaked(String machine) implements NoDataSpecialModelRenderer.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Codec.STRING.fieldOf("machine").forGetter(Unbaked::machine)
        ).apply(i, Unbaked::new));

        @Override
        public MapCodec<? extends NoDataSpecialModelRenderer.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public ReactorMachineItemRenderer bake(SpecialModelRenderer.BakingContext context) {
            EntityModelSet set = context.entityModelSet();
            switch (machine) {
                case "control_rod" -> {
                    ModelControl m = new ModelControl(set.bakeLayer(ReactorModelLayers.CONTROL_ROD));
                    return make((p, vc, l) -> m.renderAll(p, vc, l, -5F), tex("control")); // rod fully inserted
                }
                case "toroid_magnet" -> {
                    ModelMagnet m = new ModelMagnet(set.bakeLayer(ReactorModelLayers.MAGNET));
                    return make((p, vc, l) -> m.renderAll(p, vc, l), tex("magnet"), 0.5F, -20F);
                }
                case "solenoid_magnet" -> {
                    ModelSolenoid m = new ModelSolenoid(set.bakeLayer(ReactorModelLayers.SOLENOID));
                    return make((p, vc, l) -> m.renderAll(p, vc, l), tex("solenoid"), 0.3F, 0F);
                }
                case "steam_grate" -> {
                    ModelSteamGrate m = new ModelSteamGrate(set.bakeLayer(ReactorModelLayers.STEAM_GRATE));
                    return make((p, vc, l) -> m.renderAll(p, vc, l), tex("steamgrate"));
                }
                case "condenser" -> {
                    ModelCondenser m = new ModelCondenser(set.bakeLayer(ReactorModelLayers.CONDENSER));
                    return make((p, vc, l) -> m.renderAll(p, vc, l), tex("condenser"));
                }
                case "turbine_core" -> {
                    ModelTurbine m = new ModelTurbine(set.bakeLayer(ReactorModelLayers.TURBINE_STAGES[0]), 0);
                    return make((p, vc, l) -> m.renderAll(p, vc, l, 0F), tex("turbine"));
                }
                case "waste_storage" -> {
                    ModelWasteStorage m = new ModelWasteStorage(set.bakeLayer(ReactorModelLayers.WASTE_STORAGE));
                    return make((p, vc, l) -> m.renderAll(p, vc, l), tex("storage"));
                }
                case "electrolyzer" -> {
                    ModelElectrolyzer m = new ModelElectrolyzer(set.bakeLayer(ReactorModelLayers.ELECTROLYZER));
                    return make((p, vc, l) -> m.renderAll(p, vc, l), tex("electrolyzer"));
                }
                case "solar_exchanger" -> {
                    ModelSolarExchanger m = new ModelSolarExchanger(set.bakeLayer(ReactorModelLayers.SOLAR_EXCHANGER));
                    return make((p, vc, l) -> m.renderAll(p, vc, l), tex("solar"));
                }
                case "heavy_pump" -> {
                    ModelHeavyPump m = new ModelHeavyPump(set.bakeLayer(ReactorModelLayers.HEAVY_PUMP));
                    return make((p, vc, l) -> m.renderAll(p, vc, l, 0F), tex("heavypump"));
                }
                case "isotope_centrifuge" -> {
                    ModelCentrifuge m = new ModelCentrifuge(set.bakeLayer(ReactorModelLayers.CENTRIFUGE));
                    return make((p, vc, l) -> m.renderAll(p, vc, l, 0F), tex("centrifuge"));
                }
                case "uranium_processor" -> {
                    ModelProcessor m = new ModelProcessor(set.bakeLayer(ReactorModelLayers.PROCESSOR));
                    return make((p, vc, l) -> m.renderAll(p, vc, l), tex("processor"));
                }
                case "reactor_pump" -> {
                    ModelReactorPump m = new ModelReactorPump(set.bakeLayer(ReactorModelLayers.REACTOR_PUMP));
                    return make((p, vc, l) -> m.renderAll(p, vc, l, 0F), tex("pump"));
                }
                case "heat_exchanger" -> {
                    ModelExchanger m = new ModelExchanger(set.bakeLayer(ReactorModelLayers.EXCHANGER));
                    return make((p, vc, l) -> m.renderAll(p, vc, l), tex("exchanger2"));
                }
                case "gas_collector" -> {
                    ModelGasCollector m = new ModelGasCollector(set.bakeLayer(ReactorModelLayers.GAS_COLLECTOR));
                    return make((p, vc, l) -> m.renderAll(p, vc, l, true), tex("co2collector"));
                }
                default -> {
                    ReactorCraft.LOGGER.warn("Unknown machine '{}' for item renderer", machine);
                    return null;
                }
            }
        }

        private static ReactorMachineItemRenderer make(Draw inner, Identifier texture) {
            return make(inner, texture, DEFAULT_SCALE, 0F);
        }

        private static ReactorMachineItemRenderer make(Draw inner, Identifier texture, float scale, float extraYawDeg) {
            return new ReactorMachineItemRenderer((pose, vc, light) -> {
                pose.pushPose();
                baseTransform(pose);
                inner.render(pose, vc, light);
                pose.popPose();
            }, texture, scale, extraYawDeg);
        }
    }
}
