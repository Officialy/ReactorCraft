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

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class ModelExchanger {

    private final ModelPart shape1;
    private final ModelPart shape2b3;
    private final ModelPart shape1a;
    private final ModelPart shape2c2;
    private final ModelPart shape1b;
    private final ModelPart p3;
    private final ModelPart shape1c;
    private final ModelPart shape7;
    private final ModelPart shape2b;
    private final ModelPart shape2b2;
    private final ModelPart p4;
    private final ModelPart p2;
    private final ModelPart p1;
    private final ModelPart shape2c;
    private final ModelPart shape2;

    public ModelExchanger(ModelPart root) {
        this.shape1 = root.getChild("shape1");
        this.shape2b3 = root.getChild("shape2b3");
        this.shape1a = root.getChild("shape1a");
        this.shape2c2 = root.getChild("shape2c2");
        this.shape1b = root.getChild("shape1b");
        this.p3 = root.getChild("p3");
        this.shape1c = root.getChild("shape1c");
        this.shape7 = root.getChild("shape7");
        this.shape2b = root.getChild("shape2b");
        this.shape2b2 = root.getChild("shape2b2");
        this.p4 = root.getChild("p4");
        this.p2 = root.getChild("p2");
        this.p1 = root.getChild("p1");
        this.shape2c = root.getChild("shape2c");
        this.shape2 = root.getChild("shape2");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("shape1",
                CubeListBuilder.create().mirror(true).texOffs(0, 37)
                        .addBox(0F, 0F, 0F, 16F, 1F, 16F),
                PartPose.offsetAndRotation(-8F, 11F, -8F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape2b3",
                CubeListBuilder.create().mirror(true).texOffs(105, 0)
                        .addBox(0F, 0F, 0F, 3F, 14F, 3F),
                PartPose.offsetAndRotation(-6F, 9F, 3F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape1a",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(0F, 0F, 0F, 16F, 1F, 16F),
                PartPose.offsetAndRotation(-8F, 8F, -8F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape2c2",
                CubeListBuilder.create().mirror(true).texOffs(0, 79)
                        .addBox(0F, 0F, 0F, 18F, 2F, 18F),
                PartPose.offsetAndRotation(-9F, 15F, -9F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape1b",
                CubeListBuilder.create().mirror(true).texOffs(0, 37)
                        .addBox(0F, 0F, 0F, 16F, 1F, 16F),
                PartPose.offsetAndRotation(-8F, 20F, -8F, 0F, 0F, 0F));
        root.addOrReplaceChild("p3",
                CubeListBuilder.create().mirror(true).texOffs(86, 32)
                        .addBox(0F, 0F, 0F, 5F, 8F, 5F),
                PartPose.offsetAndRotation(2F, 12F, 2F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape1c",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(0F, 0F, 0F, 16F, 1F, 16F),
                PartPose.offsetAndRotation(-8F, 23F, -8F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape7",
                CubeListBuilder.create().mirror(true).texOffs(66, 46)
                        .addBox(0F, 0F, 0F, 4F, 14F, 4F),
                PartPose.offsetAndRotation(-2F, 9F, -2F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape2b",
                CubeListBuilder.create().mirror(true).texOffs(79, 0)
                        .addBox(0F, 0F, 0F, 3F, 14F, 3F),
                PartPose.offsetAndRotation(3F, 9F, -6F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape2b2",
                CubeListBuilder.create().mirror(true).texOffs(92, 0)
                        .addBox(0F, 0F, 0F, 3F, 14F, 3F),
                PartPose.offsetAndRotation(3F, 9F, 3F, 0F, 0F, 0F));
        root.addOrReplaceChild("p4",
                CubeListBuilder.create().mirror(true).texOffs(65, 32)
                        .addBox(0F, 0F, 0F, 5F, 8F, 5F),
                PartPose.offsetAndRotation(-7F, 12F, 2F, 0F, 0F, 0F));
        root.addOrReplaceChild("p2",
                CubeListBuilder.create().mirror(true).texOffs(86, 19)
                        .addBox(0F, 0F, 0F, 5F, 8F, 5F),
                PartPose.offsetAndRotation(2F, 12F, -7F, 0F, 0F, 0F));
        root.addOrReplaceChild("p1",
                CubeListBuilder.create().mirror(true).texOffs(65, 19)
                        .addBox(0F, 0F, 0F, 5F, 8F, 5F),
                PartPose.offsetAndRotation(-7F, 12F, -7F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape2c",
                CubeListBuilder.create().mirror(true).texOffs(0, 57)
                        .addBox(0F, 0F, 0F, 16F, 4F, 16F),
                PartPose.offsetAndRotation(-8F, 14F, -8F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape2",
                CubeListBuilder.create().mirror(true).texOffs(66, 0)
                        .addBox(0F, 0F, 0F, 3F, 14F, 3F),
                PartPose.offsetAndRotation(-6F, 9F, -6F, 0F, 0F, 0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    public void renderAll(PoseStack stack, VertexConsumer vc, int light) {
        shape1.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2b3.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape1a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2c2.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape1b.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        p3.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape1c.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape7.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2b.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2b2.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        p4.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        p2.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        p1.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2c.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
    }
}
