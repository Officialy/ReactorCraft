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

/** 26.2 port of the Techne model ModelControl (auto-converted by scripts/convert_reactor_model.py). */
public class ModelControl {

    private final ModelPart shape1;
    private final ModelPart shape2;
    private final ModelPart shape2a;
    private final ModelPart shape3;
    private final ModelPart shape3a;
    private final ModelPart shape4;
    private final ModelPart shape4a;
    private final ModelPart shape4b;
    private final ModelPart shape5;
    private final ModelPart shape5a;
    private final ModelPart shape5b;
    private final ModelPart shape5c;
    private final ModelPart shape5d;
    private final ModelPart shape5e;
    private final ModelPart shape5f;
    private final ModelPart shape5g;
    private final ModelPart shape5h;
    private final ModelPart shape5i;
    private final ModelPart shape5j;
    private final ModelPart shape5k;
    private final ModelPart shape6;
    private final ModelPart shape6a;
    private final ModelPart shape6b;
    private final ModelPart shape6c;
    private final ModelPart shape6d;
    private final ModelPart shape6e;
    private final ModelPart shape6f;
    private final ModelPart shape6g;
    private final ModelPart shape6h;
    private final ModelPart shape6u;
    private final ModelPart shape6i;
    private final ModelPart shape6j;
    private final ModelPart shape6k;
    private final ModelPart shape6l;
    private final ModelPart shape6m;
    private final ModelPart shape6n;
    private final ModelPart shape7;
    private final ModelPart shape7a;
    private final ModelPart shape7b;
    private final ModelPart shape7c;
    private final ModelPart shape8;
    private final ModelPart shape9;
    private final ModelPart shape9a;

    public ModelControl(ModelPart root) {
        this.shape1 = root.getChild("shape1");
        this.shape2 = root.getChild("shape2");
        this.shape2a = root.getChild("shape2a");
        this.shape3 = root.getChild("shape3");
        this.shape3a = root.getChild("shape3a");
        this.shape4 = root.getChild("shape4");
        this.shape4a = root.getChild("shape4a");
        this.shape4b = root.getChild("shape4b");
        this.shape5 = root.getChild("shape5");
        this.shape5a = root.getChild("shape5a");
        this.shape5b = root.getChild("shape5b");
        this.shape5c = root.getChild("shape5c");
        this.shape5d = root.getChild("shape5d");
        this.shape5e = root.getChild("shape5e");
        this.shape5f = root.getChild("shape5f");
        this.shape5g = root.getChild("shape5g");
        this.shape5h = root.getChild("shape5h");
        this.shape5i = root.getChild("shape5i");
        this.shape5j = root.getChild("shape5j");
        this.shape5k = root.getChild("shape5k");
        this.shape6 = root.getChild("shape6");
        this.shape6a = root.getChild("shape6a");
        this.shape6b = root.getChild("shape6b");
        this.shape6c = root.getChild("shape6c");
        this.shape6d = root.getChild("shape6d");
        this.shape6e = root.getChild("shape6e");
        this.shape6f = root.getChild("shape6f");
        this.shape6g = root.getChild("shape6g");
        this.shape6h = root.getChild("shape6h");
        this.shape6u = root.getChild("shape6u");
        this.shape6i = root.getChild("shape6i");
        this.shape6j = root.getChild("shape6j");
        this.shape6k = root.getChild("shape6k");
        this.shape6l = root.getChild("shape6l");
        this.shape6m = root.getChild("shape6m");
        this.shape6n = root.getChild("shape6n");
        this.shape7 = root.getChild("shape7");
        this.shape7a = root.getChild("shape7a");
        this.shape7b = root.getChild("shape7b");
        this.shape7c = root.getChild("shape7c");
        this.shape8 = root.getChild("shape8");
        this.shape9 = root.getChild("shape9");
        this.shape9a = root.getChild("shape9a");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("shape1",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(0F, 0F, 0F, 16F, 1F, 16F),
                PartPose.offsetAndRotation(-8F, 23F, -8F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape2",
                CubeListBuilder.create().mirror(true).texOffs(38, 19)
                        .addBox(0F, 0F, 0F, 2F, 15F, 16F),
                PartPose.offsetAndRotation(6F, 8F, -8F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape2a",
                CubeListBuilder.create().mirror(true).texOffs(0, 19)
                        .addBox(0F, 0F, 0F, 2F, 15F, 16F),
                PartPose.offsetAndRotation(-8F, 8F, -8F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape3",
                CubeListBuilder.create().mirror(true).texOffs(29, 52)
                        .addBox(0F, 0F, 0F, 12F, 15F, 2F),
                PartPose.offsetAndRotation(-6F, 8F, 6F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape3a",
                CubeListBuilder.create().mirror(true).texOffs(0, 52)
                        .addBox(0F, 0F, 0F, 12F, 15F, 2F),
                PartPose.offsetAndRotation(-6F, 8F, -8F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4",
                CubeListBuilder.create().mirror(true).texOffs(0, 71)
                        .addBox(0F, 0F, 0F, 1F, 15F, 12F),
                PartPose.offsetAndRotation(3F, 8F, -6F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4a",
                CubeListBuilder.create().mirror(true).texOffs(0, 71)
                        .addBox(0F, 0F, 0F, 1F, 15F, 12F),
                PartPose.offsetAndRotation(-4F, 8F, -6F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4b",
                CubeListBuilder.create().mirror(true).texOffs(0, 99)
                        .addBox(0F, 0F, 0F, 2F, 15F, 12F),
                PartPose.offsetAndRotation(-1F, 8F, -6F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape5",
                CubeListBuilder.create().mirror(true).texOffs(66, 0)
                        .addBox(0F, 0F, 0F, 2F, 15F, 1F),
                PartPose.offsetAndRotation(4F, 8F, -4F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape5a",
                CubeListBuilder.create().mirror(true).texOffs(74, 0)
                        .addBox(0F, 0F, 0F, 2F, 15F, 2F),
                PartPose.offsetAndRotation(-6F, 8F, -1F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape5b",
                CubeListBuilder.create().mirror(true).texOffs(66, 0)
                        .addBox(0F, 0F, 0F, 2F, 15F, 1F),
                PartPose.offsetAndRotation(-3F, 8F, -4F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape5c",
                CubeListBuilder.create().mirror(true).texOffs(66, 0)
                        .addBox(0F, 0F, 0F, 2F, 15F, 1F),
                PartPose.offsetAndRotation(1F, 8F, -4F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape5d",
                CubeListBuilder.create().mirror(true).texOffs(66, 0)
                        .addBox(0F, 0F, 0F, 2F, 15F, 1F),
                PartPose.offsetAndRotation(-6F, 8F, -4F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape5e",
                CubeListBuilder.create().mirror(true).texOffs(66, 0)
                        .addBox(0F, 0F, 0F, 2F, 15F, 1F),
                PartPose.offsetAndRotation(-6F, 8F, 3F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape5f",
                CubeListBuilder.create().mirror(true).texOffs(66, 0)
                        .addBox(0F, 0F, 0F, 2F, 15F, 1F),
                PartPose.offsetAndRotation(-3F, 8F, 3F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape5g",
                CubeListBuilder.create().mirror(true).texOffs(66, 0)
                        .addBox(0F, 0F, 0F, 2F, 15F, 1F),
                PartPose.offsetAndRotation(1F, 8F, 3F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape5h",
                CubeListBuilder.create().mirror(true).texOffs(66, 0)
                        .addBox(0F, 0F, 0F, 2F, 15F, 1F),
                PartPose.offsetAndRotation(4F, 8F, 3F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape5i",
                CubeListBuilder.create().mirror(true).texOffs(74, 0)
                        .addBox(0F, 0F, 0F, 2F, 15F, 2F),
                PartPose.offsetAndRotation(4F, 8F, -1F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape5j",
                CubeListBuilder.create().mirror(true).texOffs(74, 0)
                        .addBox(0F, 0F, 0F, 2F, 15F, 2F),
                PartPose.offsetAndRotation(1F, 8F, -1F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape5k",
                CubeListBuilder.create().mirror(true).texOffs(74, 0)
                        .addBox(0F, 0F, 0F, 2F, 15F, 2F),
                PartPose.offsetAndRotation(-3F, 8F, -1F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape6",
                CubeListBuilder.create().mirror(true).texOffs(84, 0)
                        .addBox(0F, 0F, 0F, 2F, 15F, 2F),
                PartPose.offsetAndRotation(4F, 6F, -3F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape6a",
                CubeListBuilder.create().mirror(true).texOffs(84, 0)
                        .addBox(0F, 0F, 0F, 2F, 15F, 2F),
                PartPose.offsetAndRotation(-6F, 6F, -6F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape6b",
                CubeListBuilder.create().mirror(true).texOffs(84, 0)
                        .addBox(0F, 0F, 0F, 2F, 15F, 2F),
                PartPose.offsetAndRotation(-3F, 6F, -6F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape6c",
                CubeListBuilder.create().mirror(true).texOffs(84, 0)
                        .addBox(0F, 0F, 0F, 2F, 15F, 2F),
                PartPose.offsetAndRotation(-6F, 6F, 4F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape6d",
                CubeListBuilder.create().mirror(true).texOffs(84, 0)
                        .addBox(0F, 0F, 0F, 2F, 15F, 2F),
                PartPose.offsetAndRotation(4F, 6F, -6F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape6e",
                CubeListBuilder.create().mirror(true).texOffs(84, 0)
                        .addBox(0F, 0F, 0F, 2F, 15F, 2F),
                PartPose.offsetAndRotation(1F, 6F, -6F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape6f",
                CubeListBuilder.create().mirror(true).texOffs(84, 0)
                        .addBox(0F, 0F, 0F, 2F, 15F, 2F),
                PartPose.offsetAndRotation(1F, 6F, -3F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape6g",
                CubeListBuilder.create().mirror(true).texOffs(84, 0)
                        .addBox(0F, 0F, 0F, 2F, 15F, 2F),
                PartPose.offsetAndRotation(-3F, 6F, -3F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape6h",
                CubeListBuilder.create().mirror(true).texOffs(84, 0)
                        .addBox(0F, 0F, 0F, 2F, 15F, 2F),
                PartPose.offsetAndRotation(-6F, 6F, -3F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape6u",
                CubeListBuilder.create().mirror(true).texOffs(84, 0)
                        .addBox(0F, 0F, 0F, 2F, 15F, 2F),
                PartPose.offsetAndRotation(-6F, 6F, 1F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape6i",
                CubeListBuilder.create().mirror(true).texOffs(84, 0)
                        .addBox(0F, 0F, 0F, 2F, 15F, 2F),
                PartPose.offsetAndRotation(-3F, 6F, 1F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape6j",
                CubeListBuilder.create().mirror(true).texOffs(84, 0)
                        .addBox(0F, 0F, 0F, 2F, 15F, 2F),
                PartPose.offsetAndRotation(1F, 6F, 1F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape6k",
                CubeListBuilder.create().mirror(true).texOffs(84, 0)
                        .addBox(0F, 0F, 0F, 2F, 15F, 2F),
                PartPose.offsetAndRotation(4F, 6F, 1F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape6l",
                CubeListBuilder.create().mirror(true).texOffs(84, 0)
                        .addBox(0F, 0F, 0F, 2F, 15F, 2F),
                PartPose.offsetAndRotation(4F, 6F, 4F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape6m",
                CubeListBuilder.create().mirror(true).texOffs(84, 0)
                        .addBox(0F, 0F, 0F, 2F, 15F, 2F),
                PartPose.offsetAndRotation(1F, 6F, 4F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape6n",
                CubeListBuilder.create().mirror(true).texOffs(84, 0)
                        .addBox(0F, 0F, 0F, 2F, 15F, 2F),
                PartPose.offsetAndRotation(-3F, 6F, 4F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape7",
                CubeListBuilder.create().mirror(true).texOffs(94, 0)
                        .addBox(0F, 0F, 0F, 1F, 1F, 10F),
                PartPose.offsetAndRotation(4.5F, 5.2F, -5F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape7a",
                CubeListBuilder.create().mirror(true).texOffs(94, 0)
                        .addBox(0F, 0F, 0F, 1F, 1F, 10F),
                PartPose.offsetAndRotation(-5.5F, 5.2F, -5F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape7b",
                CubeListBuilder.create().mirror(true).texOffs(94, 0)
                        .addBox(0F, 0F, 0F, 1F, 1F, 10F),
                PartPose.offsetAndRotation(1.5F, 5.2F, -5F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape7c",
                CubeListBuilder.create().mirror(true).texOffs(94, 0)
                        .addBox(0F, 0F, 0F, 1F, 1F, 10F),
                PartPose.offsetAndRotation(-2.5F, 5.2F, -5F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape8",
                CubeListBuilder.create().mirror(true).texOffs(28, 71)
                        .addBox(0F, 0F, 0F, 12F, 1F, 12F),
                PartPose.offsetAndRotation(-6F, 4.5F, -6F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape9",
                CubeListBuilder.create().mirror(true).texOffs(76, 20)
                        .addBox(-1F, 0F, -1F, 2F, 15F, 2F),
                PartPose.offsetAndRotation(0F, 5F, 0F, 0F, 0.7853982F, 0F));
        root.addOrReplaceChild("shape9a",
                CubeListBuilder.create().mirror(true).texOffs(76, 20)
                        .addBox(-1F, 0F, -1F, 2F, 15F, 2F),
                PartPose.offsetAndRotation(0F, 5F, 0F, 0F, 0F, 0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    public void renderAll(PoseStack stack, VertexConsumer vc, int light, float rodPosition) {
        shape1.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4b.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape5.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape5a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape5b.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape5c.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape5d.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape5e.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape5f.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape5g.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape5h.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape5i.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape5j.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape5k.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        // The whole rod assembly (shape6* bundle, shape7* tie-bars, shape8 cap, shape9/9a centre rod)
        // slides vertically by the rod's insertion depth. The legacy renderAll applied
        // glTranslated(0, -phi/28, 0) once with NO matching pop, so EVERY shape from shape6 onward
        // moved together — the port had wrongly popped after shape6n, pinning shape7/8/9/9a (incl. the
        // centre rod) in place. Keep the translate live through shape9a to match the original.
        stack.pushPose();
        stack.translate(0, -rodPosition / 28.0, 0);
        shape6.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape6a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape6b.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape6c.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape6d.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape6e.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape6f.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape6g.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape6h.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape6u.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape6i.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape6j.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape6k.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape6l.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape6m.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape6n.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape7.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape7a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape7b.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape7c.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape8.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape9.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape9a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        stack.popPose();
    }
}
