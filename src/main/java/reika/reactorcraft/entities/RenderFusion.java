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

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;

import reika.reactorcraft.ReactorCraft;

/** Camera-facing fusion flash billboard; shrinks over the burst's ~20-tick life like the legacy quad. */
public class RenderFusion extends EntityRenderer<EntityFusion, RenderFusion.FusionRenderState> {

	private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(ReactorCraft.MODID, "textures/fusion.png");

	public RenderFusion(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public FusionRenderState createRenderState() {
		return new FusionRenderState();
	}

	@Override
	public void extractRenderState(EntityFusion entity, FusionRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.size = 2F - (entity.tickCount + partialTicks) / 10F;
	}

	@Override
	public void submit(FusionRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		RenderPlasma.submitBillboard(state.size, TEXTURE, poseStack, collector, camera);
	}

	public static class FusionRenderState extends EntityRenderState {
		public float size;
	}
}
