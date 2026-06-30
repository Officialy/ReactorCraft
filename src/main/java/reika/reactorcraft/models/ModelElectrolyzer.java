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

/** 26.2 port of the Techne model ModelElectrolyzer (auto-converted by scripts/convert_reactor_model.py). */
public class ModelElectrolyzer {

    private final ModelPart shape1;
    private final ModelPart shape2;
    private final ModelPart shape2a;
    private final ModelPart shape3;
    private final ModelPart shape3a;
    private final ModelPart shape4;
    private final ModelPart shape5;
    private final ModelPart shape5a;
    private final ModelPart shape6;
    private final ModelPart shape6a;
    private final ModelPart shape7;
    private final ModelPart shape7a;
    private final ModelPart shape8;
    private final ModelPart shape8a;

    public ModelElectrolyzer(ModelPart root) {
        this.shape1 = root.getChild("shape1");
        this.shape2 = root.getChild("shape2");
        this.shape2a = root.getChild("shape2a");
        this.shape3 = root.getChild("shape3");
        this.shape3a = root.getChild("shape3a");
        this.shape4 = root.getChild("shape4");
        this.shape5 = root.getChild("shape5");
        this.shape5a = root.getChild("shape5a");
        this.shape6 = root.getChild("shape6");
        this.shape6a = root.getChild("shape6a");
        this.shape7 = root.getChild("shape7");
        this.shape7a = root.getChild("shape7a");
        this.shape8 = root.getChild("shape8");
        this.shape8a = root.getChild("shape8a");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("shape1",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(0F, 0F, 0F, 12F, 13F, 12F),
                PartPose.offsetAndRotation(-6F, 11F, -6F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape2",
                CubeListBuilder.create().mirror(true).texOffs(0, 25)
                        .addBox(0F, 0F, 0F, 14F, 3F, 1F),
                PartPose.offsetAndRotation(-7F, 11F, -7F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape2a",
                CubeListBuilder.create().mirror(true).texOffs(0, 25)
                        .addBox(0F, 0F, 0F, 14F, 3F, 1F),
                PartPose.offsetAndRotation(-7F, 11F, 6F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape3",
                CubeListBuilder.create().mirror(true).texOffs(50, 0)
                        .addBox(0F, 0F, 0F, 1F, 3F, 12F),
                PartPose.offsetAndRotation(6F, 11F, -6F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape3a",
                CubeListBuilder.create().mirror(true).texOffs(50, 0)
                        .addBox(0F, 0F, 0F, 1F, 3F, 12F),
                PartPose.offsetAndRotation(-7F, 11F, -6F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4",
                CubeListBuilder.create().mirror(true).texOffs(78, 0)
                        .addBox(0F, 0F, 0F, 4F, 3F, 4F),
                PartPose.offsetAndRotation(-2F, 8F, -2F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape5",
                CubeListBuilder.create().mirror(true).texOffs(0, 30)
                        .addBox(0F, 0F, 0F, 4F, 4F, 2F),
                PartPose.offsetAndRotation(-2F, 15F, -8F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape5a",
                CubeListBuilder.create().mirror(true).texOffs(0, 30)
                        .addBox(0F, 0F, 0F, 4F, 4F, 2F),
                PartPose.offsetAndRotation(-2F, 15F, 6F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape6",
                CubeListBuilder.create().mirror(true).texOffs(0, 37)
                        .addBox(0F, 0F, 0F, 2F, 4F, 4F),
                PartPose.offsetAndRotation(-8F, 15F, -2F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape6a",
                CubeListBuilder.create().mirror(true).texOffs(0, 37)
                        .addBox(0F, 0F, 0F, 2F, 4F, 4F),
                PartPose.offsetAndRotation(6F, 15F, -2F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape7",
                CubeListBuilder.create().mirror(true).texOffs(0, 52)
                        .addBox(0F, 0F, 0F, 8F, 2F, 1F),
                PartPose.offsetAndRotation(-4F, 9F, -4F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape7a",
                CubeListBuilder.create().mirror(true).texOffs(0, 47)
                        .addBox(0F, 0F, 0F, 8F, 2F, 1F),
                PartPose.offsetAndRotation(-4F, 9F, 3F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape8",
                CubeListBuilder.create().mirror(true).texOffs(29, 30)
                        .addBox(0F, 0F, 0F, 1F, 2F, 6F),
                PartPose.offsetAndRotation(3F, 9F, -3F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape8a",
                CubeListBuilder.create().mirror(true).texOffs(13, 30)
                        .addBox(0F, 0F, 0F, 1F, 2F, 6F),
                PartPose.offsetAndRotation(-4F, 9F, -3F, 0F, 0F, 0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    public void renderAll(PoseStack stack, VertexConsumer vc, int light) {
        shape1.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape5.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape5a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape6.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape6a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape7.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape7a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape8.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape8a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
    }
}
