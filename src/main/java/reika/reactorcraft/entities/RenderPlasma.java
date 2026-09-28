/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;

import reika.reactorcraft.ReactorCraft;

/** Camera-facing fullbright plasma billboard (the legacy additive quad, plasma7.png). */
public class RenderPlasma extends EntityRenderer<EntityPlasma, RenderPlasma.PlasmaRenderState> {

	private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(ReactorCraft.MODID, "textures/plasma7.png");

	public RenderPlasma(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public PlasmaRenderState createRenderState() {
		return new PlasmaRenderState();
	}

	@Override
	public void extractRenderState(EntityPlasma entity, PlasmaRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.size = 2.0F;
	}

	@Override
	public void submit(PlasmaRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		submitBillboard(state.size, TEXTURE, poseStack, collector, camera);
	}

	/** Shared camera-facing textured quad for the plasma/fusion/radiation effect entities. */
	static void submitBillboard(float size, Identifier texture, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		if (size <= 0)
			return;
		poseStack.pushPose();
		poseStack.rotate(camera.orientation);
		float h = size / 2F;
		collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(texture), (pose, buf) -> emitQuad(pose, buf, h));
		poseStack.popPose();
	}

	private static void emitQuad(PoseStack.Pose pose, VertexConsumer b, float h) {
		int light = 0xF000F0;
		b.addVertex(pose, -h, -h, 0).setColor(255, 255, 255, 200).setUv(0, 1).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
		b.addVertex(pose, h, -h, 0).setColor(255, 255, 255, 200).setUv(1, 1).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
		b.addVertex(pose, h, h, 0).setColor(255, 255, 255, 200).setUv(1, 0).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
		b.addVertex(pose, -h, h, 0).setColor(255, 255, 255, 200).setUv(0, 0).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
	}

	public static class PlasmaRenderState extends EntityRenderState {
		public float size = 2.0F;
	}
}
