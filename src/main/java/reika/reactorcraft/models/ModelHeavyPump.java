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

public class ModelHeavyPump {

    private final ModelPart shape1;
    private final ModelPart shape2;
    private final ModelPart shape2a;
    private final ModelPart shape2b;
    private final ModelPart shape2c;
    private final ModelPart shape1a;
    private final ModelPart shape3;
    private final ModelPart shape3a;
    private final ModelPart shape3b;
    private final ModelPart shape3c;
    private final ModelPart shape3d;
    private final ModelPart shape3e;
    private final ModelPart shape4;
    private final ModelPart shape4a;
    private final ModelPart shape4b;
    private final ModelPart shape4c;

    public ModelHeavyPump(ModelPart root) {
        this.shape1 = root.getChild("shape1");
        this.shape2 = root.getChild("shape2");
        this.shape2a = root.getChild("shape2a");
        this.shape2b = root.getChild("shape2b");
        this.shape2c = root.getChild("shape2c");
        this.shape1a = root.getChild("shape1a");
        this.shape3 = root.getChild("shape3");
        this.shape3a = root.getChild("shape3a");
        this.shape3b = root.getChild("shape3b");
        this.shape3c = root.getChild("shape3c");
        this.shape3d = root.getChild("shape3d");
        this.shape3e = root.getChild("shape3e");
        this.shape4 = root.getChild("shape4");
        this.shape4a = root.getChild("shape4a");
        this.shape4b = root.getChild("shape4b");
        this.shape4c = root.getChild("shape4c");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("shape1",
                CubeListBuilder.create().mirror(true).texOffs(0, 19)
                        .addBox(0F, 0F, 0F, 16F, 1F, 16F),
                PartPose.offsetAndRotation(-8F, 8F, -8F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape2",
                CubeListBuilder.create().mirror(true).texOffs(10, 37)
                        .addBox(0F, 0F, 0F, 1F, 14F, 1F),
                PartPose.offsetAndRotation(7F, 9F, 7F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape2a",
                CubeListBuilder.create().mirror(true).texOffs(0, 37)
                        .addBox(0F, 0F, 0F, 1F, 14F, 1F),
                PartPose.offsetAndRotation(-8F, 9F, 7F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape2b",
                CubeListBuilder.create().mirror(true).texOffs(5, 37)
                        .addBox(0F, 0F, 0F, 1F, 14F, 1F),
                PartPose.offsetAndRotation(-8F, 9F, -8F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape2c",
                CubeListBuilder.create().mirror(true).texOffs(15, 37)
                        .addBox(0F, 0F, 0F, 1F, 14F, 1F),
                PartPose.offsetAndRotation(7F, 9F, -8F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape1a",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(0F, 0F, 0F, 16F, 1F, 16F),
                PartPose.offsetAndRotation(-8F, 23F, -8F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape3",
                CubeListBuilder.create().mirror(true).texOffs(0, 53)
                        .addBox(-5F, 0F, -5F, 10F, 13F, 10F),
                PartPose.offsetAndRotation(0F, 9.5F, 0F, 0F, 0.3926991F, 0F));
        root.addOrReplaceChild("shape3a",
                CubeListBuilder.create().mirror(true).texOffs(0, 77)
                        .addBox(-1F, 0F, -1F, 2F, 17F, 2F),
                PartPose.offsetAndRotation(0F, 7.5F, 0F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape3b",
                CubeListBuilder.create().mirror(true).texOffs(0, 77)
                        .addBox(-1F, 0F, -1F, 2F, 17F, 2F),
                PartPose.offsetAndRotation(0F, 7.5F, 0F, 0F, 0.7853982F, 0F));
        root.addOrReplaceChild("shape3c",
                CubeListBuilder.create().mirror(true).texOffs(0, 53)
                        .addBox(-5F, 0F, -5F, 10F, 13F, 10F),
                PartPose.offsetAndRotation(0F, 9.5F, 0F, 0F, 0.7853982F, 0F));
        root.addOrReplaceChild("shape3d",
                CubeListBuilder.create().mirror(true).texOffs(0, 53)
                        .addBox(-5F, 0F, -5F, 10F, 13F, 10F),
                PartPose.offsetAndRotation(0F, 9.5F, 0F, 0F, 1.178097F, 0F));
        root.addOrReplaceChild("shape3e",
                CubeListBuilder.create().mirror(true).texOffs(0, 53)
                        .addBox(-5F, 0F, -5F, 10F, 13F, 10F),
                PartPose.offsetAndRotation(0F, 9.5F, 0F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4",
                CubeListBuilder.create().mirror(true).texOffs(66, 31)
                        .addBox(0F, 0F, 0F, 14F, 14F, 1F),
                PartPose.offsetAndRotation(-7F, 9F, 7F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4a",
                CubeListBuilder.create().mirror(true).texOffs(97, 0)
                        .addBox(0F, 0F, 0F, 1F, 14F, 14F),
                PartPose.offsetAndRotation(7F, 9F, -7F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4b",
                CubeListBuilder.create().mirror(true).texOffs(97, 31)
                        .addBox(0F, 0F, 0F, 14F, 14F, 1F),
                PartPose.offsetAndRotation(-7F, 9F, -8F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4c",
                CubeListBuilder.create().mirror(true).texOffs(66, 0)
                        .addBox(0F, 0F, 0F, 1F, 14F, 14F),
                PartPose.offsetAndRotation(-8F, 9F, -7F, 0F, 0F, 0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    /** phi (degrees) spins only the inner impeller assembly (shape3*), matching the legacy sandwich rotate. */
    public void renderAll(PoseStack stack, VertexConsumer vc, int light, float phi) {
        shape1.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2b.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2c.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape1a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);

        stack.pushPose();
        stack.rotate(Axis.YP.rotationDegrees(phi));
        shape3.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3b.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3c.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3d.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3e.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        stack.popPose();

        shape4.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4b.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4c.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
    }
}
