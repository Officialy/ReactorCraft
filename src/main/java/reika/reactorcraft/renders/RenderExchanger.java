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
import reika.reactorcraft.models.ModelExchanger;
import reika.reactorcraft.registry.ReactorModelLayers;
import reika.reactorcraft.tileentities.powergen.TileEntityHeatExchanger;

/**
 * The legacy shaft-IO connector overlay and the hot/cold fluid quad overlay (legacy
 * {@code renderFluid}, driven by {@code getCurrentRecipe()}) are not ported -- the former needs
 * {@code BlockEntityIOMachine}, the latter a general BER fluid-quad helper; neither exists yet.
 */
public class RenderExchanger extends ReactorTERenderer<TileEntityHeatExchanger> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(ReactorCraft.MODID, "textures/tileentity/exchanger2.png");

    private final ModelExchanger model;

    public RenderExchanger(BlockEntityRendererProvider.Context context) {
        model = new ModelExchanger(context.bakeLayer(ReactorModelLayers.EXCHANGER));
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
        model.renderAll(stack, vc, light);
        stack.popPose();
    }
}
