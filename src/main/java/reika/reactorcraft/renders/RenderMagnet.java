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

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;

import reika.dragonapi.libraries.rendering.ReikaColorAPI;
import reika.dragonapi.libraries.rendering.ReikaRenderHelper;
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
        stack.rotate(Axis.YP.rotationDegrees(ang));
        model.renderAll(stack, vc, light);
        stack.popPose();
    }

    // Draw the ring (super) plus the aim indicator: a cyan arrow + concentric range circles pointing
    // along the magnet's aim, in upright world space (NOT the flipped model transform). Faithful to the
    // legacy renderAngleLine; the indicator fades as TileEntityToroidMagnet.alpha decays (refreshed on aim).
    @Override
    public void submit(BlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null)
            return;
        BlockEntity be = mc.level.getBlockEntity(state.blockPos);
        if (!(be instanceof TileEntityToroidMagnet tile) || !tile.isInWorld())
            return;
        int a = Math.min(255, tile.getAlpha());
        if (a <= 0)
            return;

        PoseStack ps = new PoseStack();
        ps.last().set(poseStack.last());
        ps.translate(0.5, 0.5, 0.5);
        ps.rotate(Axis.YP.rotationDegrees(tile.getAngle() + 90.0F));

        int[] arrow = {100, 192, 255, a};
        ReikaRenderHelper.renderLine(collector, ps, 0, 0.1, 0, 4, 0.1, 0, arrow);
        ReikaRenderHelper.renderLine(collector, ps, 3.5, 0.1, 0.5, 4, 0.1, 0, arrow);
        ReikaRenderHelper.renderLine(collector, ps, 3.5, 0.1, -0.5, 4, 0.1, 0, arrow);

        int white = ReikaColorAPI.RGBtoHex(255, 255, 255, a);
        for (int i = 1; i < 4; i++)
            ReikaRenderHelper.renderVCircle(collector, ps, i, 0, 0, 0, white, 90, 10);
    }
}
