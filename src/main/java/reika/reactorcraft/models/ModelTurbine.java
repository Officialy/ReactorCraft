/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 ******************************************************************************/
package reika.reactorcraft.models;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.texture.OverlayTexture;

/**
 * 26.2 port of the Techne ModelTurbine — the steam-turbine wheel. Unlike the box-dump models this is
 * PROCEDURAL: one {@code blade} part is drawn once per {@code getAngularSeparation()} degrees around the
 * shaft (Z axis, pivot at y=vo), twisted about Y, for two stacked rings. Hand-authored because the
 * auto-converter can't express the per-blade loop. Baked for the base turbine (stage 0): blade length
 * 16, width 2, 45 blades (360/8), twist 10°, back ring scaled 1.3×. (Higher tiers / the spin animation
 * are deferred — see task notes.)
 */
public class ModelTurbine {

    private static final double VO = 0.9375;   // pivot height (legacy)
    private static final double DD = 0.25;     // ring Z-separation
    private static final double SC = 1.3;      // stage-0 back-ring scale
    private static final int SEP = 8;          // stage-0 angular separation (deg) -> 45 blades
    private static final float TWIST = 10F;    // stage-0 blade twist (deg)

    private final ModelPart shaft1;
    private final ModelPart shaft1a;
    private final ModelPart blade;

    public ModelTurbine(ModelPart root) {
        this.shaft1 = root.getChild("shaft1");
        this.shaft1a = root.getChild("shaft1a");
        this.blade = root.getChild("blade");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("shaft1",
                CubeListBuilder.create().mirror(true).texOffs(0, 106).addBox(-2F, -2F, 0F, 4F, 4F, 16F),
                PartPose.offsetAndRotation(0F, 15F, -8F, 0F, 0F, 0.7853982F));
        root.addOrReplaceChild("shaft1a",
                CubeListBuilder.create().mirror(true).texOffs(0, 106).addBox(-2F, -2F, 0F, 4F, 4F, 16F),
                PartPose.offsetAndRotation(0F, 15F, -8F, 0F, 0F, 0F));
        // stage-0 blade: length 16, width 2 -> addBox(-0.5, -16, -1, 1, 16, 2)
        root.addOrReplaceChild("blade",
                CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, -16F, -1F, 1F, 16F, 2F),
                PartPose.offset(0F, 15F, 0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    public void renderAll(PoseStack stack, VertexConsumer vc, int light) {
        this.renderAll(stack, vc, light, 0F);
    }

    /** {@code phi} is the current spin angle (deg) of the blade wheel about the shaft (Z) axis. */
    public void renderAll(PoseStack stack, VertexConsumer vc, int light, float phi) {
        shaft1.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shaft1a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);

        // front ring
        stack.pushPose();
        stack.translate(0, 0, DD);
        renderBlades(stack, vc, light, phi);
        stack.popPose();

        // back ring (offset the other way, scaled up about the pivot)
        stack.pushPose();
        stack.translate(0, 0, -DD);
        stack.translate(0, VO, 0);
        stack.scale((float) SC, (float) SC, 1F);
        stack.translate(0, -VO, 0);
        renderBlades(stack, vc, light, phi);
        stack.popPose();
    }

    private void renderBlades(PoseStack stack, VertexConsumer vc, int light, float phi) {
        for (int i = 0; i < 360; i += SEP) {
            stack.pushPose();
            stack.translate(0, VO, 0);
            stack.mulPose(Axis.ZP.rotationDegrees(i + phi));
            stack.translate(0, -VO, 0);
            stack.mulPose(Axis.YP.rotationDegrees(-TWIST));
            blade.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
            stack.popPose();
        }
    }
}
