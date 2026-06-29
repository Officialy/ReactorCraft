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
import com.mojang.math.Axis;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;

import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.base.ReactorTERenderer;
import reika.reactorcraft.models.ModelMagnet;
import reika.reactorcraft.registry.ReactorModelLayers;
import reika.reactorcraft.tileentities.fusion.TileEntityToroidMagnet;

/**
 * 26.2 port of the toroid-magnet (fusion ring) BER. Mirrors {@link RenderCondenser}'s base transform
 * ({@code translate(0,2,1) · scale(1,-1,-1) · translate(0.5,0.5,0.5)}) and adds the legacy aim
 * rotation: the ring spins about Y by {@code 90 - tile.getAngle()} to face its toroid centre.
 *
 * Deferred: the legacy {@code renderAngleLine} (a cyan aim line + range circles shown only while the
 * player wears IO goggles) — a separate line-geometry overlay, not needed for the ring itself.
 */
public class RenderMagnet extends ReactorTERenderer<TileEntityToroidMagnet> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(ReactorCraft.MODID, "textures/tileentity/magnet.png");

    private final ModelMagnet model;

    public RenderMagnet(BlockEntityRendererProvider.Context context) {
        model = new ModelMagnet(context.bakeLayer(ReactorModelLayers.MAGNET));
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
        float ang = 90.0F - ((TileEntityToroidMagnet) be).getAngle();
        stack.mulPose(Axis.YP.rotationDegrees(ang));
        model.renderAll(stack, vc, light);
        stack.popPose();
    }
}
