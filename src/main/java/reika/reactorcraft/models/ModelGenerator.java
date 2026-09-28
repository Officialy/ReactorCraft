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

/** The generator: a spinning armature (shape1/shape3 groups) inside a static octagonal housing. */
public class ModelGenerator {

    private final ModelPart shape1;
    private final ModelPart shape1b;
    private final ModelPart shape1c;
    private final ModelPart shape1d;
    private final ModelPart shape3;
    private final ModelPart shape3a;
    private final ModelPart shape3b;
    private final ModelPart shape3c;
    private final ModelPart shape2;
    private final ModelPart shape2a;
    private final ModelPart shape2b;
    private final ModelPart shape2c;
    private final ModelPart shape2d;
    private final ModelPart shape2e;
    private final ModelPart shape2f;
    private final ModelPart shape2g;
    private final ModelPart shape4;
    private final ModelPart shape5;
    private final ModelPart shape7;
    private final ModelPart shape7a;
    private final ModelPart shape7b;
    private final ModelPart shape7c;

    public ModelGenerator(ModelPart root) {
        shape1 = root.getChild("shape1");
        shape1b = root.getChild("shape1b");
        shape1c = root.getChild("shape1c");
        shape1d = root.getChild("shape1d");
        shape3 = root.getChild("shape3");
        shape3a = root.getChild("shape3a");
        shape3b = root.getChild("shape3b");
        shape3c = root.getChild("shape3c");
        shape2 = root.getChild("shape2");
        shape2a = root.getChild("shape2a");
        shape2b = root.getChild("shape2b");
        shape2c = root.getChild("shape2c");
        shape2d = root.getChild("shape2d");
        shape2e = root.getChild("shape2e");
        shape2f = root.getChild("shape2f");
        shape2g = root.getChild("shape2g");
        shape4 = root.getChild("shape4");
        shape5 = root.getChild("shape5");
        shape7 = root.getChild("shape7");
        shape7a = root.getChild("shape7a");
        shape7b = root.getChild("shape7b");
        shape7c = root.getChild("shape7c");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("shape1",
                CubeListBuilder.create().mirror(true).texOffs(0, 0).addBox(-8F, -8F, 0F, 16F, 16F, 127F),
                PartPose.rotation(0F, 0F, 1.178097F));
        root.addOrReplaceChild("shape2",
                CubeListBuilder.create().mirror(true).texOffs(0, 155).addBox(-17F, 32F, 0F, 34F, 8F, 128F),
                PartPose.rotation(0F, 0F, -0.7853982F));
        root.addOrReplaceChild("shape2a",
                CubeListBuilder.create().mirror(true).texOffs(0, 155).addBox(-17F, 32F, 0F, 34F, 8F, 128F),
                PartPose.rotation(0F, 0F, -2.356194F));
        root.addOrReplaceChild("shape2b",
                CubeListBuilder.create().mirror(true).texOffs(0, 155).addBox(-17F, 32F, 0F, 34F, 8F, 128F),
                PartPose.rotation(0F, 0F, 0.7853982F));
        root.addOrReplaceChild("shape2c",
                CubeListBuilder.create().mirror(true).texOffs(0, 155).addBox(-17F, 32F, 0F, 34F, 8F, 128F),
                PartPose.ZERO);
        root.addOrReplaceChild("shape2d",
                CubeListBuilder.create().mirror(true).texOffs(0, 155).addBox(-17F, 32F, 0F, 34F, 8F, 128F),
                PartPose.rotation(0F, 0F, 1.570796F));
        root.addOrReplaceChild("shape2e",
                CubeListBuilder.create().mirror(true).texOffs(0, 155).addBox(-17F, 32F, 0F, 34F, 8F, 128F),
                PartPose.rotation(0F, 0F, -1.570796F));
        root.addOrReplaceChild("shape2f",
                CubeListBuilder.create().mirror(true).texOffs(0, 155).addBox(-17F, 32F, 0F, 34F, 8F, 128F),
                PartPose.rotation(0F, 0F, 3.170681F));
        root.addOrReplaceChild("shape2g",
                CubeListBuilder.create().mirror(true).texOffs(0, 155).addBox(-17F, 32F, 0F, 34F, 8F, 128F),
                PartPose.rotation(0F, 0F, 2.373648F));
        root.addOrReplaceChild("shape1b",
                CubeListBuilder.create().mirror(true).texOffs(0, 0).addBox(-8F, -8F, 0F, 16F, 16F, 127F),
                PartPose.rotation(0F, 0F, 0.7853982F));
        root.addOrReplaceChild("shape1c",
                CubeListBuilder.create().mirror(true).texOffs(0, 0).addBox(-8F, -8F, 0F, 16F, 16F, 127F),
                PartPose.rotation(0F, 0F, 0.3926991F));
        root.addOrReplaceChild("shape3",
                CubeListBuilder.create().mirror(true).texOffs(0, 293).addBox(-20F, -20F, 0F, 40F, 40F, 116F),
                PartPose.offsetAndRotation(0F, 0F, 4F, 0F, 0F, 1.178097F));
        root.addOrReplaceChild("shape1d",
                CubeListBuilder.create().mirror(true).texOffs(0, 0).addBox(-8F, -8F, 0F, 16F, 16F, 127F),
                PartPose.ZERO);
        root.addOrReplaceChild("shape3a",
                CubeListBuilder.create().mirror(true).texOffs(0, 293).addBox(-20F, -20F, 0F, 40F, 40F, 116F),
                PartPose.offset(0F, 0F, 4F));
        root.addOrReplaceChild("shape3b",
                CubeListBuilder.create().mirror(true).texOffs(0, 293).addBox(-20F, -20F, 0F, 40F, 40F, 116F),
                PartPose.offsetAndRotation(0F, 0F, 4F, 0F, 0F, 0.7853982F));
        root.addOrReplaceChild("shape3c",
                CubeListBuilder.create().mirror(true).texOffs(0, 293).addBox(-20F, -20F, 0F, 40F, 40F, 116F),
                PartPose.offsetAndRotation(0F, 0F, 4F, 0F, 0F, 0.3926991F));
        root.addOrReplaceChild("shape4",
                CubeListBuilder.create().mirror(true).texOffs(0, 0).addBox(0F, 0F, 0F, 1F, 1F, 1F),
                PartPose.ZERO);
        root.addOrReplaceChild("shape5",
                CubeListBuilder.create().mirror(true).texOffs(288, 0).addBox(-40F, -40F, 0F, 80F, 80F, 32F),
                PartPose.offset(0F, 0F, -32F));
        root.addOrReplaceChild("shape7",
                CubeListBuilder.create().mirror(true).texOffs(0, 461).addBox(-36F, -4F, 0F, 72F, 8F, 1F),
                PartPose.offsetAndRotation(0F, 0F, 126.7F, 0F, 0F, -0.7853982F));
        root.addOrReplaceChild("shape7a",
                CubeListBuilder.create().mirror(true).texOffs(0, 461).addBox(-36F, -4F, 0F, 72F, 8F, 1F),
                PartPose.offset(0F, 0F, 126.8F));
        root.addOrReplaceChild("shape7b",
                CubeListBuilder.create().mirror(true).texOffs(0, 461).addBox(-36F, -4F, 0F, 72F, 8F, 1F),
                PartPose.offsetAndRotation(0F, 0F, 126.9F, 0F, 0F, 1.570796F));
        root.addOrReplaceChild("shape7c",
                CubeListBuilder.create().mirror(true).texOffs(0, 461).addBox(-36F, -4F, 0F, 72F, 8F, 1F),
                PartPose.offsetAndRotation(0F, 0F, 126.7F, 0F, 0F, 0.7853982F));
        return LayerDefinition.create(mesh, 512, 512);
    }

    /** phi (degrees) spins the armature (shape1 / shape3 groups) about local Z; the housing stays fixed. */
    public void renderAll(PoseStack stack, VertexConsumer vc, int light, float phi) {
        stack.pushPose();
        stack.translate(0, 1, -7.5);
        stack.rotate(Axis.ZP.rotationDegrees(phi));
        shape1.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape1b.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape1c.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape1d.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3b.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3c.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        stack.rotate(Axis.ZP.rotationDegrees(-phi));

        shape2.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2b.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2c.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2d.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2e.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2f.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2g.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape5.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape7.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape7a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape7b.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape7c.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        stack.popPose();
    }
}
