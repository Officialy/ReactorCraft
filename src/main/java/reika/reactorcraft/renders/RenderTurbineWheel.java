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
import reika.reactorcraft.models.ModelFlywheel;
import reika.reactorcraft.registry.ReactorModelLayers;
import reika.reactorcraft.tileentities.TileEntityReactorFlywheel;
import reika.reactorcraft.tileentities.powergen.TileEntityTurbineCore;

public class RenderTurbineWheel extends ReactorTERenderer<TileEntityReactorFlywheel> {
    private static final Identifier WHEEL = Identifier.parse("reactorcraft:textures/tileentity/flywheel.png");
    private static final Identifier CAP = Identifier.parse("reactorcraft:textures/block/multi/flywheel_1.png");
    private final ModelFlywheel model;
    private static final class State extends BlockEntityRenderState {
        Direction facing = Direction.SOUTH;
        boolean formed;
        float spin;
    }
    public RenderTurbineWheel(BlockEntityRendererProvider.Context context) {
        model = new ModelFlywheel(context.bakeLayer(ReactorModelLayers.FLYWHEEL));
    }
    @Override public BlockEntityRenderState createRenderState() { return new State(); }
    @Override
    public void extractRenderState(TileEntityReactorFlywheel tile, BlockEntityRenderState state, float partialTick,
            Vec3 camera, ModelFeatureRenderer.CrumblingOverlay breaking) {
        super.extractRenderState(tile, state, partialTick, camera, breaking);
        State wheel = (State) state;
        wheel.facing = tile.getFacing(); wheel.formed = tile.hasMultiBlock(); wheel.spin = 0;
        if (tile.hasLevel()) {
            var adjacent = tile.getLevel().getBlockEntity(tile.getBlockPos().relative(tile.getFacing()));
            if (adjacent instanceof TileEntityTurbineCore turbine && turbine.getRenderOmega() > 0) {
                int omega = turbine.getRenderOmega();
                double time = tile.getLevel().getGameTime() + partialTick;
                wheel.spin = (float) ((6 * turbine.getRenderAnimationSpeed() * Math.pow(Math.log(omega + 1.0) / Math.log(2), 1.05) * time) % 360);
            }
        }
    }
    @Override
    public void submit(BlockEntityRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        State wheel = (State) state;
        Direction facing = wheel.facing; boolean formed = wheel.formed;
        float spin = wheel.spin; int light = state.lightCoords;
        collector.submitCustomGeometry(pose, RenderTypes.entitySolid(formed ? WHEEL : CAP), (submittedPose, vertices) -> {
            PoseStack captured = new PoseStack(); captured.last().set(submittedPose);
            if (formed) {
                TurbineHousingGeometry.orient(captured, facing);
                model.renderAll(captured, vertices, light, spin);
            } else {
                TurbineHousingGeometry.render(captured, vertices, light, facing);
            }
        });
    }
}
