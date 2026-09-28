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
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;

import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.base.ReactorTERenderer;
import reika.reactorcraft.models.ModelBigTurbine;
import reika.reactorcraft.registry.ReactorModelLayers;
import reika.reactorcraft.tileentities.powergen.TileEntityTurbineCore;

/**
 * 26.2 port of the high-pressure turbine BER (legacy {@code RenderBigTurbine extends RenderTurbine}).
 * Same orientation/spin logic as {@link RenderTurbine}; texture {@code bigturbine.png}.
 */
public class RenderBigTurbine extends ReactorTERenderer<TileEntityTurbineCore> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(ReactorCraft.MODID, "textures/tileentity/bigturbine.png");

    // One model per stage (0..MAX_STAGE) — see ModelTurbine/RenderTurbine for why.
    private final ModelBigTurbine[] models;

    public RenderBigTurbine(BlockEntityRendererProvider.Context context) {
        models = new ModelBigTurbine[ReactorModelLayers.BIG_TURBINE_STAGES.length];
        for (int i = 0; i < models.length; i++)
            models[i] = new ModelBigTurbine(context.bakeLayer(ReactorModelLayers.BIG_TURBINE_STAGES[i]), i);
    }

    @Override
    protected Identifier getSubmitTexture(BlockEntity be) {
        return TEXTURE;
    }

    private static float facingAngle(Direction d) {
        return switch (d) {
            case WEST -> 90.0F;
            case EAST -> 270.0F;
            case NORTH -> 180.0F;
            default -> 0.0F; // SOUTH + vertical fallback
        };
    }

    @Override
    protected void renderModel(PoseStack stack, BlockEntity be, VertexConsumer vc, int light) {
        stack.pushPose();
        stack.translate(0.0, 2.0, 1.0);
        stack.scale(1.0F, -1.0F, -1.0F);
        stack.translate(0.5, 0.5, 0.5);
        TileEntityTurbineCore turb = (TileEntityTurbineCore) be;
        stack.rotate(Axis.YP.rotationDegrees(facingAngle(turb.getFacing())));
        int stage = Mth.clamp(turb.getStage(), 0, models.length - 1);
        models[stage].renderAll(stack, vc, light, spinAngle(turb));
        stack.popPose();
    }

    /** See RenderTurbine.spinAngle: the client BE never ticks, so derive spin from omega + game clock. */
    private static float spinAngle(TileEntityTurbineCore te) {
        int omega = te.getRenderOmega();
        if (omega <= 0)
            return 0F;
        double degPerTick = 0.2 * Math.pow(Math.log(omega + 1) / Math.log(2), 1.05);
        Minecraft mc = Minecraft.getInstance();
        double t = (mc.level != null ? mc.level.getGameTime() : 0L)
                + mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        return (float) ((t * degPerTick) % 360.0);
    }
}
