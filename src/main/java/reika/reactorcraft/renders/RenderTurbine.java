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
import net.minecraft.world.level.block.entity.BlockEntity;

import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.base.ReactorTERenderer;
import reika.reactorcraft.models.ModelTurbine;
import reika.reactorcraft.registry.ReactorModelLayers;
import reika.reactorcraft.tileentities.powergen.TileEntityTurbineCore;

/**
 * 26.2 port of the steam-turbine BER. Orients the wheel to {@link TileEntityTurbineCore#getFacing()}
 * (legacy ORIENT mapping: WEST=90, EAST=270, NORTH=180, SOUTH=0). Texture {@code turbine.png}.
 *
 * Deferred: the wheel spin (legacy rotated the whole wheel about Z by phi) needs the client phi ticker;
 * and per-damage-stage / multi-tier blade models (uses the base stage-0 model). Drawn static.
 */
public class RenderTurbine extends ReactorTERenderer<TileEntityTurbineCore> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(ReactorCraft.MODID, "textures/tileentity/turbine.png");

    private final ModelTurbine model;

    public RenderTurbine(BlockEntityRendererProvider.Context context) {
        model = new ModelTurbine(context.bakeLayer(ReactorModelLayers.TURBINE));
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
        stack.mulPose(Axis.YP.rotationDegrees(facingAngle(((TileEntityTurbineCore) be).getFacing())));
        model.renderAll(stack, vc, light, spinAngle((TileEntityTurbineCore) be));
        stack.popPose();
    }

    /**
     * Client-side spin angle for the blade wheel. The BE's own {@code phi} can't be used: the client
     * BE doesn't tick (getTicker is null client-side), so {@code animateWithTick} never advances it.
     * Instead derive the angle from the synced {@code omega} and the game clock, matching the legacy
     * rate {@code phi += 0.2 * log2(omega+1)^1.05} per tick.
     */
    private static float spinAngle(TileEntityTurbineCore te) {
        int omega = te.getOmega();
        if (omega <= 0)
            return 0F;
        double degPerTick = 0.2 * Math.pow(Math.log(omega + 1) / Math.log(2), 1.05);
        Minecraft mc = Minecraft.getInstance();
        double t = (mc.level != null ? mc.level.getGameTime() : 0L)
                + mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        return (float) ((t * degPerTick) % 360.0);
    }
}
