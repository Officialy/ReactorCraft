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

public class ModelCentrifuge {

    private final ModelPart shape2;
    private final ModelPart shape2a;
    private final ModelPart shape2b;
    private final ModelPart shape2c;
    private final ModelPart shape2d;
    private final ModelPart shape2e;
    private final ModelPart shape2f;
    private final ModelPart shape2g;
    private final ModelPart shape1;
    private final ModelPart shape3;
    private final ModelPart shape3a;
    private final ModelPart shape3b;
    private final ModelPart shape3c;

    public ModelCentrifuge(ModelPart root) {
        this.shape2 = root.getChild("shape2");
        this.shape2a = root.getChild("shape2a");
        this.shape2b = root.getChild("shape2b");
        this.shape2c = root.getChild("shape2c");
        this.shape2d = root.getChild("shape2d");
        this.shape2e = root.getChild("shape2e");
        this.shape2f = root.getChild("shape2f");
        this.shape2g = root.getChild("shape2g");
        this.shape1 = root.getChild("shape1");
        this.shape3 = root.getChild("shape3");
        this.shape3a = root.getChild("shape3a");
        this.shape3b = root.getChild("shape3b");
        this.shape3c = root.getChild("shape3c");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("shape2",
                CubeListBuilder.create().mirror(true).texOffs(0, 18)
                        .addBox(6F, 0F, -3F, 1F, 14F, 6F),
                PartPose.offsetAndRotation(0F, 9F, 0F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape2a",
                CubeListBuilder.create().mirror(true).texOffs(0, 18)
                        .addBox(-3F, 0F, 6F, 6F, 14F, 1F),
                PartPose.offsetAndRotation(0F, 9F, 0F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape2b",
                CubeListBuilder.create().mirror(true).texOffs(0, 18)
                        .addBox(-7F, 0F, -3F, 1F, 14F, 6F),
                PartPose.offsetAndRotation(0F, 9F, 0F, 0F, -0.7853982F, 0F));
        root.addOrReplaceChild("shape2c",
                CubeListBuilder.create().mirror(true).texOffs(0, 18)
                        .addBox(-3F, 0F, -7F, 6F, 14F, 1F),
                PartPose.offsetAndRotation(0F, 9F, 0F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape2d",
                CubeListBuilder.create().mirror(true).texOffs(0, 18)
                        .addBox(-7F, 0F, -3F, 1F, 14F, 6F),
                PartPose.offsetAndRotation(0F, 9F, 0F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape2e",
                CubeListBuilder.create().mirror(true).texOffs(0, 18)
                        .addBox(-7F, 0F, -3F, 1F, 14F, 6F),
                PartPose.offsetAndRotation(0F, 9F, 0F, 0F, 0.7853982F, 0F));
        root.addOrReplaceChild("shape2f",
                CubeListBuilder.create().mirror(true).texOffs(0, 18)
                        .addBox(-7F, 0F, -3F, 1F, 14F, 6F),
                PartPose.offsetAndRotation(0F, 9F, 0F, 0F, 2.356194F, 0F));
        root.addOrReplaceChild("shape2g",
                CubeListBuilder.create().mirror(true).texOffs(0, 18)
                        .addBox(-7F, 0F, -3F, 1F, 14F, 6F),
                PartPose.offsetAndRotation(0F, 9F, 0F, 0F, -2.356194F, 0F));
        root.addOrReplaceChild("shape1",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(0F, 0F, 0F, 16F, 1F, 16F),
                PartPose.offsetAndRotation(-8F, 23F, -8F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape3",
                CubeListBuilder.create().mirror(true).texOffs(0, 61)
                        .addBox(-6F, 0F, -3F, 12F, 1F, 6F),
                PartPose.offsetAndRotation(0F, 8.5F, 0F, 0F, 0.7853982F, 0F));
        root.addOrReplaceChild("shape3a",
                CubeListBuilder.create().mirror(true).texOffs(0, 39)
                        .addBox(-3F, 0F, -6F, 6F, 1F, 12F),
                PartPose.offsetAndRotation(0F, 8.4F, 0F, 0F, 0.7679449F, 0F));
        root.addOrReplaceChild("shape3b",
                CubeListBuilder.create().mirror(true).texOffs(0, 53)
                        .addBox(-6F, 0F, -3F, 12F, 1F, 6F),
                PartPose.offsetAndRotation(0F, 8.1F, 0F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape3c",
                CubeListBuilder.create().mirror(true).texOffs(0, 69)
                        .addBox(-3F, 0F, -6F, 6F, 1F, 12F),
                PartPose.offsetAndRotation(0F, 8F, 0F, 0F, 0F, 0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    /** phi (degrees) spins the rotor assembly (everything but the base plate, shape1). */
    public void renderAll(PoseStack stack, VertexConsumer vc, int light, float phi) {
        shape1.render(stack, vc, light, OverlayTexture.NO_OVERLAY);

        stack.pushPose();
        stack.rotate(Axis.YP.rotationDegrees(phi));
        shape2.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2b.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2c.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2d.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2e.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2f.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2g.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3b.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3c.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        stack.popPose();
    }
}
