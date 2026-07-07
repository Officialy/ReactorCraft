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

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

import reika.dragonapi.libraries.mathsci.ReikaPhysicsHelper;
import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.base.ReactorTERenderer;
import reika.reactorcraft.models.ModelSolarTop;
import reika.reactorcraft.registry.ReactorModelLayers;
import reika.reactorcraft.tileentities.TileEntitySolarTop;

/**
 * 26.2 port of the solar-tower top BER (legacy RenderSolarTop). Texture {@code solartop.png}. When
 * stacked directly on another solar-top block, the legacy renderer flips the model 180 degrees about
 * X then 90 about Y so the two halves mate; ported as the same {@code flip} check against the block
 * below. The coil group is tinted by the tower's current blackbody temperature color.
 *
 * TODO: the heat-shimmer flare quad (additive-blended billboard, render pass 1, shown once temperature
 * exceeds 400) is deferred — it needs the translucent billboard pipeline, not the opaque machine one.
 */
public class RenderSolarTop extends ReactorTERenderer<TileEntitySolarTop> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(ReactorCraft.MODID, "textures/tileentity/solartop.png");

    private final ModelSolarTop model;

    public RenderSolarTop(BlockEntityRendererProvider.Context context) {
        model = new ModelSolarTop(context.bakeLayer(ReactorModelLayers.SOLAR_TOP));
    }

    @Override
    protected Identifier getSubmitTexture(BlockEntity be) {
        return TEXTURE;
    }

    @Override
    protected void renderModel(PoseStack stack, BlockEntity be, VertexConsumer vc, int light) {
        TileEntitySolarTop tile = (TileEntitySolarTop) be;
        Level level = tile.getLevel();
        boolean flip = level != null && level.getBlockEntity(tile.getBlockPos().below()) instanceof TileEntitySolarTop;

        stack.pushPose();
        stack.translate(0.0, flip ? 0.0 : 2.0, 1.0);
        stack.scale(1.0F, -1.0F, -1.0F);
        stack.translate(0.5, 0.5, 0.5);
        if (flip) {
            stack.mulPose(Axis.XP.rotationDegrees(180.0F));
            stack.mulPose(Axis.YP.rotationDegrees(90.0F));
        }

        int c = ReikaPhysicsHelper.getColorForTemperature(200 + tile.getTemperature() * 2);
        int tint = 0xFF000000 | (c & 0xFFFFFF);
        model.renderAll(stack, vc, light, tint);
        stack.popPose();
    }
}
