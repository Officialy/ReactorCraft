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

public class ModelReactorPump {

    private final ModelPart shape1;
    private final ModelPart shape2;
    private final ModelPart shape2a;
    private final ModelPart shape3;
    private final ModelPart shape3a;
    private final ModelPart shape3b;
    private final ModelPart shape3d;
    private final ModelPart shape3c;
    private final ModelPart shape1a;
    private final ModelPart shape3e;
    private final ModelPart shape3f;
    private final ModelPart shape3g;
    private final ModelPart shape3h;
    private final ModelPart shape3i;
    private final ModelPart shape3j;
    private final ModelPart shape3k;
    private final ModelPart shape3l;
    private final ModelPart shape3m;
    private final ModelPart shape3n;
    private final ModelPart shape3o;
    private final ModelPart shape3p;
    private final ModelPart shape3q;
    private final ModelPart shape3r;
    private final ModelPart shape3s;
    private final ModelPart shape3t;
    private final ModelPart shape3u;
    private final ModelPart shape3v;
    private final ModelPart shape3w;
    private final ModelPart shape3x;
    private final ModelPart shape3y;
    private final ModelPart shape3z;
    private final ModelPart shape3aa;
    private final ModelPart shape3ab;
    private final ModelPart shape3ac;
    private final ModelPart shape3ad;
    private final ModelPart shape3ae;
    private final ModelPart shape3af;
    private final ModelPart shape1b;
    private final ModelPart shape1c;
    private final ModelPart shape1d;
    private final ModelPart shape4;
    private final ModelPart shape4a;
    private final ModelPart shape4b;
    private final ModelPart shape4c;
    private final ModelPart shape5;
    private final ModelPart shape5a;
    private final ModelPart shape6;
    private final ModelPart shape6a;

    public ModelReactorPump(ModelPart root) {
        this.shape1 = root.getChild("shape1");
        this.shape2 = root.getChild("shape2");
        this.shape2a = root.getChild("shape2a");
        this.shape3 = root.getChild("shape3");
        this.shape3a = root.getChild("shape3a");
        this.shape3b = root.getChild("shape3b");
        this.shape3d = root.getChild("shape3d");
        this.shape3c = root.getChild("shape3c");
        this.shape1a = root.getChild("shape1a");
        this.shape3e = root.getChild("shape3e");
        this.shape3f = root.getChild("shape3f");
        this.shape3g = root.getChild("shape3g");
        this.shape3h = root.getChild("shape3h");
        this.shape3i = root.getChild("shape3i");
        this.shape3j = root.getChild("shape3j");
        this.shape3k = root.getChild("shape3k");
        this.shape3l = root.getChild("shape3l");
        this.shape3m = root.getChild("shape3m");
        this.shape3n = root.getChild("shape3n");
        this.shape3o = root.getChild("shape3o");
        this.shape3p = root.getChild("shape3p");
        this.shape3q = root.getChild("shape3q");
        this.shape3r = root.getChild("shape3r");
        this.shape3s = root.getChild("shape3s");
        this.shape3t = root.getChild("shape3t");
        this.shape3u = root.getChild("shape3u");
        this.shape3v = root.getChild("shape3v");
        this.shape3w = root.getChild("shape3w");
        this.shape3x = root.getChild("shape3x");
        this.shape3y = root.getChild("shape3y");
        this.shape3z = root.getChild("shape3z");
        this.shape3aa = root.getChild("shape3aa");
        this.shape3ab = root.getChild("shape3ab");
        this.shape3ac = root.getChild("shape3ac");
        this.shape3ad = root.getChild("shape3ad");
        this.shape3ae = root.getChild("shape3ae");
        this.shape3af = root.getChild("shape3af");
        this.shape1b = root.getChild("shape1b");
        this.shape1c = root.getChild("shape1c");
        this.shape1d = root.getChild("shape1d");
        this.shape4 = root.getChild("shape4");
        this.shape4a = root.getChild("shape4a");
        this.shape4b = root.getChild("shape4b");
        this.shape4c = root.getChild("shape4c");
        this.shape5 = root.getChild("shape5");
        this.shape5a = root.getChild("shape5a");
        this.shape6 = root.getChild("shape6");
        this.shape6a = root.getChild("shape6a");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("shape1",
                CubeListBuilder.create().mirror(true).texOffs(46, 18)
                        .addBox(0F, 0F, 0F, 6F, 1F, 16F),
                PartPose.offsetAndRotation(2F, 9F, -8F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape2",
                CubeListBuilder.create().mirror(true).texOffs(66, 0)
                        .addBox(-1F, 0F, -1F, 2F, 13F, 2F),
                PartPose.offsetAndRotation(0F, 10F, 0F, 0F, 0.7853982F, 0F));
        root.addOrReplaceChild("shape2a",
                CubeListBuilder.create().mirror(true).texOffs(66, 0)
                        .addBox(-1F, 0F, -1F, 2F, 13F, 2F),
                PartPose.offsetAndRotation(0F, 10F, 0F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape3",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-2F, 0F, -0.5F, 4F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 11F, 0F, 0F, 2.094395F, 0F));
        root.addOrReplaceChild("shape3a",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-7F, 0F, -0.5F, 14F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 21F, 0F, 0F, 1.570796F, 0F));
        root.addOrReplaceChild("shape3b",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-6F, 0F, -0.5F, 12F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 19F, 0F, 0F, 2.617994F, 0F));
        root.addOrReplaceChild("shape3d",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-5F, 0F, -0.5F, 10F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 17F, 0F, 0F, 2.617994F, 0F));
        root.addOrReplaceChild("shape3c",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-4F, 0F, -0.5F, 8F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 15F, 0F, 0F, 1.570796F, 0F));
        root.addOrReplaceChild("shape1a",
                CubeListBuilder.create().mirror(true).texOffs(0, 37)
                        .addBox(0F, 0F, 0F, 16F, 1F, 16F),
                PartPose.offsetAndRotation(-8F, 23F, -8F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape3e",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-3F, 0F, -0.5F, 6F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 13F, 0F, 0F, 2.617994F, 0F));
        root.addOrReplaceChild("shape3f",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-2F, 0F, -0.5F, 4F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 11F, 0F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape3g",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-2F, 0F, -0.5F, 4F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 11F, 0F, 0F, 1.047198F, 0F));
        root.addOrReplaceChild("shape3h",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-3F, 0F, -0.5F, 6F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 13F, 0F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape3i",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-3F, 0F, -0.5F, 6F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 13F, 0F, 0F, 2.094395F, 0F));
        root.addOrReplaceChild("shape3j",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-4F, 0F, -0.5F, 8F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 15F, 0F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape3k",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-4F, 0F, -0.5F, 8F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 15F, 0F, 0F, 2.094395F, 0F));
        root.addOrReplaceChild("shape3l",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-5F, 0F, -0.5F, 10F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 17F, 0F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape3m",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-5F, 0F, -0.5F, 10F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 17F, 0F, 0F, 1.047198F, 0F));
        root.addOrReplaceChild("shape3n",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-6F, 0F, -0.5F, 12F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 19F, 0F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape3o",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-6F, 0F, -0.5F, 12F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 19F, 0F, 0F, 1.047198F, 0F));
        root.addOrReplaceChild("shape3p",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-7F, 0F, -0.5F, 14F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 21F, 0F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape3q",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-7F, 0F, -0.5F, 14F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 21F, 0F, 0F, 1.047198F, 0F));
        root.addOrReplaceChild("shape3r",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-7F, 0F, -0.5F, 14F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 21F, 0F, 0F, 2.094395F, 0F));
        root.addOrReplaceChild("shape3s",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-7F, 0F, -0.5F, 14F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 21F, 0F, 0F, 2.617994F, 0F));
        root.addOrReplaceChild("shape3t",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-7F, 0F, -0.5F, 14F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 21F, 0F, 0F, 0.5235988F, 0F));
        root.addOrReplaceChild("shape3u",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-6F, 0F, -0.5F, 12F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 19F, 0F, 0F, 2.094395F, 0F));
        root.addOrReplaceChild("shape3v",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-6F, 0F, -0.5F, 12F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 19F, 0F, 0F, 0.5235988F, 0F));
        root.addOrReplaceChild("shape3w",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-6F, 0F, -0.5F, 12F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 19F, 0F, 0F, 1.570796F, 0F));
        root.addOrReplaceChild("shape3x",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-5F, 0F, -0.5F, 10F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 17F, 0F, 0F, 2.094395F, 0F));
        root.addOrReplaceChild("shape3y",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-5F, 0F, -0.5F, 10F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 17F, 0F, 0F, 1.570796F, 0F));
        root.addOrReplaceChild("shape3z",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-5F, 0F, -0.5F, 10F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 17F, 0F, 0F, 0.5235988F, 0F));
        root.addOrReplaceChild("shape3aa",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-4F, 0F, -0.5F, 8F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 15F, 0F, 0F, 1.047198F, 0F));
        root.addOrReplaceChild("shape3ab",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-4F, 0F, -0.5F, 8F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 15F, 0F, 0F, 0.5235988F, 0F));
        root.addOrReplaceChild("shape3ac",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-4F, 0F, -0.5F, 8F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 15F, 0F, 0F, 2.617994F, 0F));
        root.addOrReplaceChild("shape3ad",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-3F, 0F, -0.5F, 6F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 13F, 0F, 0F, 1.047198F, 0F));
        root.addOrReplaceChild("shape3ae",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-3F, 0F, -0.5F, 6F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 13F, 0F, 0F, 1.570796F, 0F));
        root.addOrReplaceChild("shape3af",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-3F, 0F, -0.5F, 6F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 13F, 0F, 0F, 0.5235988F, 0F));
        root.addOrReplaceChild("shape1b",
                CubeListBuilder.create().mirror(true).texOffs(0, 10)
                        .addBox(0F, 0F, 0F, 4F, 1F, 6F),
                PartPose.offsetAndRotation(-2F, 9F, 2F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape1c",
                CubeListBuilder.create().mirror(true).texOffs(0, 18)
                        .addBox(0F, 0F, 0F, 6F, 1F, 16F),
                PartPose.offsetAndRotation(-8F, 9F, -8F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape1d",
                CubeListBuilder.create().mirror(true).texOffs(21, 10)
                        .addBox(0F, 0F, 0F, 4F, 1F, 6F),
                PartPose.offsetAndRotation(-2F, 9F, -8F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4",
                CubeListBuilder.create().mirror(true).texOffs(0, 7)
                        .addBox(0F, 0F, 0F, 4F, 1F, 1F),
                PartPose.offsetAndRotation(-2F, 8F, 2F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4a",
                CubeListBuilder.create().mirror(true).texOffs(31, 0)
                        .addBox(0F, 0F, 0F, 1F, 1F, 6F),
                PartPose.offsetAndRotation(2F, 8F, -3F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4b",
                CubeListBuilder.create().mirror(true).texOffs(47, 0)
                        .addBox(0F, 0F, 0F, 1F, 1F, 6F),
                PartPose.offsetAndRotation(-3F, 8F, -3F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4c",
                CubeListBuilder.create().mirror(true).texOffs(0, 4)
                        .addBox(0F, 0F, 0F, 4F, 1F, 1F),
                PartPose.offsetAndRotation(-2F, 8F, -3F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape5",
                CubeListBuilder.create().mirror(true).texOffs(35, 56)
                        .addBox(0F, 0F, 0F, 16F, 13F, 1F),
                PartPose.offsetAndRotation(-8F, 10F, 7F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape5a",
                CubeListBuilder.create().mirror(true).texOffs(0, 56)
                        .addBox(0F, 0F, 0F, 16F, 13F, 1F),
                PartPose.offsetAndRotation(-8F, 10F, -8F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape6",
                CubeListBuilder.create().mirror(true).texOffs(32, 72)
                        .addBox(0F, 0F, 0F, 1F, 13F, 14F),
                PartPose.offsetAndRotation(7F, 10F, -7F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape6a",
                CubeListBuilder.create().mirror(true).texOffs(0, 72)
                        .addBox(0F, 0F, 0F, 1F, 13F, 14F),
                PartPose.offsetAndRotation(-8F, 10F, -7F, 0F, 0F, 0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    /** phi (degrees) spins the impeller assembly (shape2/shape2a + all shape3*). */
    public void renderAll(PoseStack stack, VertexConsumer vc, int light, float phi) {
        shape1.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape1a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);

        stack.pushPose();
        stack.mulPose(Axis.YP.rotationDegrees(phi));
        shape2.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3b.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3d.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3c.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3e.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3f.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3g.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3h.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3i.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3j.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3k.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3l.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3m.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3n.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3o.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3p.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3q.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3r.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3s.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3t.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3u.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3v.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3w.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3x.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3y.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3z.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3aa.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3ab.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3ac.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3ad.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3ae.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3af.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        stack.popPose();

        shape1b.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape1c.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape1d.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4b.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4c.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape5.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape5a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape6.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape6a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
    }
}
