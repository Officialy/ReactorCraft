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

/** 26.2 port of the Techne model ModelCondenser (auto-converted by scripts/convert_reactor_model.py). */
public class ModelCondenser {

    private final ModelPart shape2;
    private final ModelPart shape2a;
    private final ModelPart shape2b;
    private final ModelPart shape2c;
    private final ModelPart shape3;
    private final ModelPart shape3a;
    private final ModelPart shape1;
    private final ModelPart shape4;
    private final ModelPart shape4a;
    private final ModelPart shape4b;
    private final ModelPart shape4c;
    private final ModelPart shape4d;
    private final ModelPart shape4e;
    private final ModelPart shape4f;
    private final ModelPart shape4g;
    private final ModelPart shape4h;
    private final ModelPart shape4i;
    private final ModelPart shape4j;
    private final ModelPart shape4k;
    private final ModelPart shape4l;
    private final ModelPart shape4m;
    private final ModelPart shape4n;
    private final ModelPart shape4o;
    private final ModelPart shape4p;
    private final ModelPart shape4q;
    private final ModelPart shape4r;
    private final ModelPart shape4s;
    private final ModelPart shape4t;
    private final ModelPart shape4u;
    private final ModelPart shape4w;
    private final ModelPart shape4x;
    private final ModelPart shape4y;
    private final ModelPart shape4z;
    private final ModelPart shape4aa;
    private final ModelPart shape4ab;
    private final ModelPart shape4ac;
    private final ModelPart shape4ad;
    private final ModelPart shape4ae;
    private final ModelPart shape4af;
    private final ModelPart shape4ag;
    private final ModelPart shape4ah;
    private final ModelPart shape4ai;
    private final ModelPart shape4aj;

    public ModelCondenser(ModelPart root) {
        this.shape2 = root.getChild("shape2");
        this.shape2a = root.getChild("shape2a");
        this.shape2b = root.getChild("shape2b");
        this.shape2c = root.getChild("shape2c");
        this.shape3 = root.getChild("shape3");
        this.shape3a = root.getChild("shape3a");
        this.shape1 = root.getChild("shape1");
        this.shape4 = root.getChild("shape4");
        this.shape4a = root.getChild("shape4a");
        this.shape4b = root.getChild("shape4b");
        this.shape4c = root.getChild("shape4c");
        this.shape4d = root.getChild("shape4d");
        this.shape4e = root.getChild("shape4e");
        this.shape4f = root.getChild("shape4f");
        this.shape4g = root.getChild("shape4g");
        this.shape4h = root.getChild("shape4h");
        this.shape4i = root.getChild("shape4i");
        this.shape4j = root.getChild("shape4j");
        this.shape4k = root.getChild("shape4k");
        this.shape4l = root.getChild("shape4l");
        this.shape4m = root.getChild("shape4m");
        this.shape4n = root.getChild("shape4n");
        this.shape4o = root.getChild("shape4o");
        this.shape4p = root.getChild("shape4p");
        this.shape4q = root.getChild("shape4q");
        this.shape4r = root.getChild("shape4r");
        this.shape4s = root.getChild("shape4s");
        this.shape4t = root.getChild("shape4t");
        this.shape4u = root.getChild("shape4u");
        this.shape4w = root.getChild("shape4w");
        this.shape4x = root.getChild("shape4x");
        this.shape4y = root.getChild("shape4y");
        this.shape4z = root.getChild("shape4z");
        this.shape4aa = root.getChild("shape4aa");
        this.shape4ab = root.getChild("shape4ab");
        this.shape4ac = root.getChild("shape4ac");
        this.shape4ad = root.getChild("shape4ad");
        this.shape4ae = root.getChild("shape4ae");
        this.shape4af = root.getChild("shape4af");
        this.shape4ag = root.getChild("shape4ag");
        this.shape4ah = root.getChild("shape4ah");
        this.shape4ai = root.getChild("shape4ai");
        this.shape4aj = root.getChild("shape4aj");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("shape2",
                CubeListBuilder.create().mirror(true).texOffs(0, 65)
                        .addBox(0F, 0F, 0F, 2F, 1F, 14F),
                PartPose.offsetAndRotation(-6F, 22F, -7F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape2a",
                CubeListBuilder.create().mirror(true).texOffs(0, 82)
                        .addBox(0F, 0F, 0F, 2F, 1F, 14F),
                PartPose.offsetAndRotation(-3F, 22F, -7F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape2b",
                CubeListBuilder.create().mirror(true).texOffs(20, 0)
                        .addBox(0F, 0F, 0F, 2F, 1F, 14F),
                PartPose.offsetAndRotation(4F, 22F, -7F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape2c",
                CubeListBuilder.create().mirror(true).texOffs(0, 99)
                        .addBox(0F, 0F, 0F, 2F, 1F, 14F),
                PartPose.offsetAndRotation(1F, 22F, -7F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape3",
                CubeListBuilder.create().mirror(true).texOffs(60, 0)
                        .addBox(0F, 0F, 0F, 16F, 3F, 16F),
                PartPose.offsetAndRotation(-8F, 8F, -8F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape3a",
                CubeListBuilder.create().mirror(true).texOffs(0, 22)
                        .addBox(0F, 0F, 0F, 16F, 3F, 16F),
                PartPose.offsetAndRotation(-8F, 19F, -8F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape1",
                CubeListBuilder.create().mirror(true).texOffs(0, 44)
                        .addBox(0F, 0F, 0F, 12F, 8F, 12F),
                PartPose.offsetAndRotation(-6F, 11F, -6F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(0F, 0F, 0F, 1F, 8F, 1F),
                PartPose.offsetAndRotation(-4F, 11F, -7F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4a",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(0F, 0F, 0F, 1F, 3F, 1F),
                PartPose.offsetAndRotation(0F, 16F, -7F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4b",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(0F, 0F, 0F, 1F, 8F, 1F),
                PartPose.offsetAndRotation(6F, 11F, 1F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4c",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(0F, 0F, 0F, 1F, 6F, 1F),
                PartPose.offsetAndRotation(4F, 13F, -7F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4d",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(0F, 0F, 0F, 1F, 2F, 1F),
                PartPose.offsetAndRotation(2F, 11F, -7F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4e",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(0F, 0F, 0F, 1F, 6F, 1F),
                PartPose.offsetAndRotation(-2F, 11F, -7F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4f",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(0F, 0F, 0F, 1F, 1F, 1F),
                PartPose.offsetAndRotation(-1F, 16F, -7F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4g",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(0F, 0F, 0F, 1F, 5F, 1F),
                PartPose.offsetAndRotation(2F, 14F, -7F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4h",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(0F, 0F, 0F, 1F, 4F, 1F),
                PartPose.offsetAndRotation(0F, 11F, -7F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4i",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(0F, 0F, 0F, 1F, 1F, 1F),
                PartPose.offsetAndRotation(1F, 14F, -7F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4j",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(0F, 0F, 0F, 4F, 1F, 1F),
                PartPose.offsetAndRotation(3F, 12F, -7F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4k",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(0F, 0F, 0F, 1F, 1F, 3F),
                PartPose.offsetAndRotation(6F, 15F, -5F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4l",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(0F, 0F, 0F, 1F, 1F, 2F),
                PartPose.offsetAndRotation(6F, 12F, -6F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4m",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(0F, 0F, 0F, 1F, 1F, 1F),
                PartPose.offsetAndRotation(5F, 13F, 6F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4n",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(0F, 0F, 0F, 1F, 2F, 1F),
                PartPose.offsetAndRotation(6F, 13F, -5F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4o",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(0F, 0F, 0F, 1F, 3F, 1F),
                PartPose.offsetAndRotation(6F, 16F, -3F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4p",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(0F, 0F, 0F, 1F, 3F, 1F),
                PartPose.offsetAndRotation(6F, 11F, -3F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4q",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(0F, 0F, 0F, 1F, 3F, 1F),
                PartPose.offsetAndRotation(4F, 11F, 6F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4r",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(0F, 0F, 0F, 1F, 6F, 1F),
                PartPose.offsetAndRotation(6F, 13F, -1F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4s",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(0F, 0F, 0F, 1F, 1F, 3F),
                PartPose.offsetAndRotation(-7F, 15F, -2F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4t",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(0F, 0F, 0F, 1F, 1F, 1F),
                PartPose.offsetAndRotation(6F, 13F, -2F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4u",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(0F, 0F, 0F, 1F, 1F, 2F),
                PartPose.offsetAndRotation(6F, 13F, 5F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4w",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(0F, 0F, 0F, 1F, 6F, 1F),
                PartPose.offsetAndRotation(6F, 13F, 4F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4x",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(0F, 0F, 0F, 1F, 8F, 1F),
                PartPose.offsetAndRotation(2F, 11F, 6F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4y",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(0F, 0F, 0F, 1F, 8F, 1F),
                PartPose.offsetAndRotation(0F, 11F, 6F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4z",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(0F, 0F, 0F, 1F, 2F, 1F),
                PartPose.offsetAndRotation(-4F, 11F, 6F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4aa",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(0F, 0F, 0F, 1F, 5F, 1F),
                PartPose.offsetAndRotation(-3F, 14F, 6F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4ab",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(0F, 0F, 0F, 1F, 4F, 1F),
                PartPose.offsetAndRotation(-2F, 11F, 6F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4ac",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(0F, 0F, 0F, 1F, 7F, 1F),
                PartPose.offsetAndRotation(-5F, 12F, 6F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4ad",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(0F, 0F, 0F, 1F, 4F, 1F),
                PartPose.offsetAndRotation(-7F, 11F, -2F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4ae",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(0F, 0F, 0F, 1F, 3F, 1F),
                PartPose.offsetAndRotation(-7F, 16F, -5F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4af",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(0F, 0F, 0F, 1F, 6F, 1F),
                PartPose.offsetAndRotation(-7F, 13F, 4F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4ag",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(0F, 0F, 0F, 1F, 2F, 1F),
                PartPose.offsetAndRotation(-7F, 11F, 0F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4ah",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(0F, 0F, 0F, 1F, 1F, 4F),
                PartPose.offsetAndRotation(-7F, 13F, 0F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4ai",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(0F, 0F, 0F, 1F, 4F, 1F),
                PartPose.offsetAndRotation(-7F, 15F, 1F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4aj",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(0F, 0F, 0F, 1F, 6F, 1F),
                PartPose.offsetAndRotation(-7F, 11F, -4F, 0F, 0F, 0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    public void renderAll(PoseStack stack, VertexConsumer vc, int light) {
        shape2.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2b.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2c.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape1.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4b.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4c.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4d.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4e.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4f.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4g.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4h.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4i.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4j.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4k.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4l.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4m.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4n.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4o.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4p.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4q.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4r.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4s.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4t.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4u.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4w.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4x.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4y.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4z.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4aa.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4ab.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4ac.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4ad.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4ae.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4af.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4ag.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4ah.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4ai.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4aj.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
    }
}
