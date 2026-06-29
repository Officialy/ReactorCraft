/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 ******************************************************************************/
package reika.reactorcraft.renders;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;

import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.base.ReactorTERenderer;
import reika.reactorcraft.models.ModelControl;
import reika.reactorcraft.registry.ReactorModelLayers;
import reika.reactorcraft.tileentities.fission.TileEntityControlRod;

/**
 * 26.2 port of the control-rod BER. The rod assembly slides vertically with the rod's insertion
 * depth ({@link TileEntityControlRod#getRodPosition()}); the housing stays fixed. Texture {@code control.png}.
 */
public class RenderControl extends ReactorTERenderer<TileEntityControlRod> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(ReactorCraft.MODID, "textures/tileentity/control.png");

    private final ModelControl model;

    public RenderControl(BlockEntityRendererProvider.Context context) {
        model = new ModelControl(context.bakeLayer(ReactorModelLayers.CONTROL_ROD));
    }

    @Override
    protected Identifier getSubmitTexture(BlockEntity be) {
        return TEXTURE;
    }

    @Override
    protected void renderModel(PoseStack stack, BlockEntity be, VertexConsumer vc, int light) {
        stack.pushPose();
        stack.translate(0.0, 2.0, 1.0);
        stack.scale(1.0F, -1.0F, -1.0F);
        stack.translate(0.5, 0.5, 0.5);
        model.renderAll(stack, vc, light, ((TileEntityControlRod) be).getRodPosition());
        stack.popPose();
    }
}
