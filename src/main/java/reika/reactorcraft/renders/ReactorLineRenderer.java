/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.renders;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.joml.Matrix4f;

import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.base.ReactorTERenderer;
import reika.reactorcraft.base.TileEntityLine;
import reika.reactorcraft.tileentities.TileEntityHeatPipe;

/**
 * 26.2 BER for the ReactorCraft lines (steam line, heat pipe). Port of the legacy {@code RenderWaterLine}:
 * a connected square pipe drawn from the BE's {@code connections[]} cache, textured with the standalone
 * {@code tileentity/waterline.png}. Reuses the connected-tube geometry from {@link ReactorPipeRenderer}
 * (the same legacy {@code renderLiquid} quads), but as an opaque solid pass via the
 * {@link ReactorTERenderer} base ({@code getSubmitTexture} + {@code renderModel}) since the line texture
 * is a dedicated PNG rather than a block-atlas sprite. The heat pipe gets the legacy warm tint.
 */
public class ReactorLineRenderer extends ReactorTERenderer<TileEntityLine> {

    // The line getTexture() in the TEs points at block/steam_line + block/heat_pipe sprites that were
    // never authored; waterline.png is the only real line art (used for both, heat pipe tinted warm).
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(ReactorCraft.MODID, "textures/tileentity/waterline.png");

    // Lines are drawn thinner than ducts — legacy SteamLineRenderer.renderFace used a fixed
    // size=1/3 cross-section (not the fluid-pipe shell's 0.75, and NOT 0.375 either — that was
    // this port's own earlier guess). bounds = 0.5 +/- size/2.
    private static final double SIZE = 1.0 / 3.0;
    private static final double IN = 0.5 + SIZE / 2.0;   // 0.6667
    private static final double IN2 = 0.5 - SIZE / 2.0;  // 0.3333
    // Arm-extension texture offset along the flow axis for a full-texture (0..1) sprite.
    private static final float DU = (float) ((IN - IN2) / 4D);

    public ReactorLineRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected Identifier getSubmitTexture(BlockEntity be) {
        return TEXTURE;
    }

    @Override
    protected void renderModel(PoseStack stack, BlockEntity be, VertexConsumer vc, int light) {
        TileEntityLine te = (TileEntityLine) be;
        // Steam line draws at half brightness (the legacy 0.5 grey); the heat pipe takes its
        // live temperature colour from the TE.
        int tint = 0xFF808080;
        if (te instanceof TileEntityHeatPipe pipe)
            tint = 0xFF000000 | (pipe.getRenderColor() & 0xFFFFFF);
        int overlay = OverlayTexture.NO_OVERLAY;
        Matrix4f m = stack.last().pose();
        for (Direction dir : Direction.values()) {
            if (te.isConnectionValidForSide(dir))
                ReactorPipeRenderer.emitConnectedFluid(m, vc, dir, 0F, 0F, 1F, 1F, DU, IN, IN2, tint, light, overlay);
            else
                ReactorPipeRenderer.emitCap(m, vc, dir, 0F, 0F, 1F, 1F, IN, IN2, tint, light, overlay);
        }
    }
}
