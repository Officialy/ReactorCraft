/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2017
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.auxiliary;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import reika.dragonapi.auxiliary.trackers.PlayerSpecificRenderer;
import reika.dragonapi.interfaces.PlayerRenderObj;
import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.models.ModelMagnet;

/** Original toroid head render, including the detached BER's internal 0.4 scale and 0.6 offset. */
@EventBusSubscriber(modid = ReactorCraft.MODID, value = Dist.CLIENT)
public final class DonatorToroidRender implements PlayerRenderObj {
    public static final DonatorToroidRender instance = new DonatorToroidRender();
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(ReactorCraft.MODID, "textures/tileentity/magnet.png");
    private final ModelMagnet model = new ModelMagnet(ModelMagnet.createLayer().bakeRoot());
    private DonatorToroidRender() {}

    @SubscribeEvent public static void load(FMLLoadCompleteEvent event) {
        event.enqueueWork(() -> PlayerSpecificRenderer.instance.registerDonatorRenderer(instance));
    }

    @Override public void extractRenderState(PoseStack stack, Player player, float partialTick,
            PlayerSpecificRenderer.PlayerRotationData rotation, SubmitNodeCollector collector) {
        stack.pushPose();
        try {
            double d = 0.1875;
            stack.translate(-d, 2.0625, -d);
            stack.translate(-d, d, 0);
            stack.translate(d, 0, d);
            stack.translate(d, d, 0);
            stack.rotate(Axis.ZP.rotationDegrees(90));
            stack.rotate(Axis.XP.rotationDegrees((System.currentTimeMillis() / 10L) % 360));
            stack.translate(-d, -d, 0);
            stack.translate(-d, 0, -d);
            stack.scale(0.375F, 0.375F, 0.375F);
            // Original RenderMagnet.renderTileEntityMagnetAt for an unplaced Aim.N magnet.
            stack.translate(0, 2, 1);
            stack.scale(1, -1, -1);
            stack.translate(0.5, 0.5, 0.5);
            stack.translate(0, 0.6, 0);
            stack.scale(0.4F, 0.4F, 0.4F);
            stack.rotate(Axis.YP.rotationDegrees(90));
            PoseStack snapshot = new PoseStack();
            snapshot.last().set(stack.last());
            collector.submitCustomGeometry(stack, RenderTypes.entitySolid(TEXTURE),
                    (pose, vertices) -> model.renderAll(snapshot, vertices, LightCoordsUtil.FULL_BRIGHT));
        } finally { stack.popPose(); }
    }
    @Override public int getRenderPriority() { return 0; }
}
