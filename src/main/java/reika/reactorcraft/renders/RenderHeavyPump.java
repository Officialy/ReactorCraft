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
import reika.reactorcraft.models.ModelHeavyPump;
import reika.reactorcraft.registry.ReactorModelLayers;
import reika.reactorcraft.tileentities.TileEntityHeavyPump;

/**
 * The legacy shaft-IO connector overlay ({@code IORenderer.renderIO}) is not ported — ReactorCraft
 * tile entities don't yet implement RotaryCraft's {@code BlockEntityIOMachine} interface.
 */
public class RenderHeavyPump extends ReactorTERenderer<TileEntityHeavyPump> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(ReactorCraft.MODID, "textures/tileentity/heavypump.png");

    private final ModelHeavyPump model;

    public RenderHeavyPump(BlockEntityRendererProvider.Context context) {
        model = new ModelHeavyPump(context.bakeLayer(ReactorModelLayers.HEAVY_PUMP));
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
        model.renderAll(stack, vc, light, -spinAngle((TileEntityHeavyPump) be));
        stack.popPose();
    }

    /**
     * Client-derived spin angle -- the client BE never ticks, so {@code te.phi} only jumps once per
     * sync packet. Legacy accumulated {@code phi += 10F} per tick while powered; reproduce that rate
     * from the game clock, gated on the same power/torque threshold as {@code animateWithTick}.
     */
    private static float spinAngle(TileEntityHeavyPump te) {
        if (te.getPower() < TileEntityHeavyPump.MINPOWER || te.getTorque() < TileEntityHeavyPump.MINTORQUE)
            return 0F;
        Minecraft mc = Minecraft.getInstance();
        double t = (mc.level != null ? mc.level.getGameTime() : 0L)
                + mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        return (float) ((t * 10.0) % 360.0);
    }
}
