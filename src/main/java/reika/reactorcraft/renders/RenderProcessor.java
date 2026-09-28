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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.base.ReactorTERenderer;
import reika.reactorcraft.blocks.BlockReactorMachine;
import reika.reactorcraft.models.ModelProcessor;
import reika.reactorcraft.registry.ReactorModelLayers;
import reika.reactorcraft.tileentities.processing.TileEntityUProcessor;

/**
 * The tank fluid rendering (input/intermediate/output liquid boxes, legacy {@code renderLiquids})
 * is not ported -- it needs a general BER fluid-quad helper that doesn't exist yet in this port.
 */
public class RenderProcessor extends ReactorTERenderer<TileEntityUProcessor> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(ReactorCraft.MODID, "textures/tileentity/processor.png");

    private final ModelProcessor model;

    public RenderProcessor(BlockEntityRendererProvider.Context context) {
        model = new ModelProcessor(context.bakeLayer(ReactorModelLayers.PROCESSOR));
    }

    @Override
    protected Identifier getSubmitTexture(BlockEntity be) {
        return TEXTURE;
    }

    private static float facingAngle(Direction d) {
        return switch (d) {
            case WEST -> 270.0F;
            case EAST -> 90.0F;
            case SOUTH -> 180.0F;
            default -> 0.0F; // NORTH + vertical fallback
        };
    }

    @Override
    protected void renderModel(PoseStack stack, BlockEntity be, VertexConsumer vc, int light) {
        stack.pushPose();
        stack.translate(0.0, 2.0, 1.0);
        stack.scale(1.0F, -1.0F, -1.0F);
        stack.translate(0.5, 0.5, 0.5);
        stack.translate(0.0, 0.01, 0.0);
        Level level = be.getLevel();
        if (level != null) {
            Direction facing = be.getBlockState().getValue(BlockReactorMachine.FACING);
            stack.rotate(Axis.YP.rotationDegrees(facingAngle(facing)));
        }
        model.renderAll(stack, vc, light);
        stack.popPose();
    }
}
