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
import reika.reactorcraft.models.ModelDiffuser;
import reika.reactorcraft.registry.ReactorModelLayers;
import reika.reactorcraft.tileentities.TileEntitySteamDiffuser;

/**
 * 26.2 port of the steam diffuser BER (legacy RenderSteamDiffuser). Static box model, no moving
 * parts; texture {@code diffuser.png}.
 */
public class RenderSteamDiffuser extends ReactorTERenderer<TileEntitySteamDiffuser> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(ReactorCraft.MODID, "textures/tileentity/diffuser.png");

    private final ModelDiffuser model;

    public RenderSteamDiffuser(BlockEntityRendererProvider.Context context) {
        model = new ModelDiffuser(context.bakeLayer(ReactorModelLayers.STEAM_DIFFUSER));
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
        TileEntitySteamDiffuser diffuser = (TileEntitySteamDiffuser) be;
        stack.mulPose(Axis.YP.rotationDegrees(facingAngle(diffuser.getFacing())));
        model.renderAll(stack, vc, light);
        stack.popPose();
    }
}
