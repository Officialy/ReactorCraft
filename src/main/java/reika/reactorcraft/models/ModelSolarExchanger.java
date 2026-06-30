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

/** 26.2 port of the Techne model ModelSolarExchanger (auto-converted by scripts/convert_reactor_model.py). */
public class ModelSolarExchanger {

    private final ModelPart shape2;
    private final ModelPart shape2a;
    private final ModelPart shape1;
    private final ModelPart shape1a;
    private final ModelPart shape1b;
    private final ModelPart shape1c;
    private final ModelPart shape1d;
    private final ModelPart shape1e;
    private final ModelPart shape1f;
    private final ModelPart shape1g;
    private final ModelPart shape3;
    private final ModelPart shape3a;
    private final ModelPart shape3b;
    private final ModelPart shape3c;
    private final ModelPart shape4;
    private final ModelPart shape5;
    private final ModelPart shape5a;
    private final ModelPart shape5b;
    private final ModelPart shape5c;
    private final ModelPart shape5d;
    private final ModelPart shape5e;
    private final ModelPart shape5f;
    private final ModelPart shape5g;

    public ModelSolarExchanger(ModelPart root) {
        this.shape2 = root.getChild("shape2");
        this.shape2a = root.getChild("shape2a");
        this.shape1 = root.getChild("shape1");
        this.shape1a = root.getChild("shape1a");
        this.shape1b = root.getChild("shape1b");
        this.shape1c = root.getChild("shape1c");
        this.shape1d = root.getChild("shape1d");
        this.shape1e = root.getChild("shape1e");
        this.shape1f = root.getChild("shape1f");
        this.shape1g = root.getChild("shape1g");
        this.shape3 = root.getChild("shape3");
        this.shape3a = root.getChild("shape3a");
        this.shape3b = root.getChild("shape3b");
        this.shape3c = root.getChild("shape3c");
        this.shape4 = root.getChild("shape4");
        this.shape5 = root.getChild("shape5");
        this.shape5a = root.getChild("shape5a");
        this.shape5b = root.getChild("shape5b");
        this.shape5c = root.getChild("shape5c");
        this.shape5d = root.getChild("shape5d");
        this.shape5e = root.getChild("shape5e");
        this.shape5f = root.getChild("shape5f");
        this.shape5g = root.getChild("shape5g");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("shape2",
                CubeListBuilder.create().mirror(true).texOffs(0, 55)
                        .addBox(0F, 0F, 0F, 16F, 1F, 16F),
                PartPose.offsetAndRotation(-8F, 23F, -8F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape2a",
                CubeListBuilder.create().mirror(true).texOffs(0, 55)
                        .addBox(0F, 0F, 0F, 16F, 1F, 16F),
                PartPose.offsetAndRotation(-8F, 8F, -8F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape1",
                CubeListBuilder.create().mirror(true).texOffs(67, 98)
                        .addBox(-0.5F, -6F, 0F, 1F, 6F, 14F),
                PartPose.offsetAndRotation(4F, 23.5F, -7F, 0F, 0F, 0.5235988F));
        root.addOrReplaceChild("shape1a",
                CubeListBuilder.create().mirror(true).texOffs(67, 76)
                        .addBox(-0.5F, 0F, 0F, 1F, 6F, 14F),
                PartPose.offsetAndRotation(4F, 8.5F, -7F, 0F, 0F, -0.5235988F));
        root.addOrReplaceChild("shape1b",
                CubeListBuilder.create().mirror(true).texOffs(67, 0)
                        .addBox(0F, 0F, -0.5F, 14F, 6F, 1F),
                PartPose.offsetAndRotation(-7F, 8.5F, 4F, 0.5235988F, 0F, 0F));
        root.addOrReplaceChild("shape1c",
                CubeListBuilder.create().mirror(true).texOffs(67, 8)
                        .addBox(0F, -6F, -0.5F, 14F, 6F, 1F),
                PartPose.offsetAndRotation(-7F, 23.5F, 4F, -0.5235988F, 0F, 0F));
        root.addOrReplaceChild("shape1d",
                CubeListBuilder.create().mirror(true).texOffs(67, 32)
                        .addBox(-0.5F, 0F, 0F, 1F, 6F, 14F),
                PartPose.offsetAndRotation(-4F, 8.5F, -7F, 0F, 0F, 0.5235988F));
        root.addOrReplaceChild("shape1e",
                CubeListBuilder.create().mirror(true).texOffs(67, 16)
                        .addBox(0F, 0F, -0.5F, 14F, 6F, 1F),
                PartPose.offsetAndRotation(-7F, 8.5F, -4F, -0.5235988F, 0F, 0F));
        root.addOrReplaceChild("shape1f",
                CubeListBuilder.create().mirror(true).texOffs(67, 54)
                        .addBox(-0.5F, -6F, 0F, 1F, 6F, 14F),
                PartPose.offsetAndRotation(-4F, 23.5F, -7F, 0F, 0F, -0.5235988F));
        root.addOrReplaceChild("shape1g",
                CubeListBuilder.create().mirror(true).texOffs(67, 24)
                        .addBox(0F, -6F, -0.5F, 14F, 6F, 1F),
                PartPose.offsetAndRotation(-7F, 23.5F, -4F, 0.5235988F, 0F, 0F));
        root.addOrReplaceChild("shape3",
                CubeListBuilder.create().mirror(true).texOffs(20, 30)
                        .addBox(0F, 0F, 0F, 2F, 14F, 2F),
                PartPose.offsetAndRotation(5.5F, 9F, 5.5F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape3a",
                CubeListBuilder.create().mirror(true).texOffs(30, 30)
                        .addBox(0F, 0F, 0F, 2F, 14F, 2F),
                PartPose.offsetAndRotation(-7.5F, 9F, 5.5F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape3b",
                CubeListBuilder.create().mirror(true).texOffs(10, 30)
                        .addBox(0F, 0F, 0F, 2F, 14F, 2F),
                PartPose.offsetAndRotation(-7.5F, 9F, -7.5F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape3c",
                CubeListBuilder.create().mirror(true).texOffs(0, 30)
                        .addBox(0F, 0F, 0F, 2F, 14F, 2F),
                PartPose.offsetAndRotation(5.5F, 9F, -7.5F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(0F, 0F, 0F, 14F, 5F, 14F),
                PartPose.offsetAndRotation(-7F, 13.5F, -7F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape5",
                CubeListBuilder.create().mirror(true).texOffs(0, 21)
                        .addBox(0F, 0F, 0F, 1F, 5F, 1F),
                PartPose.offsetAndRotation(6.2F, 13.5F, -3F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape5a",
                CubeListBuilder.create().mirror(true).texOffs(0, 21)
                        .addBox(0F, 0F, 0F, 1F, 5F, 1F),
                PartPose.offsetAndRotation(-3F, 13.5F, 6.2F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape5b",
                CubeListBuilder.create().mirror(true).texOffs(0, 21)
                        .addBox(0F, 0F, 0F, 1F, 5F, 1F),
                PartPose.offsetAndRotation(2F, 13.5F, -7.2F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape5c",
                CubeListBuilder.create().mirror(true).texOffs(0, 21)
                        .addBox(0F, 0F, 0F, 1F, 5F, 1F),
                PartPose.offsetAndRotation(2F, 13.5F, 6.2F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape5d",
                CubeListBuilder.create().mirror(true).texOffs(0, 21)
                        .addBox(0F, 0F, 0F, 1F, 5F, 1F),
                PartPose.offsetAndRotation(-3F, 13.5F, -7.2F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape5e",
                CubeListBuilder.create().mirror(true).texOffs(0, 21)
                        .addBox(0F, 0F, 0F, 1F, 5F, 1F),
                PartPose.offsetAndRotation(-7.2F, 13.5F, -3F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape5f",
                CubeListBuilder.create().mirror(true).texOffs(0, 21)
                        .addBox(0F, 0F, 0F, 1F, 5F, 1F),
                PartPose.offsetAndRotation(-7.2F, 13.5F, 2F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape5g",
                CubeListBuilder.create().mirror(true).texOffs(0, 21)
                        .addBox(0F, 0F, 0F, 1F, 5F, 1F),
                PartPose.offsetAndRotation(6.2F, 13.5F, 2F, 0F, 0F, 0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    public void renderAll(PoseStack stack, VertexConsumer vc, int light) {
        shape2.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape1.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape1a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape1b.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape1c.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape1d.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape1e.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape1f.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape1g.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3b.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3c.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape5.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape5a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape5b.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape5c.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape5d.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape5e.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape5f.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape5g.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
    }
}
