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

public class ModelProcessor {

    private final ModelPart shape2a;
    private final ModelPart shape2b;
    private final ModelPart shape3;
    private final ModelPart shape3a;
    private final ModelPart shape3b;
    private final ModelPart shape3c;
    private final ModelPart shape2;
    private final ModelPart shape2c;
    private final ModelPart shape2d;
    private final ModelPart shape1;
    private final ModelPart shape1a;
    private final ModelPart shape1b;
    private final ModelPart shape1c;

    public ModelProcessor(ModelPart root) {
        this.shape2a = root.getChild("shape2a");
        this.shape2b = root.getChild("shape2b");
        this.shape3 = root.getChild("shape3");
        this.shape3a = root.getChild("shape3a");
        this.shape3b = root.getChild("shape3b");
        this.shape3c = root.getChild("shape3c");
        this.shape2 = root.getChild("shape2");
        this.shape2c = root.getChild("shape2c");
        this.shape2d = root.getChild("shape2d");
        this.shape1 = root.getChild("shape1");
        this.shape1a = root.getChild("shape1a");
        this.shape1b = root.getChild("shape1b");
        this.shape1c = root.getChild("shape1c");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("shape2a",
                CubeListBuilder.create().mirror(true).texOffs(36, 47)
                        .addBox(0F, 0F, 0F, 1F, 14F, 7F),
                PartPose.offsetAndRotation(-0.5F, 9F, -7F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape2b",
                CubeListBuilder.create().mirror(true).texOffs(31, 18)
                        .addBox(0F, 0F, 0F, 1F, 14F, 7F),
                PartPose.offsetAndRotation(-8F, 9F, -7F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape3",
                CubeListBuilder.create().mirror(true).texOffs(0, 47)
                        .addBox(0F, 0F, 0F, 16F, 14F, 1F),
                PartPose.offsetAndRotation(-8F, 9F, -8F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape3a",
                CubeListBuilder.create().mirror(true).texOffs(0, 95)
                        .addBox(0F, 0F, 0F, 14F, 11F, 1F),
                PartPose.offsetAndRotation(-7F, 12F, 1F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape3b",
                CubeListBuilder.create().mirror(true).texOffs(0, 63)
                        .addBox(0F, 0F, 0F, 14F, 11F, 1F),
                PartPose.offsetAndRotation(-7F, 12F, 7F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape3c",
                CubeListBuilder.create().mirror(true).texOffs(0, 79)
                        .addBox(0F, 0F, 0F, 16F, 14F, 1F),
                PartPose.offsetAndRotation(-8F, 9F, 0F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape2",
                CubeListBuilder.create().mirror(true).texOffs(50, 19)
                        .addBox(0F, 0F, 0F, 1F, 11F, 5F),
                PartPose.offsetAndRotation(-7F, 12F, 2F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape2c",
                CubeListBuilder.create().mirror(true).texOffs(0, 18)
                        .addBox(0F, 0F, 0F, 1F, 14F, 7F),
                PartPose.offsetAndRotation(7F, 9F, -7F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape2d",
                CubeListBuilder.create().mirror(true).texOffs(18, 19)
                        .addBox(0F, 0F, 0F, 1F, 11F, 5F),
                PartPose.offsetAndRotation(6F, 12F, 2F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape1",
                CubeListBuilder.create().mirror(true).texOffs(54, 10)
                        .addBox(0F, 0F, 0F, 14F, 1F, 7F),
                PartPose.offsetAndRotation(-7F, 11F, 1F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape1a",
                CubeListBuilder.create().mirror(true).texOffs(0, 109)
                        .addBox(0F, 0F, 0F, 16F, 1F, 9F),
                PartPose.offsetAndRotation(-8F, 8F, -8F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape1b",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(0F, 0F, 0F, 16F, 1F, 9F),
                PartPose.offsetAndRotation(-8F, 23F, -8F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape1c",
                CubeListBuilder.create().mirror(true).texOffs(54, 0)
                        .addBox(0F, 0F, 0F, 14F, 1F, 7F),
                PartPose.offsetAndRotation(-7F, 23F, 1F, 0F, 0F, 0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    public void renderAll(PoseStack stack, VertexConsumer vc, int light) {
        shape2a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2b.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3b.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3c.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2c.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2d.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape1.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape1a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape1b.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape1c.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
    }
}
