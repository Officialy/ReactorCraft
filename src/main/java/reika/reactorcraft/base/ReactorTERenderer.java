/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.base;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import reika.dragonapi.base.BlockEntityBase;
import reika.dragonapi.base.BlockEntityRenderBase;
import reika.dragonapi.base.DragonAPIMod;
import reika.reactorcraft.ReactorCraft;
import reika.rotarycraft.RotaryCraft;

/**
 * 26.2 base for ReactorCraft machine block-entity renderers — the ReactorCraft analogue of
 * {@code reika.rotarycraft.base.RotaryTERenderer}. Simple machines override {@link #getSubmitTexture}
 * + {@link #renderModel} and inherit the {@link #submit} pipeline (snapshot pose, single RenderType
 * from the texture, {@code submitCustomGeometry}). The neighbour-light override stops the model from
 * rendering pure black inside its own opaque host block.
 */
public abstract class ReactorTERenderer<TE extends BlockEntity> extends BlockEntityRenderBase<TE> {

	@Override
	protected Class<?> getModClass() {
		return ReactorCraft.class;
	}

	@Override
	protected String getModID() {
		return ReactorCraft.MODID;
	}

	@Override
	protected boolean loadXmasTextures() {
		return false;
	}

	// ReactorCraft's main class is not a DragonAPIMod; getOwnerMod is only read by commented-out
	// ModLockController code in the base, so any non-null DragonAPIMod is fine. RotaryCraft (a hard
	// dependency) provides one.
	@Override
	protected final DragonAPIMod getOwnerMod() {
		return RotaryCraft.getInstance();
	}

	@Override
	protected final boolean doRenderModel(PoseStack stack, BlockEntityBase te) {
		return this.isValidMachineRenderPass(te);
	}

	/** Texture for the single RenderType the model is drawn with; null suppresses rendering. */
	protected Identifier getSubmitTexture(BlockEntity be) {
		return null;
	}

	protected boolean useEntityCutout() {
		return false;
	}

	/** Draw the model into the provided buffer; PoseStack is already a snapshot at the block origin. */
	protected void renderModel(PoseStack stack, BlockEntity be, VertexConsumer vc, int light) {
	}

	// Machine blocks are opaque, so the light sampled at their own position is 0 and the BER model
	// renders black. Sample the brightest neighbour instead so the model is lit like its surroundings.
	@Override
	public void extractRenderState(TE be, BlockEntityRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress);
		Level level = be.getLevel();
		if (level == null)
			return;
		BlockPos pos = be.getBlockPos();
		int best = state.lightCoords;
		for (Direction d : Direction.values()) {
			int l = LightCoordsUtil.getLightCoords(level, pos.relative(d));
			if (l > best)
				best = l;
		}
		state.lightCoords = best;
	}

	@Override
	public void submit(BlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		Level level = Minecraft.getInstance().level;
		if (level == null) return;
		BlockEntity be = level.getBlockEntity(state.blockPos);
		if (!(be instanceof BlockEntityBase bbe)) return;
		if (!this.doRenderModel(poseStack, bbe)) return;

		Identifier tex = this.getSubmitTexture(be);
		if (tex == null) return;

		RenderType rt = this.useEntityCutout() ? RenderTypes.entityCutout(tex) : RenderTypes.entitySolid(tex);
		PoseStack snapped = new PoseStack();
		snapped.last().set(poseStack.last());
		int light = state.lightCoords;
		collector.submitCustomGeometry(poseStack, rt, (pose, vc) -> this.renderModel(snapped, be, vc, light));
	}
}
