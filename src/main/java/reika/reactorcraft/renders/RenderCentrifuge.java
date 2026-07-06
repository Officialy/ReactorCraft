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

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;

import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.base.ReactorTERenderer;
import reika.reactorcraft.models.ModelCentrifuge;
import reika.reactorcraft.registry.ReactorModelLayers;
import reika.reactorcraft.tileentities.processing.TileEntityCentrifuge;

/**
 * The legacy shaft-IO connector overlay ({@code IORenderer.renderIO}) is not ported — ReactorCraft
 * tile entities don't yet implement RotaryCraft's {@code BlockEntityIOMachine} interface.
 */
public class RenderCentrifuge extends ReactorTERenderer<TileEntityCentrifuge> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(ReactorCraft.MODID, "textures/tileentity/centrifuge.png");

    private final ModelCentrifuge model;

    public RenderCentrifuge(BlockEntityRendererProvider.Context context) {
        model = new ModelCentrifuge(context.bakeLayer(ReactorModelLayers.CENTRIFUGE));
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
        model.renderAll(stack, vc, light, -spinAngle((TileEntityCentrifuge) be));
        stack.popPose();
    }

    /**
     * Client-derived spin angle -- the client BE never ticks, so {@code te.phi} only jumps once per
     * sync packet. Legacy {@code animateWithTick} accumulated phi per-tick in omega-dependent steps
     * (each bracket's contribution is cumulative with the two trailing independent {@code if}s);
     * reproduce that same step function as a degrees-per-tick rate from the game clock.
     */
    private static float spinAngle(TileEntityCentrifuge te) {
        int omega = te.getOmega();
        float degPerTick = 0F;
        if (omega >= 262144)
            degPerTick += 40F;
        else if (omega >= 65536)
            degPerTick += 30F;
        else if (omega >= 16384)
            degPerTick += 20F;
        else if (omega >= 4096)
            degPerTick += 15F;
        if (omega >= 1024)
            degPerTick += 10F;
        if (omega >= 256)
            degPerTick += 7F;
        else if (omega > 0)
            degPerTick += 5F;
        if (degPerTick <= 0F)
            return 0F;
        Minecraft mc = Minecraft.getInstance();
        double t = (mc.level != null ? mc.level.getGameTime() : 0L)
                + mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        return (float) ((t * degPerTick) % 360.0);
    }
}
