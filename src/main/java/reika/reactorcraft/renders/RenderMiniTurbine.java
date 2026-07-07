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
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;

import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.base.ReactorTERenderer;
import reika.reactorcraft.models.ModelMiniTurbine;
import reika.reactorcraft.registry.ReactorModelLayers;
import reika.reactorcraft.tileentities.powergen.TileEntityTurbineCore;

/**
 * 26.2 port of the mini/centrifugal turbine BER (legacy {@code RenderMiniTurbine extends RenderTurbine}).
 * Same orientation as {@link RenderTurbine}; texture {@code miniturbine3.png}. The model itself has no
 * per-stage variation (static strut cage, no spinning blades — see ModelMiniTurbine), so a single
 * baked model is reused for every stage.
 */
public class RenderMiniTurbine extends ReactorTERenderer<TileEntityTurbineCore> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(ReactorCraft.MODID, "textures/tileentity/miniturbine3.png");

    private final ModelMiniTurbine model;

    public RenderMiniTurbine(BlockEntityRendererProvider.Context context) {
        model = new ModelMiniTurbine(context.bakeLayer(ReactorModelLayers.MINI_TURBINE));
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
        stack.mulPose(Axis.YP.rotationDegrees(facingAngle(turb.getFacing())));
        model.renderAll(stack, vc, light);
        stack.popPose();
    }
}
