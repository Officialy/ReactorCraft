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

/** 26.2 port of the Techne model ModelMagnet (auto-converted by scripts/convert_reactor_model.py). */
public class ModelMagnet {

    private final ModelPart shape1;
    private final ModelPart shape1a;
    private final ModelPart shape1b;
    private final ModelPart shape1c;
    private final ModelPart shape1d;
    private final ModelPart shape1e;
    private final ModelPart shape3;
    private final ModelPart shape3a;
    private final ModelPart shape2a;
    private final ModelPart shape2;
    private final ModelPart shape2b;
    private final ModelPart shape4;
    private final ModelPart shape4a;
    private final ModelPart shape4b;
    private final ModelPart shape4c;
    private final ModelPart shape4d;
    private final ModelPart shape2c;
    private final ModelPart shape2d;
    private final ModelPart shape2e;
    private final ModelPart shape2f;
    private final ModelPart shape2g;
    private final ModelPart shape2h;
    private final ModelPart shape2i;
    private final ModelPart shape2j;
    private final ModelPart shape2k;
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
    private final ModelPart shape1f;
    private final ModelPart shape1g;
    private final ModelPart shape3b;
    private final ModelPart shape1h;
    private final ModelPart shape1i;
    private final ModelPart shape3c;
    private final ModelPart shape1j;
    private final ModelPart shape1k;
    private final ModelPart shape5;
    private final ModelPart shape5a;
    private final ModelPart shape5b;
    private final ModelPart shape5d;
    private final ModelPart shape5e;
    private final ModelPart shape5f;
    private final ModelPart shape5g;
    private final ModelPart shape5h;
    private final ModelPart shape5i;
    private final ModelPart shape5j;
    private final ModelPart shape5k;
    private final ModelPart shape5l;
    private final ModelPart shape5m;
    private final ModelPart shape5n;
    private final ModelPart shape5o;
    private final ModelPart shape5p;
    private final ModelPart shape6;
    private final ModelPart shape6a;
    private final ModelPart shape6b;
    private final ModelPart shape6c;
    private final ModelPart shape6d;
    private final ModelPart shape6e;
    private final ModelPart shape6f;
    private final ModelPart shape7;
    private final ModelPart shape7a;
    private final ModelPart shape7b;
    private final ModelPart shape7c;
    private final ModelPart shape7d;
    private final ModelPart shape7e;

    public ModelMagnet(ModelPart root) {
        this.shape1 = root.getChild("shape1");
        this.shape1a = root.getChild("shape1a");
        this.shape1b = root.getChild("shape1b");
        this.shape1c = root.getChild("shape1c");
        this.shape1d = root.getChild("shape1d");
        this.shape1e = root.getChild("shape1e");
        this.shape3 = root.getChild("shape3");
        this.shape3a = root.getChild("shape3a");
        this.shape2a = root.getChild("shape2a");
        this.shape2 = root.getChild("shape2");
        this.shape2b = root.getChild("shape2b");
        this.shape4 = root.getChild("shape4");
        this.shape4a = root.getChild("shape4a");
        this.shape4b = root.getChild("shape4b");
        this.shape4c = root.getChild("shape4c");
        this.shape4d = root.getChild("shape4d");
        this.shape2c = root.getChild("shape2c");
        this.shape2d = root.getChild("shape2d");
        this.shape2e = root.getChild("shape2e");
        this.shape2f = root.getChild("shape2f");
        this.shape2g = root.getChild("shape2g");
        this.shape2h = root.getChild("shape2h");
        this.shape2i = root.getChild("shape2i");
        this.shape2j = root.getChild("shape2j");
        this.shape2k = root.getChild("shape2k");
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
        this.shape1f = root.getChild("shape1f");
        this.shape1g = root.getChild("shape1g");
        this.shape3b = root.getChild("shape3b");
        this.shape1h = root.getChild("shape1h");
        this.shape1i = root.getChild("shape1i");
        this.shape3c = root.getChild("shape3c");
        this.shape1j = root.getChild("shape1j");
        this.shape1k = root.getChild("shape1k");
        this.shape5 = root.getChild("shape5");
        this.shape5a = root.getChild("shape5a");
        this.shape5b = root.getChild("shape5b");
        this.shape5d = root.getChild("shape5d");
        this.shape5e = root.getChild("shape5e");
        this.shape5f = root.getChild("shape5f");
        this.shape5g = root.getChild("shape5g");
        this.shape5h = root.getChild("shape5h");
        this.shape5i = root.getChild("shape5i");
        this.shape5j = root.getChild("shape5j");
        this.shape5k = root.getChild("shape5k");
        this.shape5l = root.getChild("shape5l");
        this.shape5m = root.getChild("shape5m");
        this.shape5n = root.getChild("shape5n");
        this.shape5o = root.getChild("shape5o");
        this.shape5p = root.getChild("shape5p");
        this.shape6 = root.getChild("shape6");
        this.shape6a = root.getChild("shape6a");
        this.shape6b = root.getChild("shape6b");
        this.shape6c = root.getChild("shape6c");
        this.shape6d = root.getChild("shape6d");
        this.shape6e = root.getChild("shape6e");
        this.shape6f = root.getChild("shape6f");
        this.shape7 = root.getChild("shape7");
        this.shape7a = root.getChild("shape7a");
        this.shape7b = root.getChild("shape7b");
        this.shape7c = root.getChild("shape7c");
        this.shape7d = root.getChild("shape7d");
        this.shape7e = root.getChild("shape7e");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("shape1",
                CubeListBuilder.create().mirror(true).texOffs(0, 18)
                        .addBox(3F, -7.5F, 0F, 18F, 1F, 16F),
                PartPose.offsetAndRotation(-12F, -13F, -8F, 0F, 0F, 2.356194F));
        root.addOrReplaceChild("shape1a",
                CubeListBuilder.create().mirror(true).texOffs(0, 18)
                        .addBox(3F, 6.5F, 0F, 18F, 1F, 16F),
                PartPose.offsetAndRotation(-12F, 45F, -8F, 0F, 0F, -2.356194F));
        root.addOrReplaceChild("shape1b",
                CubeListBuilder.create().mirror(true).texOffs(0, 18)
                        .addBox(-21F, 6.5F, 0F, 18F, 1F, 16F),
                PartPose.offsetAndRotation(12F, 45F, -8F, 0F, 0F, 2.356194F));
        root.addOrReplaceChild("shape1c",
                CubeListBuilder.create().mirror(true).texOffs(0, 18)
                        .addBox(0F, -6.5F, 0F, 18F, 1F, 16F),
                PartPose.offsetAndRotation(-9F, 44F, -8F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape1d",
                CubeListBuilder.create().mirror(true).texOffs(0, 18)
                        .addBox(-21F, -7.5F, 0F, 18F, 1F, 16F),
                PartPose.offsetAndRotation(12F, -13F, -8F, 0F, 0F, -2.356194F));
        root.addOrReplaceChild("shape1e",
                CubeListBuilder.create().mirror(true).texOffs(0, 18)
                        .addBox(0F, 0.5F, 0F, 18F, 1F, 16F),
                PartPose.offsetAndRotation(-9F, -7F, -8F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape3",
                CubeListBuilder.create().mirror(true).texOffs(35, 37)
                        .addBox(-0.5F, -21F, 0F, 1F, 18F, 16F),
                PartPose.offsetAndRotation(21.7F, 28F, -8F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape3a",
                CubeListBuilder.create().mirror(true).texOffs(35, 37)
                        .addBox(6.5F, -21F, 0F, 1F, 18F, 16F),
                PartPose.offsetAndRotation(-28.7F, 28F, -8F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape2a",
                CubeListBuilder.create().mirror(true).texOffs(81, 0)
                        .addBox(27F, -11F, 0F, 1F, 24F, 1F),
                PartPose.offsetAndRotation(0F, 16F, -7F, 0F, 0F, 0.7853982F));
        root.addOrReplaceChild("shape2",
                CubeListBuilder.create().mirror(true).texOffs(81, 0)
                        .addBox(27F, -11F, 0F, 1F, 24F, 1F),
                PartPose.offsetAndRotation(0F, 15F, -7F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape2b",
                CubeListBuilder.create().mirror(true).texOffs(81, 0)
                        .addBox(26.5F, -11F, 0F, 1F, 24F, 1F),
                PartPose.offsetAndRotation(0F, 15F, -7F, 0F, 0F, -0.7853982F));
        root.addOrReplaceChild("shape4",
                CubeListBuilder.create().mirror(true).texOffs(0, 78)
                        .addBox(-11F, 27.7F, -3F, 24F, 1F, 1F),
                PartPose.offsetAndRotation(0F, 15F, -4F, 0F, 0F, 0.8028515F));
        root.addOrReplaceChild("shape4a",
                CubeListBuilder.create().mirror(true).texOffs(0, 78)
                        .addBox(-12F, 27F, -7F, 24F, 1F, 1F),
                PartPose.offsetAndRotation(0F, 15.5F, 0F, 0F, 0F, 1.570796F));
        root.addOrReplaceChild("shape4b",
                CubeListBuilder.create().mirror(true).texOffs(0, 78)
                        .addBox(-12F, -28F, -7F, 24F, 1F, 1F),
                PartPose.offsetAndRotation(0F, 16F, 0F, 0F, 0F, -0.8028515F));
        root.addOrReplaceChild("shape4c",
                CubeListBuilder.create().mirror(true).texOffs(0, 78)
                        .addBox(-12F, 28F, 1F, 24F, 1F, 1F),
                PartPose.offsetAndRotation(0F, 15F, -8F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4d",
                CubeListBuilder.create().mirror(true).texOffs(0, 78)
                        .addBox(-12F, -28F, -7F, 24F, 1F, 1F),
                PartPose.offsetAndRotation(0F, 16F, 0F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape2c",
                CubeListBuilder.create().mirror(true).texOffs(81, 0)
                        .addBox(27F, -11F, 0F, 1F, 24F, 1F),
                PartPose.offsetAndRotation(0F, 15F, 6F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape2d",
                CubeListBuilder.create().mirror(true).texOffs(82, 0)
                        .addBox(27F, -11F, 0F, 1F, 24F, 2F),
                PartPose.offsetAndRotation(0F, 15F, 1F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape2e",
                CubeListBuilder.create().mirror(true).texOffs(82, 0)
                        .addBox(27F, -11F, 0F, 1F, 24F, 2F),
                PartPose.offsetAndRotation(0F, 15F, -3F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape2f",
                CubeListBuilder.create().mirror(true).texOffs(81, 0)
                        .addBox(26.5F, -11F, 0F, 1F, 24F, 1F),
                PartPose.offsetAndRotation(0F, 15F, 6F, 0F, 0F, -0.7853982F));
        root.addOrReplaceChild("shape2g",
                CubeListBuilder.create().mirror(true).texOffs(82, 0)
                        .addBox(26.5F, -11F, 0F, 1F, 24F, 2F),
                PartPose.offsetAndRotation(0F, 15F, 1F, 0F, 0F, -0.7853982F));
        root.addOrReplaceChild("shape2h",
                CubeListBuilder.create().mirror(true).texOffs(82, 0)
                        .addBox(26.5F, -11F, 0F, 1F, 24F, 2F),
                PartPose.offsetAndRotation(0F, 15F, -3F, 0F, 0F, -0.7853982F));
        root.addOrReplaceChild("shape2i",
                CubeListBuilder.create().mirror(true).texOffs(81, 0)
                        .addBox(27F, -11F, 0F, 1F, 24F, 1F),
                PartPose.offsetAndRotation(0F, 16F, 6F, 0F, 0F, 0.7853982F));
        root.addOrReplaceChild("shape2j",
                CubeListBuilder.create().mirror(true).texOffs(82, 0)
                        .addBox(27F, -11F, 0F, 1F, 24F, 2F),
                PartPose.offsetAndRotation(0F, 16F, 1F, 0F, 0F, 0.7853982F));
        root.addOrReplaceChild("shape2k",
                CubeListBuilder.create().mirror(true).texOffs(82, 0)
                        .addBox(27F, -11F, 0F, 1F, 24F, 2F),
                PartPose.offsetAndRotation(0F, 16F, -3F, 0F, 0F, 0.7853982F));
        root.addOrReplaceChild("shape4e",
                CubeListBuilder.create().mirror(true).texOffs(0, 78)
                        .addBox(-12F, 28F, 6F, 24F, 1F, 1F),
                PartPose.offsetAndRotation(0F, 15F, 0F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4f",
                CubeListBuilder.create().mirror(true).texOffs(0, 79)
                        .addBox(-12F, 28F, 1F, 24F, 1F, 2F),
                PartPose.offsetAndRotation(0F, 15F, 0F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4g",
                CubeListBuilder.create().mirror(true).texOffs(0, 91)
                        .addBox(-12F, 28F, 1F, 24F, 1F, 2F),
                PartPose.offsetAndRotation(0F, 15F, -4F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4h",
                CubeListBuilder.create().mirror(true).texOffs(0, 78)
                        .addBox(-11F, 27.7F, 6F, 24F, 1F, 1F),
                PartPose.offsetAndRotation(0F, 15F, 0F, 0F, 0F, 0.8028515F));
        root.addOrReplaceChild("shape4i",
                CubeListBuilder.create().mirror(true).texOffs(0, 79)
                        .addBox(-11F, 27.7F, 1F, 24F, 1F, 2F),
                PartPose.offsetAndRotation(0F, 15F, 0F, 0F, 0F, 0.8028515F));
        root.addOrReplaceChild("shape4j",
                CubeListBuilder.create().mirror(true).texOffs(0, 91)
                        .addBox(-11F, 27.7F, -3F, 24F, 1F, 2F),
                PartPose.offsetAndRotation(0F, 15F, 0F, 0F, 0F, 0.8028515F));
        root.addOrReplaceChild("shape4k",
                CubeListBuilder.create().mirror(true).texOffs(0, 78)
                        .addBox(-12F, 27F, 6F, 24F, 1F, 1F),
                PartPose.offsetAndRotation(0F, 15.5F, 0F, 0F, 0F, 1.570796F));
        root.addOrReplaceChild("shape4l",
                CubeListBuilder.create().mirror(true).texOffs(0, 79)
                        .addBox(-12F, 27F, 1F, 24F, 1F, 2F),
                PartPose.offsetAndRotation(0F, 15.5F, 0F, 0F, 0F, 1.570796F));
        root.addOrReplaceChild("shape4m",
                CubeListBuilder.create().mirror(true).texOffs(0, 91)
                        .addBox(-12F, 27F, -3F, 24F, 1F, 2F),
                PartPose.offsetAndRotation(0F, 15.5F, 0F, 0F, 0F, 1.570796F));
        root.addOrReplaceChild("shape4n",
                CubeListBuilder.create().mirror(true).texOffs(0, 78)
                        .addBox(-12F, -28F, 6F, 24F, 1F, 1F),
                PartPose.offsetAndRotation(0F, 16F, 0F, 0F, 0F, -0.8028515F));
        root.addOrReplaceChild("shape4o",
                CubeListBuilder.create().mirror(true).texOffs(0, 79)
                        .addBox(-12F, -28F, 1F, 24F, 1F, 2F),
                PartPose.offsetAndRotation(0F, 16F, 0F, 0F, 0F, -0.8028515F));
        root.addOrReplaceChild("shape4p",
                CubeListBuilder.create().mirror(true).texOffs(0, 91)
                        .addBox(-12F, -28F, -3F, 24F, 1F, 2F),
                PartPose.offsetAndRotation(0F, 16F, 0F, 0F, 0F, -0.8028515F));
        root.addOrReplaceChild("shape4q",
                CubeListBuilder.create().mirror(true).texOffs(0, 78)
                        .addBox(-12F, -28F, 6F, 24F, 1F, 1F),
                PartPose.offsetAndRotation(0F, 16F, 0F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4r",
                CubeListBuilder.create().mirror(true).texOffs(0, 91)
                        .addBox(-12F, -28F, -3F, 24F, 1F, 2F),
                PartPose.offsetAndRotation(0F, 16F, 0F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4s",
                CubeListBuilder.create().mirror(true).texOffs(0, 79)
                        .addBox(-12F, -28F, 1F, 24F, 1F, 2F),
                PartPose.offsetAndRotation(0F, 16F, 0F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape1f",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(0F, 0F, 0F, 24F, 1F, 16F),
                PartPose.offsetAndRotation(-12F, -13F, -8F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape1g",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-24F, -1F, 0F, 24F, 1F, 16F),
                PartPose.offsetAndRotation(12F, -13F, -8F, 0F, 0F, -2.356194F));
        root.addOrReplaceChild("shape3b",
                CubeListBuilder.create().mirror(true).texOffs(0, 37)
                        .addBox(0F, -24F, 0F, 1F, 24F, 16F),
                PartPose.offsetAndRotation(27.7F, 28F, -8F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape1h",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-24F, 0F, 0F, 24F, 1F, 16F),
                PartPose.offsetAndRotation(12F, 45F, -8F, 0F, 0F, 2.356194F));
        root.addOrReplaceChild("shape1i",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(0F, -1F, 0F, 24F, 1F, 16F),
                PartPose.offsetAndRotation(-12F, -13F, -8F, 0F, 0F, 2.356194F));
        root.addOrReplaceChild("shape3c",
                CubeListBuilder.create().mirror(true).texOffs(0, 37)
                        .addBox(0F, -24F, 0F, 1F, 24F, 16F),
                PartPose.offsetAndRotation(-28.7F, 28F, -8F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape1j",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(0F, 0F, 0F, 24F, 1F, 16F),
                PartPose.offsetAndRotation(-12F, 44F, -8F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape1k",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(0F, 0F, 0F, 24F, 1F, 16F),
                PartPose.offsetAndRotation(-12F, 45F, -8F, 0F, 0F, -2.356194F));
        root.addOrReplaceChild("shape5",
                CubeListBuilder.create().mirror(true).texOffs(69, 18)
                        .addBox(-1.5F, -30.5F, 0F, 1F, 7F, 1F),
                PartPose.offsetAndRotation(0F, 15F, 7F, 0F, 0F, -1.972222F));
        root.addOrReplaceChild("shape5a",
                CubeListBuilder.create().mirror(true).texOffs(69, 18)
                        .addBox(-1F, -31.5F, 0F, 1F, 7F, 1F),
                PartPose.offsetAndRotation(0F, 15F, 7F, 0F, 0F, -2.75762F));
        root.addOrReplaceChild("shape5b",
                CubeListBuilder.create().mirror(true).texOffs(69, 18)
                        .addBox(0F, -31.5F, 0F, 1F, 7F, 1F),
                PartPose.offsetAndRotation(0F, 15F, 7F, 0F, 0F, 2.75762F));
        root.addOrReplaceChild("shape5d",
                CubeListBuilder.create().mirror(true).texOffs(69, 18)
                        .addBox(0.5F, -30F, 0F, 1F, 7F, 1F),
                PartPose.offsetAndRotation(0F, 15F, 7F, 0F, 0F, 1.178097F));
        root.addOrReplaceChild("shape5e",
                CubeListBuilder.create().mirror(true).texOffs(69, 18)
                        .addBox(-1F, -29.5F, 0F, 1F, 7F, 1F),
                PartPose.offsetAndRotation(0F, 15F, 7F, 0F, 0F, -0.3926991F));
        root.addOrReplaceChild("shape5f",
                CubeListBuilder.create().mirror(true).texOffs(69, 18)
                        .addBox(0F, -30.5F, 0F, 1F, 7F, 1F),
                PartPose.offsetAndRotation(0F, 15F, 7F, 0F, 0F, 1.980949F));
        root.addOrReplaceChild("shape5g",
                CubeListBuilder.create().mirror(true).texOffs(69, 18)
                        .addBox(-1.5F, -30F, 0F, 1F, 7F, 1F),
                PartPose.offsetAndRotation(0F, 15F, 7F, 0F, 0F, -1.178097F));
        root.addOrReplaceChild("shape5h",
                CubeListBuilder.create().mirror(true).texOffs(69, 18)
                        .addBox(0F, -29.5F, 0F, 1F, 7F, 1F),
                PartPose.offsetAndRotation(0F, 15F, 7F, 0F, 0F, 0.3926991F));
        root.addOrReplaceChild("shape5i",
                CubeListBuilder.create().mirror(true).texOffs(69, 18)
                        .addBox(-1F, -29.5F, 0F, 1F, 7F, 1F),
                PartPose.offsetAndRotation(0F, 15F, -8F, 0F, 0F, -0.3926991F));
        root.addOrReplaceChild("shape5j",
                CubeListBuilder.create().mirror(true).texOffs(69, 18)
                        .addBox(0F, -29.5F, 0F, 1F, 7F, 1F),
                PartPose.offsetAndRotation(0F, 15F, -8F, 0F, 0F, 0.3926991F));
        root.addOrReplaceChild("shape5k",
                CubeListBuilder.create().mirror(true).texOffs(69, 18)
                        .addBox(0.5F, -30F, 0F, 1F, 7F, 1F),
                PartPose.offsetAndRotation(0F, 15F, -8F, 0F, 0F, 1.178097F));
        root.addOrReplaceChild("shape5l",
                CubeListBuilder.create().mirror(true).texOffs(69, 18)
                        .addBox(0F, -30.5F, 0F, 1F, 7F, 1F),
                PartPose.offsetAndRotation(0F, 15F, -8F, 0F, 0F, 1.980949F));
        root.addOrReplaceChild("shape5m",
                CubeListBuilder.create().mirror(true).texOffs(69, 18)
                        .addBox(-1.5F, -30F, 0F, 1F, 7F, 1F),
                PartPose.offsetAndRotation(0F, 15F, -8F, 0F, 0F, -1.178097F));
        root.addOrReplaceChild("shape5n",
                CubeListBuilder.create().mirror(true).texOffs(69, 18)
                        .addBox(-1.5F, -30.5F, 0F, 1F, 7F, 1F),
                PartPose.offsetAndRotation(0F, 15F, -8F, 0F, 0F, -1.972222F));
        root.addOrReplaceChild("shape5o",
                CubeListBuilder.create().mirror(true).texOffs(69, 18)
                        .addBox(-1F, -31.5F, 0F, 1F, 7F, 1F),
                PartPose.offsetAndRotation(0F, 15F, -8F, 0F, 0F, -2.75762F));
        root.addOrReplaceChild("shape5p",
                CubeListBuilder.create().mirror(true).texOffs(69, 18)
                        .addBox(0F, -31.5F, 0F, 1F, 7F, 1F),
                PartPose.offsetAndRotation(0F, 15F, -8F, 0F, 0F, 2.75762F));
        root.addOrReplaceChild("shape6",
                CubeListBuilder.create().mirror(true).texOffs(0, 95)
                        .addBox(7.5F, 0F, 0F, 5F, 1F, 12F),
                PartPose.offsetAndRotation(-10F, 45F, -6F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape6a",
                CubeListBuilder.create().mirror(true).texOffs(0, 109)
                        .addBox(0F, 0F, 0F, 5F, 1F, 12F),
                PartPose.offsetAndRotation(5F, 45F, -6F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape6b",
                CubeListBuilder.create().mirror(true).texOffs(0, 109)
                        .addBox(0F, 0F, 0F, 5F, 1F, 12F),
                PartPose.offsetAndRotation(-10F, 45F, -6F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape6c",
                CubeListBuilder.create().mirror(true).texOffs(0, 95)
                        .addBox(0F, 0F, 0F, 5F, 1F, 12F),
                PartPose.offsetAndRotation(-10F, -14F, -6F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape6d",
                CubeListBuilder.create().mirror(true).texOffs(0, 95)
                        .addBox(0F, 0F, 0F, 5F, 1F, 12F),
                PartPose.offsetAndRotation(-10F, -14F, -6F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape6e",
                CubeListBuilder.create().mirror(true).texOffs(0, 109)
                        .addBox(7.5F, 0F, 0F, 5F, 1F, 12F),
                PartPose.offsetAndRotation(-10F, -14F, -6F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape6f",
                CubeListBuilder.create().mirror(true).texOffs(0, 95)
                        .addBox(0F, 0F, 0F, 5F, 1F, 12F),
                PartPose.offsetAndRotation(5F, -14F, -6F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape7",
                CubeListBuilder.create().mirror(true).texOffs(35, 95)
                        .addBox(0F, 0F, 0F, 1F, 5F, 12F),
                PartPose.offsetAndRotation(-29.5F, 21F, -6F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape7a",
                CubeListBuilder.create().mirror(true).texOffs(62, 95)
                        .addBox(0F, 0F, 0F, 1F, 5F, 12F),
                PartPose.offsetAndRotation(-29.5F, 13.5F, -6F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape7b",
                CubeListBuilder.create().mirror(true).texOffs(35, 95)
                        .addBox(0F, 0F, 0F, 1F, 5F, 12F),
                PartPose.offsetAndRotation(28.5F, 13.5F, -6F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape7c",
                CubeListBuilder.create().mirror(true).texOffs(35, 95)
                        .addBox(0F, 0F, 0F, 1F, 5F, 12F),
                PartPose.offsetAndRotation(-29.5F, 6F, -6F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape7d",
                CubeListBuilder.create().mirror(true).texOffs(62, 95)
                        .addBox(0F, 0F, 0F, 1F, 5F, 12F),
                PartPose.offsetAndRotation(28.5F, 6F, -6F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape7e",
                CubeListBuilder.create().mirror(true).texOffs(62, 95)
                        .addBox(0F, 0F, 0F, 1F, 5F, 12F),
                PartPose.offsetAndRotation(28.5F, 21F, -6F, 0F, 0F, 0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    public void renderAll(PoseStack stack, VertexConsumer vc, int light) {
        shape1.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape1a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape1b.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape1c.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape1d.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape1e.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2b.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4b.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4c.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4d.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2c.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2d.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2e.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2f.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2g.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2h.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2i.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2j.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape2k.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
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
        shape1f.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape1g.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3b.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape1h.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape1i.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3c.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape1j.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape1k.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape5.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape5a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape5b.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape5d.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape5e.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape5f.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape5g.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape5h.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape5i.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape5j.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape5k.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape5l.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape5m.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape5n.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape5o.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape5p.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape6.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape6a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape6b.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape6c.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape6d.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape6e.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape6f.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape7.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape7a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape7b.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape7c.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape7d.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape7e.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
    }
}
