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
 * 26.2 port of the Techne ModelMiniTurbine (mini/centrifugal turbine, {@code miniturbine3.png}, 64x64).
 * Unlike {@link ModelTurbine}, this is a static strut cage, not a spinning blade wheel: the legacy
 * blade rendering (Shape1/Shape1a/Shape2*) was entirely commented out upstream (dead code), so only
 * the fixed struts + end caps are ported. Two strut rings at fixed angles (front ring of 9 at Z=-3.5,
 * tex 16,21; rear ring of 8 at Z=7.5, tex 24,21) plus two hub caps (tex 0,48 / 20,48).
 */
public class ModelMiniTurbine {

    // Front ring: 9 struts (legacy Shape3a/b/c/d/e/f/g/h/i), fixed degrees about Z.
    private static final float[] FRONT_ANGLES = {-120F, -20F, -140F, 80F, -80F, 120F, 140F, 20F, 0F};
    // Rear ring: 8 struts (legacy Shape4a-h), fixed degrees about Z.
    private static final float[] REAR_ANGLES = {145F, 167.5F, 55F, 100F, 10F, 32.5F, 77.5F, 122.5F};

    private final ModelPart frontStrut;
    private final ModelPart rearStrut;
    private final ModelPart capFront;
    private final ModelPart capRear;

    public ModelMiniTurbine(ModelPart root) {
        this.frontStrut = root.getChild("front_strut");
        this.rearStrut = root.getChild("rear_strut");
        this.capFront = root.getChild("cap_front");
        this.capRear = root.getChild("cap_rear");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("front_strut",
                CubeListBuilder.create().mirror(true).texOffs(16, 21).addBox(-0.5F, -12F, -3F, 1F, 24F, 2F),
                PartPose.offset(0F, 14F, -3.5F));
        root.addOrReplaceChild("rear_strut",
                CubeListBuilder.create().mirror(true).texOffs(24, 21).addBox(-0.5F, -12F, -3F, 1F, 24F, 2F),
                PartPose.offset(0F, 14F, 7.5F));
        root.addOrReplaceChild("cap_front",
                CubeListBuilder.create().mirror(true).texOffs(0, 48).addBox(-3F, -3F, 0F, 6F, 6F, 3F),
                PartPose.offset(0F, 14F, -7F));
        root.addOrReplaceChild("cap_rear",
                CubeListBuilder.create().mirror(true).texOffs(20, 48).addBox(-3F, -3F, 0F, 6F, 6F, 3F),
                PartPose.offset(0F, 14F, 4F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    public void renderAll(PoseStack stack, VertexConsumer vc, int light) {
        for (float a : FRONT_ANGLES) {
            stack.pushPose();
            stack.rotate(Axis.ZP.rotationDegrees(a));
            frontStrut.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
            stack.popPose();
        }
        for (float a : REAR_ANGLES) {
            stack.pushPose();
            stack.rotate(Axis.ZP.rotationDegrees(a));
            rearStrut.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
            stack.popPose();
        }
        capFront.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        capRear.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
    }
}
