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
import reika.reactorcraft.models.ModelSolarExchanger;
import reika.reactorcraft.registry.ReactorModelLayers;
import reika.reactorcraft.tileentities.powergen.TileEntitySolarExchanger;

/**
 * 26.2 port of the static solar-exchanger BER. The legacy renderer was pure {@code GL11} with no
 * animation ({@code renderAll(tile, null)}); here it inherits {@link ReactorTERenderer#submit} and
 * only supplies the texture + the baked model draw. The base transform reproduces the legacy
 * {@code translate(0,2,1) · scale(1,-1,-1) · translate(0.5,0.5,0.5)} (net top-centre origin (0.5,1.5,0.5)).
 * The legacy shaft-IO connector overlay ({@code IORenderer.renderIO}) is not ported — ReactorCraft
 * tile entities don't yet implement RotaryCraft's {@code BlockEntityIOMachine} interface.
 */
public class RenderSolarExchanger extends ReactorTERenderer<TileEntitySolarExchanger> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(ReactorCraft.MODID, "textures/tileentity/solar.png");

    private final ModelSolarExchanger model;

    public RenderSolarExchanger(BlockEntityRendererProvider.Context context) {
        model = new ModelSolarExchanger(context.bakeLayer(ReactorModelLayers.SOLAR_EXCHANGER));
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
        model.renderAll(stack, vc, light);
        stack.popPose();
    }
}
