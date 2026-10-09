/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.renders;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import reika.reactorcraft.base.ReactorTERenderer;
import reika.reactorcraft.models.ModelTurbine;
import reika.reactorcraft.registry.ReactorModelLayers;
import reika.reactorcraft.tileentities.powergen.TileEntityTurbineCore;

/** Captures formed state, wheel tier and wear before deferred 26.3 geometry submission. */
public class RenderTurbine extends ReactorTERenderer<TileEntityTurbineCore> {
    private static final Identifier CAP = Identifier.parse("reactorcraft:textures/block/multi/turbine_3.png");
    private final WheelDraw[] models;
    private final Identifier texture;

    @FunctionalInterface
    protected interface WheelDraw {
        void render(PoseStack pose, VertexConsumer vertices, int light, float spin, int damage);
    }
    private static final class State extends BlockEntityRenderState {
        Direction facing = Direction.SOUTH;
        int stage, damage;
        boolean formed;
        float spin;
    }

    public RenderTurbine(BlockEntityRendererProvider.Context context) {
        this(Identifier.parse("reactorcraft:textures/tileentity/turbine.png"),
                ReactorModelLayers.TURBINE_STAGES.length, stage -> {
                    var model = new ModelTurbine(context.bakeLayer(ReactorModelLayers.TURBINE_STAGES[stage]), stage);
                    return model::renderAll;
                });
    }

    protected RenderTurbine(Identifier texture, int stages, java.util.function.IntFunction<WheelDraw> factory) {
        this.texture = texture;
        models = new WheelDraw[stages];
        for (int stage = 0; stage < stages; stage++) models[stage] = factory.apply(stage);
    }

    @Override public BlockEntityRenderState createRenderState() { return new State(); }

    @Override
    public void extractRenderState(TileEntityTurbineCore tile, BlockEntityRenderState state, float partialTick,
            Vec3 camera, ModelFeatureRenderer.CrumblingOverlay breaking) {
        super.extractRenderState(tile, state, partialTick, camera, breaking);
        State wheel = (State) state;
        wheel.facing = tile.getFacing();
        wheel.stage = Math.clamp(tile.getStage(), 0, models.length - 1);
        wheel.damage = Math.max(0, tile.getDamage());
        // Preserve the original remote-player cosmetic wear, captured before deferred rendering.
        if (tile.hasLevel()) {
            var minecraft = net.minecraft.client.Minecraft.getInstance();
            if (minecraft.player != null && "waterlubber".equals(minecraft.player.getName().getString())
                    && (System.identityHashCode(tile) & 1) == 0 && !minecraft.hasSingleplayerServer())
                wheel.damage = (int)Math.min(Integer.MAX_VALUE, wheel.damage + 2L);
        }
        wheel.formed = tile.hasMultiBlock();
        int omega = tile.getRenderOmega();
        double time = tile.hasLevel() ? tile.getLevel().getGameTime() + partialTick : 0;
        wheel.spin = omega > 0 ? (float) ((-tile.getRenderAnimationSpeed() * Math.pow(Math.log(omega + 1.0) / Math.log(2), 1.05) * time) % 360) : 0;
    }

    @Override
    public void submit(BlockEntityRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        State wheel = (State) state;
        Direction facing = wheel.facing;
        int light = state.lightCoords, damage = wheel.damage;
        float spin = wheel.spin;
        if (wheel.formed) {
            WheelDraw model = models[wheel.stage];
            collector.submitCustomGeometry(pose, RenderTypes.entitySolid(texture), (submittedPose, vertices) -> {
                PoseStack captured = new PoseStack(); captured.last().set(submittedPose);
                TurbineHousingGeometry.orient(captured, facing);
                model.render(captured, vertices, light, spin, damage);
            });
        } else {
            collector.submitCustomGeometry(pose, RenderTypes.entitySolid(CAP), (submittedPose, vertices) -> {
                PoseStack captured = new PoseStack(); captured.last().set(submittedPose);
                TurbineHousingGeometry.render(captured, vertices, light, facing);
            });
        }
    }
}
