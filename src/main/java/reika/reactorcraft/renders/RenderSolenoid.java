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

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;

import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.base.ReactorTERenderer;
import reika.reactorcraft.models.ModelSolenoid;
import reika.reactorcraft.registry.ReactorModelLayers;
import reika.reactorcraft.tileentities.fusion.TileEntitySolenoidMagnet;

/**
 * 26.2 port of the solenoid-magnet (central fusion coil) BER. Only renders when the multiblock coil
 * should show ({@link TileEntitySolenoidMagnet#canRenderCoil()}). Texture {@code solenoid.png}.
 *
 * Deferred: the coil spin (legacy {@code renderAll(tile, list, -phi, 0)}) — needs the client phi
 * ticker; drawn static for now.
 */
public class RenderSolenoid extends ReactorTERenderer<TileEntitySolenoidMagnet> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(ReactorCraft.MODID, "textures/tileentity/solenoid.png");

    private final ModelSolenoid model;

    public RenderSolenoid(BlockEntityRendererProvider.Context context) {
        model = new ModelSolenoid(context.bakeLayer(ReactorModelLayers.SOLENOID));
    }

    @Override
    protected Identifier getSubmitTexture(BlockEntity be) {
        return ((TileEntitySolenoidMagnet) be).canRenderCoil() ? TEXTURE : null;
    }

    @Override
    protected void renderModel(PoseStack stack, BlockEntity be, VertexConsumer vc, int light) {
        if (!((TileEntitySolenoidMagnet) be).canRenderCoil())
            return;
        stack.pushPose();
        stack.translate(0.0, 2.0, 1.0);
        stack.scale(1.0F, -1.0F, -1.0F);
        stack.translate(0.5, 0.5, 0.5);
        model.renderAll(stack, vc, light);
        stack.popPose();
    }
}
