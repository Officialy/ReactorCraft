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
 * 26.2 port of the Techne ModelFlywheel. Legacy {@code renderAll} only ever drew the shaft
 * (shape1/shape1a) and three scaled copies of the "center" hub ring (shape2*) -- the shape3 / shape4 groups
 * groups were built but their render calls were commented out (dead geometry), so they are not
 * transcribed.
 */
public class ModelFlywheel {

    private final ModelPart shape1;
    private final ModelPart shape1a;
    private final ModelPart shape2;
    private final ModelPart shape2a;
    private final ModelPart shape2b;
    private final ModelPart shape2c;
    private final ModelPart shape2d;
    private final ModelPart shape2e;
    private final ModelPart shape2f;
    private final ModelPart shape2g;
    private final ModelPart shape2h;
    private final ModelPart shape2i;
    private final ModelPart shape2j;
    private final ModelPart shape2k;
    private final ModelPart shape2l;
    private final ModelPart shape2m;
    private final ModelPart shape2n;
    private final ModelPart shape2o;
    private final ModelPart shape2p;

    public ModelFlywheel(ModelPart root) {
        shape1 = root.getChild("shape1");
        shape1a = root.getChild("shape1a");
        shape2 = root.getChild("shape2");
        shape2a = root.getChild("shape2a");
        shape2b = root.getChild("shape2b");
        shape2c = root.getChild("shape2c");
        shape2d = root.getChild("shape2d");
        shape2e = root.getChild("shape2e");
        shape2f = root.getChild("shape2f");
        shape2g = root.getChild("shape2g");
        shape2h = root.getChild("shape2h");
        shape2i = root.getChild("shape2i");
        shape2j = root.getChild("shape2j");
        shape2k = root.getChild("shape2k");
        shape2l = root.getChild("shape2l");
        shape2m = root.getChild("shape2m");
        shape2n = root.getChild("shape2n");
        shape2o = root.getChild("shape2o");
        shape2p = root.getChild("shape2p");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("shape1",
                CubeListBuilder.create().mirror(true).texOffs(0, 0).addBox(-2F, -2F, -8.5F, 4F, 4F, 17F),
                PartPose.offsetAndRotation(0F, 15F, 0F, 0F, 0F, 0.7853982F));
        root.addOrReplaceChild("shape1a",
                CubeListBuilder.create().mirror(true).texOffs(0, 0).addBox(-2F, -2F, -8.5F, 4F, 4F, 17F),
                PartPose.offset(0F, 15F, 0F));
        root.addOrReplaceChild("shape2",
                CubeListBuilder.create().mirror(true).texOffs(96, 49).addBox(-10F, 16F, -7F, 20F, 4F, 14F),
                PartPose.offset(0F, 15F, 0F));
        root.addOrReplaceChild("shape2a",
                CubeListBuilder.create().mirror(true).texOffs(166, 22).addBox(16F, -10F, -7F, 4F, 20F, 14F),
                PartPose.offset(0F, 15F, 0F));
        root.addOrReplaceChild("shape2b",
                CubeListBuilder.create().mirror(true).texOffs(96, 22).addBox(10F, 16F, -7F, 4F, 2F, 14F),
                PartPose.offset(0F, 15F, 0F));
        root.addOrReplaceChild("shape2c",
                CubeListBuilder.create().mirror(true).texOffs(166, 22).addBox(-20F, -10F, -7F, 4F, 20F, 14F),
                PartPose.offset(0F, 15F, 0F));
        root.addOrReplaceChild("shape2d",
                CubeListBuilder.create().mirror(true).texOffs(96, 22).addBox(-6F, 20F, -7F, 12F, 2F, 14F),
                PartPose.offset(0F, 15F, 0F));
        root.addOrReplaceChild("shape2e",
                CubeListBuilder.create().mirror(true).texOffs(96, 22).addBox(-14F, 16F, -7F, 4F, 2F, 14F),
                PartPose.offset(0F, 15F, 0F));
        root.addOrReplaceChild("shape2f",
                CubeListBuilder.create().mirror(true).texOffs(96, 22).addBox(16F, -14F, -7F, 2F, 4F, 14F),
                PartPose.offset(0F, 15F, 0F));
        root.addOrReplaceChild("shape2g",
                CubeListBuilder.create().mirror(true).texOffs(96, 22).addBox(10F, -18F, -7F, 4F, 2F, 14F),
                PartPose.offset(0F, 15F, 0F));
        root.addOrReplaceChild("shape2h",
                CubeListBuilder.create().mirror(true).texOffs(96, 22).addBox(-14F, -18F, -7F, 4F, 2F, 14F),
                PartPose.offset(0F, 15F, 0F));
        root.addOrReplaceChild("shape2i",
                CubeListBuilder.create().mirror(true).texOffs(96, 22).addBox(16F, 10F, -7F, 2F, 4F, 14F),
                PartPose.offset(0F, 15F, 0F));
        root.addOrReplaceChild("shape2j",
                CubeListBuilder.create().mirror(true).texOffs(96, 22).addBox(-18F, -14F, -7F, 2F, 4F, 14F),
                PartPose.offset(0F, 15F, 0F));
        root.addOrReplaceChild("shape2k",
                CubeListBuilder.create().mirror(true).texOffs(96, 22).addBox(-18F, 10F, -7F, 2F, 4F, 14F),
                PartPose.offset(0F, 15F, 0F));
        root.addOrReplaceChild("shape2l",
                CubeListBuilder.create().mirror(true).texOffs(96, 49).addBox(-10F, -20F, -7F, 20F, 4F, 14F),
                PartPose.offset(0F, 15F, 0F));
        root.addOrReplaceChild("shape2m",
                CubeListBuilder.create().mirror(true).texOffs(96, 22).addBox(20F, -6F, -7F, 2F, 12F, 14F),
                PartPose.offset(0F, 15F, 0F));
        root.addOrReplaceChild("shape2n",
                CubeListBuilder.create().mirror(true).texOffs(96, 22).addBox(-6F, -22F, -7F, 12F, 2F, 14F),
                PartPose.offset(0F, 15F, 0F));
        root.addOrReplaceChild("shape2o",
                CubeListBuilder.create().mirror(true).texOffs(96, 22).addBox(-22F, -6F, -7F, 2F, 12F, 14F),
                PartPose.offset(0F, 15F, 0F));
        root.addOrReplaceChild("shape2p",
                CubeListBuilder.create().mirror(true).texOffs(0, 22).addBox(-16F, -16F, -7F, 32F, 32F, 14F),
                PartPose.offset(0F, 15F, 0F));
        return LayerDefinition.create(mesh, 256, 256);
    }

    /** phi (degrees) spins the shaft + hub ring about local Z, pivoted at y=0.9375 (legacy VO). */
    public void renderAll(PoseStack stack, VertexConsumer vc, int light, float phi) {
        stack.pushPose();
        double vo = 0.9375;
        stack.translate(0, vo, 0);
        stack.rotate(Axis.ZP.rotationDegrees(phi));
        stack.translate(0, -vo, 0);
        stack.translate(0, 0, -0.0625);
        stack.scale(1F, 1F, 1.25F);
        shape1.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape1a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);

        renderCenter(stack, vc, light, 0.25, 2.0, 0.35);
        renderCenter(stack, vc, light, -0.25, 2.0, 0.35);
        renderCenterNoZOffset(stack, vc, light, 1.75, 0.35);
        stack.popPose();
    }

    private void renderCenter(PoseStack stack, VertexConsumer vc, int light, double dz, double sc, double scz) {
        stack.pushPose();
        stack.translate(0, -0.9375, dz);
        stack.scale((float) sc, (float) sc, (float) scz);
        renderHub(stack, vc, light);
        stack.popPose();
    }

    private void renderCenterNoZOffset(PoseStack stack, VertexConsumer vc, int light, double sc, double scz) {
        stack.pushPose();
        stack.translate(0, -0.6875, 0);
        stack.scale((float) sc, (float) sc, (float) scz);
        renderHub(stack, vc, light);
        stack.popPose();
    }

    private void renderHub(PoseStack stack, VertexConsumer vc, int light) {
        shape2.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2b.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2c.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2d.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2e.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2f.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2g.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2h.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2i.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2j.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2k.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2l.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2m.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2n.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2o.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2p.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
    }
}
