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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.base.ReactorTERenderer;
import reika.reactorcraft.models.ModelFlywheel;
import reika.reactorcraft.registry.ReactorModelLayers;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.tileentities.TileEntityReactorFlywheel;
import reika.reactorcraft.tileentities.powergen.TileEntityTurbineCore;

/**
 * 26.2 port of the flywheel BER. Legacy only rendered the spinning wheel when
 * {@code hasMultiBlock()} (the multiblock housing formed); the unformed state drew a flat
 * placeholder quad using the multiblock-shell block's own texture, which needs a BER quad helper
 * this port doesn't have yet -- rendering nothing unformed is the TODO in its place.
 */
public class RenderTurbineWheel extends ReactorTERenderer<TileEntityReactorFlywheel> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(ReactorCraft.MODID, "textures/tileentity/flywheel.png");

    private final ModelFlywheel model;

    public RenderTurbineWheel(BlockEntityRendererProvider.Context context) {
        model = new ModelFlywheel(context.bakeLayer(ReactorModelLayers.FLYWHEEL));
    }

    @Override
    protected Identifier getSubmitTexture(BlockEntity be) {
        TileEntityReactorFlywheel tile = (TileEntityReactorFlywheel) be;
        return tile.hasMultiBlock() ? TEXTURE : null;
    }

    // Legacy meta table (0=EAST,1=WEST,2=SOUTH,3=NORTH -> 270/90/0/180) matches RenderTurbine's
    // Direction->angle mapping exactly.
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
        TileEntityReactorFlywheel tile = (TileEntityReactorFlywheel) be;
        stack.pushPose();
        stack.translate(0.0, 2.0, 1.0);
        stack.scale(1.0F, -1.0F, -1.0F);
        stack.translate(0.5, 0.5, 0.5);
        stack.rotate(Axis.YP.rotationDegrees(facingAngle(tile.getFacing())));
        model.renderAll(stack, vc, light, spinAngle(tile));
        stack.popPose();
    }

    /**
     * The flywheel's own {@code omega}/sync tag doesn't carry the turbine's speed to the client fast
     * enough to look right (legacy {@code animateWithTick} read {@code turbine.phi*6} directly); derive
     * the spin from the adjacent turbine core's synced {@code getRenderOmega()} instead, exactly like
     * {@code RenderTurbine.spinAngle}, scaled by the legacy 6x factor.
     */
    private static float spinAngle(TileEntityReactorFlywheel te) {
        Level level = te.getLevel();
        if (level == null)
            return 0F;
        BlockPos tp = te.getBlockPos().relative(te.getFacing());
        ReactorTiles r = ReactorTiles.getTE(level, tp);
        if (r == null || !r.isTurbine())
            return 0F;
        TileEntityTurbineCore turbine = (TileEntityTurbineCore) level.getBlockEntity(tp);
        int omega = turbine.getRenderOmega();
        if (omega <= 0)
            return 0F;
        double degPerTick = 6.0 * 0.2 * Math.pow(Math.log(omega + 1) / Math.log(2), 1.05);
        Minecraft mc = Minecraft.getInstance();
        double t = (mc.level != null ? mc.level.getGameTime() : 0L)
                + mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        return (float) ((t * degPerTick) % 360.0);
    }
}
