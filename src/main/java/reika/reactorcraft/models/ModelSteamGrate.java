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

/** 26.2 port of the Techne model ModelSteamGrate (auto-converted by scripts/convert_reactor_model.py). */
public class ModelSteamGrate {

    private final ModelPart shape1;
    private final ModelPart shape2;
    private final ModelPart shape2a;
    private final ModelPart shape3;
    private final ModelPart shape3a;
    private final ModelPart shape4;
    private final ModelPart shape7;
    private final ModelPart shape7a;
    private final ModelPart shape7b;
    private final ModelPart shape7c;
    private final ModelPart shape7d;
    private final ModelPart shape7e;
    private final ModelPart shape7f;
    private final ModelPart shape7g;
    private final ModelPart shape7h;
    private final ModelPart shape6;
    private final ModelPart shape6a;
    private final ModelPart shape6b;

    public ModelSteamGrate(ModelPart root) {
        this.shape1 = root.getChild("shape1");
        this.shape2 = root.getChild("shape2");
        this.shape2a = root.getChild("shape2a");
        this.shape3 = root.getChild("shape3");
        this.shape3a = root.getChild("shape3a");
        this.shape4 = root.getChild("shape4");
        this.shape7 = root.getChild("shape7");
        this.shape7a = root.getChild("shape7a");
        this.shape7b = root.getChild("shape7b");
        this.shape7c = root.getChild("shape7c");
        this.shape7d = root.getChild("shape7d");
        this.shape7e = root.getChild("shape7e");
        this.shape7f = root.getChild("shape7f");
        this.shape7g = root.getChild("shape7g");
        this.shape7h = root.getChild("shape7h");
        this.shape6 = root.getChild("shape6");
        this.shape6a = root.getChild("shape6a");
        this.shape6b = root.getChild("shape6b");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("shape1",
                CubeListBuilder.create().mirror(true).texOffs(63, 72)
                        .addBox(0F, 0F, 0F, 16F, 1F, 16F),
                PartPose.offsetAndRotation(-8F, 23F, -8F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape2",
                CubeListBuilder.create().mirror(true).texOffs(33, 37)
                        .addBox(0F, 0F, 0F, 16F, 15F, 1F),
                PartPose.offsetAndRotation(-8F, 8F, 7F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape2a",
                CubeListBuilder.create().mirror(true).texOffs(33, 55)
                        .addBox(0F, 0F, 0F, 16F, 15F, 1F),
                PartPose.offsetAndRotation(-8F, 8F, -8F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape3",
                CubeListBuilder.create().mirror(true).texOffs(33, 89)
                        .addBox(0F, 0F, 0F, 1F, 15F, 14F),
                PartPose.offsetAndRotation(-8F, 8F, -7F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape3a",
                CubeListBuilder.create().mirror(true).texOffs(0, 89)
                        .addBox(0F, 0F, 0F, 1F, 15F, 14F),
                PartPose.offsetAndRotation(7F, 8F, -7F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4",
                CubeListBuilder.create().mirror(true).texOffs(64, 0)
                        .addBox(0F, 0F, 0F, 14F, 1F, 14F),
                PartPose.offsetAndRotation(-7F, 22F, -7F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape7",
                CubeListBuilder.create().mirror(true).texOffs(0, 65)
                        .addBox(0F, 0F, 0F, 1F, 1F, 14F),
                PartPose.offsetAndRotation(4F, 8.2F, -7F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape7a",
                CubeListBuilder.create().mirror(true).texOffs(0, 65)
                        .addBox(0F, 0F, 0F, 1F, 1F, 14F),
                PartPose.offsetAndRotation(-3.5F, 8.2F, -7F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape7b",
                CubeListBuilder.create().mirror(true).texOffs(0, 65)
                        .addBox(0F, 0F, 0F, 1F, 1F, 14F),
                PartPose.offsetAndRotation(1F, 8.2F, -7F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape7c",
                CubeListBuilder.create().mirror(true).texOffs(0, 65)
                        .addBox(0F, 0F, 0F, 1F, 1F, 14F),
                PartPose.offsetAndRotation(-5F, 8.2F, -7F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape7d",
                CubeListBuilder.create().mirror(true).texOffs(0, 65)
                        .addBox(0F, 0F, 0F, 1F, 1F, 14F),
                PartPose.offsetAndRotation(-6.5F, 8.2F, -7F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape7e",
                CubeListBuilder.create().mirror(true).texOffs(0, 65)
                        .addBox(0F, 0F, 0F, 1F, 1F, 14F),
                PartPose.offsetAndRotation(5.5F, 8.2F, -7F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape7f",
                CubeListBuilder.create().mirror(true).texOffs(0, 65)
                        .addBox(0F, 0F, 0F, 1F, 1F, 14F),
                PartPose.offsetAndRotation(2.5F, 8.2F, -7F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape7g",
                CubeListBuilder.create().mirror(true).texOffs(0, 65)
                        .addBox(0F, 0F, 0F, 1F, 1F, 14F),
                PartPose.offsetAndRotation(-2F, 8.2F, -7F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape7h",
                CubeListBuilder.create().mirror(true).texOffs(0, 65)
                        .addBox(0F, 0F, 0F, 1F, 1F, 14F),
                PartPose.offsetAndRotation(-0.5F, 8.2F, -7F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape6",
                CubeListBuilder.create().mirror(true).texOffs(0, 80)
                        .addBox(0F, 0F, 0F, 14F, 1F, 1F),
                PartPose.offsetAndRotation(-7F, 8.3F, -4F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape6a",
                CubeListBuilder.create().mirror(true).texOffs(0, 80)
                        .addBox(0F, 0F, 0F, 14F, 1F, 1F),
                PartPose.offsetAndRotation(-7F, 8.3F, -0.5F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape6b",
                CubeListBuilder.create().mirror(true).texOffs(0, 80)
                        .addBox(0F, 0F, 0F, 14F, 1F, 1F),
                PartPose.offsetAndRotation(-7F, 8.3F, 3F, 0F, 0F, 0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    public void renderAll(PoseStack stack, VertexConsumer vc, int light) {
        shape1.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape7.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape7a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape7b.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape7c.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape7d.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape7e.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape7f.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape7g.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape7h.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape6.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape6a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape6b.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
    }
}
