/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 ******************************************************************************/
package reika.reactorcraft.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;

import reika.reactorcraft.entities.EntityNeutron.NeutronSpeed;
import reika.rotarycraft.renders.RotaryRenderPipelines;

/**
 * 26.2 port of the neutron renderer. The legacy renderer drew a small camera-facing coloured quad;
 * here we emit a tiny no-depth coloured cube (always visible — handy for seeing fission activity)
 * via RotaryCraft's {@code NO_DEPTH_FILLED_BOX_TYPE}. Blue for slow/thermal neutrons, bright cyan-blue
 * for fast ones, matching the legacy colours.
 */
public class RenderNeutron extends EntityRenderer<EntityNeutron, RenderNeutron.NeutronRenderState> {

    private static final float S = 0.12F;

    public RenderNeutron(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public NeutronRenderState createRenderState() {
        return new NeutronRenderState();
    }

    @Override
    public void extractRenderState(EntityNeutron entity, NeutronRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.color = entity.getNeutronSpeed() == NeutronSpeed.FAST ? 0xFF22AAFF : 0xFF0000AA;
    }

    @Override
    public void submit(NeutronRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        int rgba = state.color;
        collector.submitCustomGeometry(poseStack, RotaryRenderPipelines.NO_DEPTH_FILLED_BOX_TYPE,
                (pose, buf) -> emitCube(pose, buf, -S, -S, -S, S, S, S, rgba));
    }

    private static void emitCube(PoseStack.Pose pose, VertexConsumer b, float x0, float y0, float z0, float x1, float y1, float z1, int rgba) {
        b.addVertex(pose, x0, y0, z0).setColor(rgba); b.addVertex(pose, x1, y0, z0).setColor(rgba); b.addVertex(pose, x1, y0, z1).setColor(rgba); b.addVertex(pose, x0, y0, z1).setColor(rgba);
        b.addVertex(pose, x0, y1, z1).setColor(rgba); b.addVertex(pose, x1, y1, z1).setColor(rgba); b.addVertex(pose, x1, y1, z0).setColor(rgba); b.addVertex(pose, x0, y1, z0).setColor(rgba);
        b.addVertex(pose, x0, y0, z0).setColor(rgba); b.addVertex(pose, x0, y1, z0).setColor(rgba); b.addVertex(pose, x1, y1, z0).setColor(rgba); b.addVertex(pose, x1, y0, z0).setColor(rgba);
        b.addVertex(pose, x1, y0, z1).setColor(rgba); b.addVertex(pose, x1, y1, z1).setColor(rgba); b.addVertex(pose, x0, y1, z1).setColor(rgba); b.addVertex(pose, x0, y0, z1).setColor(rgba);
        b.addVertex(pose, x0, y0, z1).setColor(rgba); b.addVertex(pose, x0, y1, z1).setColor(rgba); b.addVertex(pose, x0, y1, z0).setColor(rgba); b.addVertex(pose, x0, y0, z0).setColor(rgba);
        b.addVertex(pose, x1, y0, z0).setColor(rgba); b.addVertex(pose, x1, y1, z0).setColor(rgba); b.addVertex(pose, x1, y1, z1).setColor(rgba); b.addVertex(pose, x1, y0, z1).setColor(rgba);
    }

    /** Carries the per-neutron colour (fast vs slow) into the submit pass. */
    public static class NeutronRenderState extends EntityRenderState {
        public int color = 0xFF0000AA;
    }
}
