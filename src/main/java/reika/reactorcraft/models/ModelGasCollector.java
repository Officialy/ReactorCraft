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

/** 26.2 port of the Techne model ModelGasCollector (auto-converted by scripts/convert_reactor_model.py). */
public class ModelGasCollector {

    private final ModelPart shape1;
    private final ModelPart shape2;
    private final ModelPart shape2a;
    private final ModelPart shape3;
    private final ModelPart shape3a;
    private final ModelPart shape4;
    private final ModelPart shape4a;
    private final ModelPart shape4b;
    private final ModelPart shape4c;
    private final ModelPart shape4d;

    public ModelGasCollector(ModelPart root) {
        this.shape1 = root.getChild("shape1");
        this.shape2 = root.getChild("shape2");
        this.shape2a = root.getChild("shape2a");
        this.shape3 = root.getChild("shape3");
        this.shape3a = root.getChild("shape3a");
        this.shape4 = root.getChild("shape4");
        this.shape4a = root.getChild("shape4a");
        this.shape4b = root.getChild("shape4b");
        this.shape4c = root.getChild("shape4c");
        this.shape4d = root.getChild("shape4d");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("shape1",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(0F, 0F, 0F, 16F, 1F, 16F),
                PartPose.offsetAndRotation(-8F, 23F, -8F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape2",
                CubeListBuilder.create().mirror(true).texOffs(0, 30)
                        .addBox(0F, 0F, 0F, 16F, 8F, 1F),
                PartPose.offsetAndRotation(-8F, 23F, 8F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape2a",
                CubeListBuilder.create().mirror(true).texOffs(0, 19)
                        .addBox(0F, 0F, 0F, 16F, 8F, 1F),
                PartPose.offsetAndRotation(-8F, 23F, -9F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape3",
                CubeListBuilder.create().mirror(true).texOffs(0, 69)
                        .addBox(0F, 0F, 0F, 1F, 8F, 18F),
                PartPose.offsetAndRotation(-9F, 23F, -9F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape3a",
                CubeListBuilder.create().mirror(true).texOffs(0, 41)
                        .addBox(0F, 0F, 0F, 1F, 8F, 18F),
                PartPose.offsetAndRotation(8F, 23F, -9F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4",
                CubeListBuilder.create().mirror(true).texOffs(64, 67)
                        .addBox(0F, 0F, 0F, 8F, 5F, 8F),
                PartPose.offsetAndRotation(-4F, 8F, -4F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4a",
                CubeListBuilder.create().mirror(true).texOffs(64, 0)
                        .addBox(0F, 0F, 0F, 16F, 3F, 16F),
                PartPose.offsetAndRotation(-8F, 20F, -8F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4b",
                CubeListBuilder.create().mirror(true).texOffs(64, 20)
                        .addBox(0F, 0F, 0F, 14F, 3F, 14F),
                PartPose.offsetAndRotation(-7F, 17F, -7F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4c",
                CubeListBuilder.create().mirror(true).texOffs(64, 38)
                        .addBox(0F, 0F, 0F, 12F, 2F, 12F),
                PartPose.offsetAndRotation(-6F, 15F, -6F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4d",
                CubeListBuilder.create().mirror(true).texOffs(64, 54)
                        .addBox(0F, 0F, 0F, 10F, 2F, 10F),
                PartPose.offsetAndRotation(-5F, 13F, -5F, 0F, 0F, 0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    /**
     * @param hasFurnace legacy {@code li.get(0)}: the four funnel walls (shape2/2a/3/3a) only draw when
     *                   the collector is actually mounted against a furnace/refrigerator to collect from.
     */
    public void renderAll(PoseStack stack, VertexConsumer vc, int light, boolean hasFurnace) {
        shape1.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        if (hasFurnace) {
            shape2.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
            shape2a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
            shape3.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
            shape3a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        }
        shape4.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4b.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4c.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4d.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
    }
}
