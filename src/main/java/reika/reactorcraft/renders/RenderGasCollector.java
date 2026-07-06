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
import reika.reactorcraft.blocks.BlockReactorMachine;
import reika.reactorcraft.models.ModelGasCollector;
import reika.reactorcraft.registry.ReactorModelLayers;
import reika.reactorcraft.tileentities.TileEntityGasCollector;

/**
 * The legacy read-direction target overlay (debug AABB highlight on the adjacent furnace/refrigerator,
 * {@code renderTarget}/{@code ReikaAABBHelper.renderAABB}) is not ported -- pure debug visual, no
 * gameplay effect.
 */
public class RenderGasCollector extends ReactorTERenderer<TileEntityGasCollector> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(ReactorCraft.MODID, "textures/tileentity/co2collector.png");

    private final ModelGasCollector model;

    public RenderGasCollector(BlockEntityRendererProvider.Context context) {
        model = new ModelGasCollector(context.bakeLayer(ReactorModelLayers.GAS_COLLECTOR));
    }

    @Override
    protected Identifier getSubmitTexture(BlockEntity be) {
        return TEXTURE;
    }

    private static float facingAngle(Direction d) {
        return switch (d) {
            case DOWN, NORTH -> 180.0F;
            case EAST -> 90.0F;
            case WEST -> 270.0F;
            default -> 0.0F; // UP, SOUTH
        };
    }

    @Override
    protected void renderModel(PoseStack stack, BlockEntity be, VertexConsumer vc, int light) {
        TileEntityGasCollector te = (TileEntityGasCollector) be;
        stack.pushPose();
        stack.translate(0.0, 2.0, 1.0);
        stack.scale(1.0F, -1.0F, -1.0F);
        stack.translate(0.5, 0.5, 0.5);

        Direction facing = be.getBlockState().getValue(BlockReactorMachine.FACING);
        float angle = facingAngle(facing);
        if (facing.getAxis() == Direction.Axis.Y) {
            stack.mulPose(Axis.ZP.rotationDegrees(angle));
            if (facing == Direction.DOWN)
                stack.translate(0.0, -2.0, 0.0);
        }
        else {
            stack.mulPose(Axis.XP.rotationDegrees(90F));
            stack.mulPose(Axis.ZP.rotationDegrees(angle));
            stack.translate(0.0, -1.0, -1.0);
        }

        model.renderAll(stack, vc, light, te.hasFurnace());
        stack.popPose();
    }
}
