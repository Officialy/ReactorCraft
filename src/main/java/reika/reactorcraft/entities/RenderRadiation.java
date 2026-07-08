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

/** Camera-facing radiation-field billboard, sized to the field's range like the legacy quad. */
public class RenderRadiation extends EntityRenderer<EntityRadiation, RenderRadiation.RadiationRenderState> {

	private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(ReactorCraft.MODID, "textures/radiation.png");

	public RenderRadiation(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public RadiationRenderState createRenderState() {
		return new RadiationRenderState();
	}

	@Override
	public void extractRenderState(EntityRadiation entity, RadiationRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.size = Math.min(20, entity.getRange());
	}

	@Override
	public void submit(RadiationRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		RenderPlasma.submitBillboard(state.size, TEXTURE, poseStack, collector, camera);
	}

	public static class RadiationRenderState extends EntityRenderState {
		public float size;
	}
}
