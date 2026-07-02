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
 * <p>The coil spin (legacy {@code GL11.glRotated(phi, 0, 1, 0)}, a single rigid Y-axis rotation of the
 * whole model) can't be driven by the BE's own {@code phi} field: the client BE never ticks
 * ({@code BlockReactorMachine.getTicker} returns null client-side), so {@code phi} only jumps once
 * per sync packet instead of advancing every frame -- the model would freeze between syncs rather than
 * spin. Matches {@code RenderTurbine}'s fix: derive a continuously-advancing angle client-side from the
 * synced {@code omega} (via {@link TileEntitySolenoidMagnet#getMaxRenderSpeed()}) and the game clock.</p>
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
        TileEntitySolenoidMagnet te = (TileEntitySolenoidMagnet) be;
        if (!te.canRenderCoil())
            return;
        stack.pushPose();
        stack.translate(0.0, 2.0, 1.0);
        stack.scale(1.0F, -1.0F, -1.0F);
        stack.translate(0.5, 0.5, 0.5);
        stack.mulPose(Axis.YP.rotationDegrees(spinAngle(te)));
        model.renderAll(stack, vc, light);
        stack.popPose();
    }

    /** Client-derived spin angle in degrees -- see class doc for why {@code te.phi} can't be used. */
    private static float spinAngle(TileEntitySolenoidMagnet te) {
        if (!te.canTurn())
            return 0F;
        float degPerTick = te.getMaxRenderSpeed();
        Minecraft mc = Minecraft.getInstance();
        double t = (mc.level != null ? mc.level.getGameTime() : 0L)
                + mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        return (float) ((t * degPerTick) % 360.0);
    }
}
