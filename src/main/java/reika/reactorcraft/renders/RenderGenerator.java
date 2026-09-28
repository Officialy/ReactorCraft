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
import reika.reactorcraft.models.ModelGenerator;
import reika.reactorcraft.registry.ReactorModelLayers;
import reika.reactorcraft.tileentities.TileEntityReactorGenerator;

/**
 * TODO: the unformed state should draw the flat generator_multi cap quads like the legacy renderer
 * (shared gap with RenderTurbine's unformed cap).
 */
public class RenderGenerator extends ReactorTERenderer<TileEntityReactorGenerator> {

    // One texture per generator mode (generator<mode.ordinal()>.png).
    private static final Identifier[] TEXTURES = new Identifier[3];
    static {
        for (int i = 0; i < TEXTURES.length; i++)
            TEXTURES[i] = Identifier.fromNamespaceAndPath(ReactorCraft.MODID, "textures/tileentity/generator" + i + ".png");
    }

    private final ModelGenerator model;

    public RenderGenerator(BlockEntityRendererProvider.Context context) {
        model = new ModelGenerator(context.bakeLayer(ReactorModelLayers.GENERATOR));
    }

    @Override
    protected Identifier getSubmitTexture(BlockEntity be) {
        int mode = ((TileEntityReactorGenerator) be).getMode().ordinal();
        return TEXTURES[Math.min(mode, TEXTURES.length - 1)];
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
        TileEntityReactorGenerator gen = (TileEntityReactorGenerator) be;
        if (gen.getLevel() != null && !gen.hasMultiBlock())
            return;
        stack.pushPose();
        stack.translate(0.0, 2.0, 1.0);
        stack.scale(1.0F, -1.0F, -1.0F);
        stack.translate(0.5, 0.5, 0.5);
        stack.rotate(Axis.YP.rotationDegrees(facingAngle(gen.getFacing())));
        model.renderAll(stack, vc, light, spinAngle(gen));
        stack.popPose();
    }

    /** Armature spin from the synced omega + game clock (the client BE never ticks). */
    private static float spinAngle(TileEntityReactorGenerator gen) {
        int omega = gen.getOmega();
        if (omega <= 0)
            return 0F;
        double degPerTick = 0.2 * Math.pow(Math.log(omega + 1) / Math.log(2), 1.05);
        Minecraft mc = Minecraft.getInstance();
        double t = (mc.level != null ? mc.level.getGameTime() : 0L)
                + mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        return (float) ((t * degPerTick) % 360.0);
    }
}
