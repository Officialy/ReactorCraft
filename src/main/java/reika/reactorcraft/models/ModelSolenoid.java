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

/** 26.2 port of the Techne model ModelSolenoid (auto-converted by scripts/convert_reactor_model.py). */
public class ModelSolenoid {

    private final ModelPart shape3;
    private final ModelPart shape1a;
    private final ModelPart shape1b;
    private final ModelPart shape1c;
    private final ModelPart shape1d;
    private final ModelPart shape1e;
    private final ModelPart shape1f;
    private final ModelPart shape1g;
    private final ModelPart shape1h;
    private final ModelPart shape1i;
    private final ModelPart shape1j;
    private final ModelPart shape1k;
    private final ModelPart shape1l;
    private final ModelPart shape4;
    private final ModelPart shape1n;
    private final ModelPart shape1o;
    private final ModelPart shape2;
    private final ModelPart shape2a;
    private final ModelPart shape2b;
    private final ModelPart shape2d;
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
    private final ModelPart shape2q;
    private final ModelPart shape2r;
    private final ModelPart shape2s;
    private final ModelPart shape2t;
    private final ModelPart shape2u;
    private final ModelPart shape2v;
    private final ModelPart shape2w;
    private final ModelPart shape2x;
    private final ModelPart shape2y;
    private final ModelPart shape2z;
    private final ModelPart shape2aa;
    private final ModelPart shape2ab;
    private final ModelPart shape2ac;
    private final ModelPart shape2ad;
    private final ModelPart shape2ae;
    private final ModelPart shape2af;
    private final ModelPart shape2ag;
    private final ModelPart shape2ah;
    private final ModelPart shape2ai;
    private final ModelPart shape2aj;
    private final ModelPart shape2ak;
    private final ModelPart shape2al;
    private final ModelPart shape2am;
    private final ModelPart shape2an;
    private final ModelPart shape2ao;
    private final ModelPart shape2ap;
    private final ModelPart shape2aq;
    private final ModelPart shape2ar;
    private final ModelPart shape2as;
    private final ModelPart shape2at;
    private final ModelPart shape2au;
    private final ModelPart shape2av;
    private final ModelPart shape2aw;
    private final ModelPart shape2ax;
    private final ModelPart shape1z;
    private final ModelPart shape3a;
    private final ModelPart shape3b;
    private final ModelPart shape3c;
    private final ModelPart shape3d;
    private final ModelPart shape3e;
    private final ModelPart shape3f;
    private final ModelPart shape3g;
    private final ModelPart shape1m;
    private final ModelPart shape4a;
    private final ModelPart shape4b;
    private final ModelPart shape4c;
    private final ModelPart shape4d;
    private final ModelPart shape4e;
    private final ModelPart shape4f;
    private final ModelPart shape4g;
    private final ModelPart shape4h;
    private final ModelPart shape3h;
    private final ModelPart shape4i;
    private final ModelPart shape3i;
    private final ModelPart shape4j;
    private final ModelPart shape3j;
    private final ModelPart shape4k;
    private final ModelPart shape3k;
    private final ModelPart shape4l;
    private final ModelPart shape3l;
    private final ModelPart shape4m;
    private final ModelPart shape3m;
    private final ModelPart shape4n;
    private final ModelPart shape3n;
    private final ModelPart shape4o;
    private final ModelPart shape3o;
    private final ModelPart shape6g;
    private final ModelPart shape7;
    private final ModelPart shape7a;
    private final ModelPart shape7b;
    private final ModelPart shape7c;
    private final ModelPart shape6f;
    private final ModelPart shape6a;
    private final ModelPart shape6b;
    private final ModelPart shape6;
    private final ModelPart shape6c;
    private final ModelPart shape6d;
    private final ModelPart shape6e;
    private final ModelPart shape8;
    private final ModelPart shape8a;
    private final ModelPart shape8b;
    private final ModelPart shape8c;

    public ModelSolenoid(ModelPart root) {
        this.shape3 = root.getChild("shape3");
        this.shape1a = root.getChild("shape1a");
        this.shape1b = root.getChild("shape1b");
        this.shape1c = root.getChild("shape1c");
        this.shape1d = root.getChild("shape1d");
        this.shape1e = root.getChild("shape1e");
        this.shape1f = root.getChild("shape1f");
        this.shape1g = root.getChild("shape1g");
        this.shape1h = root.getChild("shape1h");
        this.shape1i = root.getChild("shape1i");
        this.shape1j = root.getChild("shape1j");
        this.shape1k = root.getChild("shape1k");
        this.shape1l = root.getChild("shape1l");
        this.shape4 = root.getChild("shape4");
        this.shape1n = root.getChild("shape1n");
        this.shape1o = root.getChild("shape1o");
        this.shape2 = root.getChild("shape2");
        this.shape2a = root.getChild("shape2a");
        this.shape2b = root.getChild("shape2b");
        this.shape2d = root.getChild("shape2d");
        this.shape2g = root.getChild("shape2g");
        this.shape2h = root.getChild("shape2h");
        this.shape2i = root.getChild("shape2i");
        this.shape2j = root.getChild("shape2j");
        this.shape2k = root.getChild("shape2k");
        this.shape2l = root.getChild("shape2l");
        this.shape2m = root.getChild("shape2m");
        this.shape2n = root.getChild("shape2n");
        this.shape2o = root.getChild("shape2o");
        this.shape2p = root.getChild("shape2p");
        this.shape2q = root.getChild("shape2q");
        this.shape2r = root.getChild("shape2r");
        this.shape2s = root.getChild("shape2s");
        this.shape2t = root.getChild("shape2t");
        this.shape2u = root.getChild("shape2u");
        this.shape2v = root.getChild("shape2v");
        this.shape2w = root.getChild("shape2w");
        this.shape2x = root.getChild("shape2x");
        this.shape2y = root.getChild("shape2y");
        this.shape2z = root.getChild("shape2z");
        this.shape2aa = root.getChild("shape2aa");
        this.shape2ab = root.getChild("shape2ab");
        this.shape2ac = root.getChild("shape2ac");
        this.shape2ad = root.getChild("shape2ad");
        this.shape2ae = root.getChild("shape2ae");
        this.shape2af = root.getChild("shape2af");
        this.shape2ag = root.getChild("shape2ag");
        this.shape2ah = root.getChild("shape2ah");
        this.shape2ai = root.getChild("shape2ai");
        this.shape2aj = root.getChild("shape2aj");
        this.shape2ak = root.getChild("shape2ak");
        this.shape2al = root.getChild("shape2al");
        this.shape2am = root.getChild("shape2am");
        this.shape2an = root.getChild("shape2an");
        this.shape2ao = root.getChild("shape2ao");
        this.shape2ap = root.getChild("shape2ap");
        this.shape2aq = root.getChild("shape2aq");
        this.shape2ar = root.getChild("shape2ar");
        this.shape2as = root.getChild("shape2as");
        this.shape2at = root.getChild("shape2at");
        this.shape2au = root.getChild("shape2au");
        this.shape2av = root.getChild("shape2av");
        this.shape2aw = root.getChild("shape2aw");
        this.shape2ax = root.getChild("shape2ax");
        this.shape1z = root.getChild("shape1z");
        this.shape3a = root.getChild("shape3a");
        this.shape3b = root.getChild("shape3b");
        this.shape3c = root.getChild("shape3c");
        this.shape3d = root.getChild("shape3d");
        this.shape3e = root.getChild("shape3e");
        this.shape3f = root.getChild("shape3f");
        this.shape3g = root.getChild("shape3g");
        this.shape1m = root.getChild("shape1m");
        this.shape4a = root.getChild("shape4a");
        this.shape4b = root.getChild("shape4b");
        this.shape4c = root.getChild("shape4c");
        this.shape4d = root.getChild("shape4d");
        this.shape4e = root.getChild("shape4e");
        this.shape4f = root.getChild("shape4f");
        this.shape4g = root.getChild("shape4g");
        this.shape4h = root.getChild("shape4h");
        this.shape3h = root.getChild("shape3h");
        this.shape4i = root.getChild("shape4i");
        this.shape3i = root.getChild("shape3i");
        this.shape4j = root.getChild("shape4j");
        this.shape3j = root.getChild("shape3j");
        this.shape4k = root.getChild("shape4k");
        this.shape3k = root.getChild("shape3k");
        this.shape4l = root.getChild("shape4l");
        this.shape3l = root.getChild("shape3l");
        this.shape4m = root.getChild("shape4m");
        this.shape3m = root.getChild("shape3m");
        this.shape4n = root.getChild("shape4n");
        this.shape3n = root.getChild("shape3n");
        this.shape4o = root.getChild("shape4o");
        this.shape3o = root.getChild("shape3o");
        this.shape6g = root.getChild("shape6g");
        this.shape7 = root.getChild("shape7");
        this.shape7a = root.getChild("shape7a");
        this.shape7b = root.getChild("shape7b");
        this.shape7c = root.getChild("shape7c");
        this.shape6f = root.getChild("shape6f");
        this.shape6a = root.getChild("shape6a");
        this.shape6b = root.getChild("shape6b");
        this.shape6 = root.getChild("shape6");
        this.shape6c = root.getChild("shape6c");
        this.shape6d = root.getChild("shape6d");
        this.shape6e = root.getChild("shape6e");
        this.shape8 = root.getChild("shape8");
        this.shape8a = root.getChild("shape8a");
        this.shape8b = root.getChild("shape8b");
        this.shape8c = root.getChild("shape8c");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("shape3",
                CubeListBuilder.create().mirror(true).texOffs(0, 18)
                        .addBox(-27F, 0F, -128.5F, 52F, 16F, 1F),
                PartPose.offsetAndRotation(0F, 24F, 0F, 0F, -2.391101F, 0F));
        root.addOrReplaceChild("shape1a",
                CubeListBuilder.create().mirror(true).texOffs(0, 18)
                        .addBox(-27F, 0F, -128.5F, 52F, 16F, 1F),
                PartPose.offsetAndRotation(0F, 8F, 0F, 0F, -2.391101F, 0F));
        root.addOrReplaceChild("shape1b",
                CubeListBuilder.create().mirror(true).texOffs(0, 18)
                        .addBox(-26F, 0F, -128.5F, 52F, 16F, 1F),
                PartPose.offsetAndRotation(0F, 8F, 0F, 0F, -2.7838F, 0F));
        root.addOrReplaceChild("shape1c",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-25F, 0F, -128.5F, 50F, 16F, 1F),
                PartPose.offsetAndRotation(0F, 8F, 0F, 0F, 1.570796F, 0F));
        root.addOrReplaceChild("shape1d",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-25F, 0F, -128.5F, 50F, 16F, 1F),
                PartPose.offsetAndRotation(0F, 8F, 0F, 0F, 2.356194F, 0F));
        root.addOrReplaceChild("shape1e",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-25F, 0F, -128.5F, 50F, 16F, 1F),
                PartPose.offsetAndRotation(0F, 8F, 0F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape1f",
                CubeListBuilder.create().mirror(true).texOffs(0, 18)
                        .addBox(-25F, 0F, -128.5F, 52F, 16F, 1F),
                PartPose.offsetAndRotation(0F, 8F, 0F, 0F, -0.7853982F, 0F));
        root.addOrReplaceChild("shape1g",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-25F, 0F, -128.5F, 50F, 16F, 1F),
                PartPose.offsetAndRotation(0F, 8F, 0F, 0F, -1.58825F, 0F));
        root.addOrReplaceChild("shape1h",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-25F, 0F, -128.5F, 50F, 16F, 1F),
                PartPose.offsetAndRotation(0F, 8F, 0F, 0F, 0.7853982F, 0F));
        root.addOrReplaceChild("shape1i",
                CubeListBuilder.create().mirror(true).texOffs(0, 18)
                        .addBox(-26F, 0F, -128.5F, 52F, 16F, 1F),
                PartPose.offsetAndRotation(0F, 8F, 0F, 0F, 0.3926991F, 0F));
        root.addOrReplaceChild("shape1j",
                CubeListBuilder.create().mirror(true).texOffs(0, 18)
                        .addBox(-26F, 0F, -128.5F, 52F, 16F, 1F),
                PartPose.offsetAndRotation(0F, 8F, 0F, 0F, 1.178097F, 0F));
        root.addOrReplaceChild("shape1k",
                CubeListBuilder.create().mirror(true).texOffs(0, 18)
                        .addBox(-26F, 0F, -128.5F, 52F, 16F, 1F),
                PartPose.offsetAndRotation(0F, 8F, 0F, 0F, 1.963495F, 0F));
        root.addOrReplaceChild("shape1l",
                CubeListBuilder.create().mirror(true).texOffs(0, 18)
                        .addBox(-26F, 0F, -128.5F, 52F, 16F, 1F),
                PartPose.offsetAndRotation(0F, 8F, 0F, 0F, 2.748893F, 0F));
        root.addOrReplaceChild("shape4",
                CubeListBuilder.create().mirror(true).texOffs(0, 18)
                        .addBox(-26F, 0F, -128.5F, 52F, 16F, 1F),
                PartPose.offsetAndRotation(0F, 24F, 0F, 0F, -1.980949F, 0F));
        root.addOrReplaceChild("shape1n",
                CubeListBuilder.create().mirror(true).texOffs(0, 18)
                        .addBox(-26F, 0F, -128.5F, 52F, 16F, 1F),
                PartPose.offsetAndRotation(0F, 8F, 0F, 0F, -1.19555F, 0F));
        root.addOrReplaceChild("shape1o",
                CubeListBuilder.create().mirror(true).texOffs(0, 18)
                        .addBox(-26F, 0F, -128.5F, 52F, 16F, 1F),
                PartPose.offsetAndRotation(0F, 8F, 0F, 0F, -1.980949F, 0F));
        root.addOrReplaceChild("shape2",
                CubeListBuilder.create().mirror(true).texOffs(0, 44)
                        .addBox(-26F, 0F, -129.5F, 52F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 19F, 0F, 0F, -2.7838F, 0F));
        root.addOrReplaceChild("shape2a",
                CubeListBuilder.create().mirror(true).texOffs(0, 37)
                        .addBox(-26F, 0F, -129.5F, 52F, 1F, 1F),
                PartPose.offsetAndRotation(0F, 22F, 0F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape2b",
                CubeListBuilder.create().mirror(true).texOffs(0, 40)
                        .addBox(-26F, 0F, -129.5F, 52F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 11F, 0F, 0F, -2.7838F, 0F));
        root.addOrReplaceChild("shape2d",
                CubeListBuilder.create().mirror(true).texOffs(0, 37)
                        .addBox(-26F, 0F, -129.5F, 52F, 1F, 1F),
                PartPose.offsetAndRotation(0F, 22F, 0F, 0F, 3.141593F, 0F));
        root.addOrReplaceChild("shape2g",
                CubeListBuilder.create().mirror(true).texOffs(0, 37)
                        .addBox(-26F, 0F, -129.5F, 52F, 1F, 1F),
                PartPose.offsetAndRotation(0F, 22F, 0F, 0F, 0.3926991F, 0F));
        root.addOrReplaceChild("shape2h",
                CubeListBuilder.create().mirror(true).texOffs(0, 37)
                        .addBox(-26F, 0F, -129.5F, 52F, 1F, 1F),
                PartPose.offsetAndRotation(0F, 22F, 0F, 0F, 0.7853982F, 0F));
        root.addOrReplaceChild("shape2i",
                CubeListBuilder.create().mirror(true).texOffs(0, 37)
                        .addBox(-26F, 0F, -129.5F, 52F, 1F, 1F),
                PartPose.offsetAndRotation(0F, 22F, 0F, 0F, 1.178097F, 0F));
        root.addOrReplaceChild("shape2j",
                CubeListBuilder.create().mirror(true).texOffs(0, 37)
                        .addBox(-26F, 0F, -129.5F, 52F, 1F, 1F),
                PartPose.offsetAndRotation(0F, 22F, 0F, 0F, 1.570796F, 0F));
        root.addOrReplaceChild("shape2k",
                CubeListBuilder.create().mirror(true).texOffs(0, 37)
                        .addBox(-26F, 0F, -129.5F, 52F, 1F, 1F),
                PartPose.offsetAndRotation(0F, 22F, 0F, 0F, 1.963495F, 0F));
        root.addOrReplaceChild("shape2l",
                CubeListBuilder.create().mirror(true).texOffs(0, 37)
                        .addBox(-26F, 0F, -129.5F, 52F, 1F, 1F),
                PartPose.offsetAndRotation(0F, 22F, 0F, 0F, 2.356194F, 0F));
        root.addOrReplaceChild("shape2m",
                CubeListBuilder.create().mirror(true).texOffs(0, 37)
                        .addBox(-26F, 0F, -129.5F, 52F, 1F, 1F),
                PartPose.offsetAndRotation(0F, 22F, 0F, 0F, 2.748893F, 0F));
        root.addOrReplaceChild("shape2n",
                CubeListBuilder.create().mirror(true).texOffs(0, 37)
                        .addBox(-26F, 0F, -129.5F, 52F, 1F, 1F),
                PartPose.offsetAndRotation(0F, 22F, 0F, 0F, -0.3926991F, 0F));
        root.addOrReplaceChild("shape2o",
                CubeListBuilder.create().mirror(true).texOffs(0, 37)
                        .addBox(-25.5F, 0F, -129.5F, 53F, 1F, 1F),
                PartPose.offsetAndRotation(0F, 22F, 0F, 0F, -0.7853982F, 0F));
        root.addOrReplaceChild("shape2p",
                CubeListBuilder.create().mirror(true).texOffs(0, 37)
                        .addBox(-26F, 0F, -129.5F, 52F, 1F, 1F),
                PartPose.offsetAndRotation(0F, 22F, 0F, 0F, -1.19555F, 0F));
        root.addOrReplaceChild("shape2q",
                CubeListBuilder.create().mirror(true).texOffs(0, 37)
                        .addBox(-26F, 0F, -129.5F, 52F, 1F, 1F),
                PartPose.offsetAndRotation(0F, 22F, 0F, 0F, -1.58825F, 0F));
        root.addOrReplaceChild("shape2r",
                CubeListBuilder.create().mirror(true).texOffs(0, 37)
                        .addBox(-26F, 0F, -129.5F, 52F, 1F, 1F),
                PartPose.offsetAndRotation(0F, 22F, 0F, 0F, -1.980949F, 0F));
        root.addOrReplaceChild("shape2s",
                CubeListBuilder.create().mirror(true).texOffs(0, 37)
                        .addBox(-27.5F, 0F, -129.5F, 53F, 1F, 1F),
                PartPose.offsetAndRotation(0F, 22F, 0F, 0F, -2.391101F, 0F));
        root.addOrReplaceChild("shape2t",
                CubeListBuilder.create().mirror(true).texOffs(0, 37)
                        .addBox(-26F, 0F, -129.5F, 52F, 1F, 1F),
                PartPose.offsetAndRotation(0F, 22F, 0F, 0F, -2.7838F, 0F));
        root.addOrReplaceChild("shape2u",
                CubeListBuilder.create().mirror(true).texOffs(0, 40)
                        .addBox(-26F, 0F, -129.5F, 52F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 11F, 0F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape2v",
                CubeListBuilder.create().mirror(true).texOffs(0, 40)
                        .addBox(-26F, 0F, -129.5F, 52F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 11F, 0F, 0F, 0.3926991F, 0F));
        root.addOrReplaceChild("shape2w",
                CubeListBuilder.create().mirror(true).texOffs(0, 40)
                        .addBox(-26F, 0F, -129.5F, 52F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 11F, 0F, 0F, 0.7853982F, 0F));
        root.addOrReplaceChild("shape2x",
                CubeListBuilder.create().mirror(true).texOffs(0, 40)
                        .addBox(-26F, 0F, -129.5F, 52F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 11F, 0F, 0F, 1.178097F, 0F));
        root.addOrReplaceChild("shape2y",
                CubeListBuilder.create().mirror(true).texOffs(0, 40)
                        .addBox(-26F, 0F, -129.5F, 52F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 11F, 0F, 0F, 1.570796F, 0F));
        root.addOrReplaceChild("shape2z",
                CubeListBuilder.create().mirror(true).texOffs(0, 40)
                        .addBox(-26F, 0F, -129.5F, 52F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 11F, 0F, 0F, 1.963495F, 0F));
        root.addOrReplaceChild("shape2aa",
                CubeListBuilder.create().mirror(true).texOffs(0, 40)
                        .addBox(-26F, 0F, -129.5F, 52F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 11F, 0F, 0F, 2.356194F, 0F));
        root.addOrReplaceChild("shape2ab",
                CubeListBuilder.create().mirror(true).texOffs(0, 40)
                        .addBox(-26F, 0F, -129.5F, 52F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 11F, 0F, 0F, 2.748893F, 0F));
        root.addOrReplaceChild("shape2ac",
                CubeListBuilder.create().mirror(true).texOffs(0, 40)
                        .addBox(-26F, 0F, -129.5F, 52F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 11F, 0F, 0F, 3.141593F, 0F));
        root.addOrReplaceChild("shape2ad",
                CubeListBuilder.create().mirror(true).texOffs(0, 40)
                        .addBox(-26F, 0F, -129.5F, 52F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 11F, 0F, 0F, -0.3926991F, 0F));
        root.addOrReplaceChild("shape2ae",
                CubeListBuilder.create().mirror(true).texOffs(0, 40)
                        .addBox(-25.5F, 0F, -129.5F, 53F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 11F, 0F, 0F, -0.7853982F, 0F));
        root.addOrReplaceChild("shape2af",
                CubeListBuilder.create().mirror(true).texOffs(0, 40)
                        .addBox(-26F, 0F, -129.5F, 52F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 11F, 0F, 0F, -1.19555F, 0F));
        root.addOrReplaceChild("shape2ag",
                CubeListBuilder.create().mirror(true).texOffs(0, 40)
                        .addBox(-26F, 0F, -129.5F, 52F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 11F, 0F, 0F, -1.58825F, 0F));
        root.addOrReplaceChild("shape2ah",
                CubeListBuilder.create().mirror(true).texOffs(0, 40)
                        .addBox(-26F, 0F, -129.5F, 52F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 11F, 0F, 0F, -1.980949F, 0F));
        root.addOrReplaceChild("shape2ai",
                CubeListBuilder.create().mirror(true).texOffs(0, 40)
                        .addBox(-27.5F, 0F, -129.5F, 53F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 11F, 0F, 0F, -2.391101F, 0F));
        root.addOrReplaceChild("shape2aj",
                CubeListBuilder.create().mirror(true).texOffs(0, 44)
                        .addBox(-26F, 0F, -129.5F, 52F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 19F, 0F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape2ak",
                CubeListBuilder.create().mirror(true).texOffs(0, 44)
                        .addBox(-26F, 0F, -129.5F, 52F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 19F, 0F, 0F, 0.3926991F, 0F));
        root.addOrReplaceChild("shape2al",
                CubeListBuilder.create().mirror(true).texOffs(0, 44)
                        .addBox(-26F, 0F, -129.5F, 52F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 19F, 0F, 0F, 0.7853982F, 0F));
        root.addOrReplaceChild("shape2am",
                CubeListBuilder.create().mirror(true).texOffs(0, 44)
                        .addBox(-26F, 0F, -129.5F, 52F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 19F, 0F, 0F, 1.178097F, 0F));
        root.addOrReplaceChild("shape2an",
                CubeListBuilder.create().mirror(true).texOffs(0, 44)
                        .addBox(-26F, 0F, -129.5F, 52F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 19F, 0F, 0F, 1.570796F, 0F));
        root.addOrReplaceChild("shape2ao",
                CubeListBuilder.create().mirror(true).texOffs(0, 44)
                        .addBox(-26F, 0F, -129.5F, 52F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 19F, 0F, 0F, 1.963495F, 0F));
        root.addOrReplaceChild("shape2ap",
                CubeListBuilder.create().mirror(true).texOffs(0, 44)
                        .addBox(-26F, 0F, -129.5F, 52F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 19F, 0F, 0F, 2.356194F, 0F));
        root.addOrReplaceChild("shape2aq",
                CubeListBuilder.create().mirror(true).texOffs(0, 44)
                        .addBox(-26F, 0F, -129.5F, 52F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 19F, 0F, 0F, 2.748893F, 0F));
        root.addOrReplaceChild("shape2ar",
                CubeListBuilder.create().mirror(true).texOffs(0, 44)
                        .addBox(-26F, 0F, -129.5F, 52F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 19F, 0F, 0F, 3.141593F, 0F));
        root.addOrReplaceChild("shape2as",
                CubeListBuilder.create().mirror(true).texOffs(0, 44)
                        .addBox(-26F, 0F, -129.5F, 52F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 19F, 0F, 0F, -0.3926991F, 0F));
        root.addOrReplaceChild("shape2at",
                CubeListBuilder.create().mirror(true).texOffs(0, 44)
                        .addBox(-25.5F, 0F, -129.5F, 53F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 19F, 0F, 0F, -0.7853982F, 0F));
        root.addOrReplaceChild("shape2au",
                CubeListBuilder.create().mirror(true).texOffs(0, 44)
                        .addBox(-26F, 0F, -129.5F, 52F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 19F, 0F, 0F, -1.19555F, 0F));
        root.addOrReplaceChild("shape2av",
                CubeListBuilder.create().mirror(true).texOffs(0, 44)
                        .addBox(-26F, 0F, -129.5F, 52F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 19F, 0F, 0F, -1.58825F, 0F));
        root.addOrReplaceChild("shape2aw",
                CubeListBuilder.create().mirror(true).texOffs(0, 44)
                        .addBox(-27.5F, 0F, -129.5F, 53F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 19F, 0F, 0F, -2.391101F, 0F));
        root.addOrReplaceChild("shape2ax",
                CubeListBuilder.create().mirror(true).texOffs(0, 44)
                        .addBox(-26F, 0F, -129.5F, 52F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 19F, 0F, 0F, -1.980949F, 0F));
        root.addOrReplaceChild("shape1z",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-25F, 0F, -128.5F, 50F, 16F, 1F),
                PartPose.offsetAndRotation(0F, 8F, 0F, 0F, 3.141593F, 0F));
        root.addOrReplaceChild("shape3a",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-25F, 0F, -128.5F, 50F, 16F, 1F),
                PartPose.offsetAndRotation(0F, 24F, 0F, 0F, 3.141593F, 0F));
        root.addOrReplaceChild("shape3b",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-25F, 0F, -128.5F, 50F, 16F, 1F),
                PartPose.offsetAndRotation(0F, 24F, 0F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape3c",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-25F, 0F, -128.5F, 50F, 16F, 1F),
                PartPose.offsetAndRotation(0F, 24F, 0F, 0F, 0.7853982F, 0F));
        root.addOrReplaceChild("shape3d",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-25F, 0F, -128.5F, 50F, 16F, 1F),
                PartPose.offsetAndRotation(0F, 24F, 0F, 0F, 1.570796F, 0F));
        root.addOrReplaceChild("shape3e",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-25F, 0F, -128.5F, 50F, 16F, 1F),
                PartPose.offsetAndRotation(0F, 24F, 0F, 0F, 2.356194F, 0F));
        root.addOrReplaceChild("shape3f",
                CubeListBuilder.create().mirror(true).texOffs(0, 18)
                        .addBox(-25F, 0F, -128.5F, 52F, 16F, 1F),
                PartPose.offsetAndRotation(0F, 24F, 0F, 0F, -0.7853982F, 0F));
        root.addOrReplaceChild("shape3g",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-25F, 0F, -128.5F, 50F, 16F, 1F),
                PartPose.offsetAndRotation(0F, 24F, 0F, 0F, -1.58825F, 0F));
        root.addOrReplaceChild("shape1m",
                CubeListBuilder.create().mirror(true).texOffs(0, 18)
                        .addBox(-26F, 0F, -128.5F, 52F, 16F, 1F),
                PartPose.offsetAndRotation(0F, 8F, 0F, 0F, -0.3926991F, 0F));
        root.addOrReplaceChild("shape4a",
                CubeListBuilder.create().mirror(true).texOffs(0, 18)
                        .addBox(-26F, 0F, -128.5F, 52F, 16F, 1F),
                PartPose.offsetAndRotation(0F, 24F, 0F, 0F, -0.3926991F, 0F));
        root.addOrReplaceChild("shape4b",
                CubeListBuilder.create().mirror(true).texOffs(0, 18)
                        .addBox(-26F, 0F, -128.5F, 52F, 16F, 1F),
                PartPose.offsetAndRotation(0F, 24F, 0F, 0F, -1.19555F, 0F));
        root.addOrReplaceChild("shape4c",
                CubeListBuilder.create().mirror(true).texOffs(0, 18)
                        .addBox(-26F, 0F, -128.5F, 52F, 16F, 1F),
                PartPose.offsetAndRotation(0F, 24F, 0F, 0F, 0.3926991F, 0F));
        root.addOrReplaceChild("shape4d",
                CubeListBuilder.create().mirror(true).texOffs(0, 18)
                        .addBox(-26F, 0F, -128.5F, 52F, 16F, 1F),
                PartPose.offsetAndRotation(0F, 24F, 0F, 0F, 1.178097F, 0F));
        root.addOrReplaceChild("shape4e",
                CubeListBuilder.create().mirror(true).texOffs(0, 18)
                        .addBox(-26F, 0F, -128.5F, 52F, 16F, 1F),
                PartPose.offsetAndRotation(0F, 24F, 0F, 0F, 1.963495F, 0F));
        root.addOrReplaceChild("shape4f",
                CubeListBuilder.create().mirror(true).texOffs(0, 18)
                        .addBox(-26F, 0F, -128.5F, 52F, 16F, 1F),
                PartPose.offsetAndRotation(0F, 24F, 0F, 0F, 2.748893F, 0F));
        root.addOrReplaceChild("shape4g",
                CubeListBuilder.create().mirror(true).texOffs(0, 18)
                        .addBox(-26F, 0F, -128.5F, 52F, 16F, 1F),
                PartPose.offsetAndRotation(0F, 24F, 0F, 0F, -2.7838F, 0F));
        root.addOrReplaceChild("shape4h",
                CubeListBuilder.create().mirror(true).texOffs(0, 18)
                        .addBox(-26F, 0F, -128.5F, 52F, 16F, 1F),
                PartPose.offsetAndRotation(0F, -8F, 0F, 0F, 0.3926991F, 0F));
        root.addOrReplaceChild("shape3h",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-25F, 0F, -128.5F, 50F, 16F, 1F),
                PartPose.offsetAndRotation(0F, -8F, 0F, 0F, 0.7853982F, 0F));
        root.addOrReplaceChild("shape4i",
                CubeListBuilder.create().mirror(true).texOffs(0, 18)
                        .addBox(-26F, 0F, -128.5F, 52F, 16F, 1F),
                PartPose.offsetAndRotation(0F, -8F, 0F, 0F, 1.178097F, 0F));
        root.addOrReplaceChild("shape3i",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-25F, 0F, -128.5F, 50F, 16F, 1F),
                PartPose.offsetAndRotation(0F, -8F, 0F, 0F, 1.570796F, 0F));
        root.addOrReplaceChild("shape4j",
                CubeListBuilder.create().mirror(true).texOffs(0, 18)
                        .addBox(-26F, 0F, -128.5F, 52F, 16F, 1F),
                PartPose.offsetAndRotation(0F, -8F, 0F, 0F, 1.963495F, 0F));
        root.addOrReplaceChild("shape3j",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-25F, 0F, -128.5F, 50F, 16F, 1F),
                PartPose.offsetAndRotation(0F, -8F, 0F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape4k",
                CubeListBuilder.create().mirror(true).texOffs(0, 18)
                        .addBox(-26F, 0F, -128.5F, 52F, 16F, 1F),
                PartPose.offsetAndRotation(0F, -8F, 0F, 0F, -0.3926991F, 0F));
        root.addOrReplaceChild("shape3k",
                CubeListBuilder.create().mirror(true).texOffs(0, 18)
                        .addBox(-25F, 0F, -128.5F, 52F, 16F, 1F),
                PartPose.offsetAndRotation(0F, -8F, 0F, 0F, -0.7853982F, 0F));
        root.addOrReplaceChild("shape4l",
                CubeListBuilder.create().mirror(true).texOffs(0, 18)
                        .addBox(-26F, 0F, -128.5F, 52F, 16F, 1F),
                PartPose.offsetAndRotation(0F, -8F, 0F, 0F, -1.19555F, 0F));
        root.addOrReplaceChild("shape3l",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-25F, 0F, -128.5F, 50F, 16F, 1F),
                PartPose.offsetAndRotation(0F, -8F, 0F, 0F, -1.58825F, 0F));
        root.addOrReplaceChild("shape4m",
                CubeListBuilder.create().mirror(true).texOffs(0, 18)
                        .addBox(-26F, 0F, -128.5F, 52F, 16F, 1F),
                PartPose.offsetAndRotation(0F, -8F, 0F, 0F, -1.980949F, 0F));
        root.addOrReplaceChild("shape3m",
                CubeListBuilder.create().mirror(true).texOffs(0, 18)
                        .addBox(-27F, 0F, -128.5F, 52F, 16F, 1F),
                PartPose.offsetAndRotation(0F, -8F, 0F, 0F, -2.391101F, 0F));
        root.addOrReplaceChild("shape4n",
                CubeListBuilder.create().mirror(true).texOffs(0, 18)
                        .addBox(-26F, 0F, -128.5F, 52F, 16F, 1F),
                PartPose.offsetAndRotation(0F, -8F, 0F, 0F, -2.7838F, 0F));
        root.addOrReplaceChild("shape3n",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-25F, 0F, -128.5F, 50F, 16F, 1F),
                PartPose.offsetAndRotation(0F, -8F, 0F, 0F, 3.141593F, 0F));
        root.addOrReplaceChild("shape4o",
                CubeListBuilder.create().mirror(true).texOffs(0, 18)
                        .addBox(-26F, 0F, -128.5F, 52F, 16F, 1F),
                PartPose.offsetAndRotation(0F, -8F, 0F, 0F, 2.748893F, 0F));
        root.addOrReplaceChild("shape3o",
                CubeListBuilder.create().mirror(true).texOffs(0, 0)
                        .addBox(-25F, 0F, -128.5F, 50F, 16F, 1F),
                PartPose.offsetAndRotation(0F, -8F, 0F, 0F, 2.356194F, 0F));
        root.addOrReplaceChild("shape6g",
                CubeListBuilder.create().mirror(true).texOffs(0, 125)
                        .addBox(-2F, -7F, 12.5F, 4F, 14F, 115F),
                PartPose.offsetAndRotation(0F, 16F, 0F, 0F, -2.356194F, 0F));
        root.addOrReplaceChild("shape7",
                CubeListBuilder.create().mirror(true).texOffs(112, 0)
                        .addBox(-16F, 0F, -16F, 32F, 48F, 32F),
                PartPose.offsetAndRotation(0F, -8F, 0F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape7a",
                CubeListBuilder.create().mirror(true).texOffs(112, 0)
                        .addBox(-16F, 0F, -16F, 32F, 48F, 32F),
                PartPose.offsetAndRotation(0F, -8F, 0F, 0F, 1.178097F, 0F));
        root.addOrReplaceChild("shape7b",
                CubeListBuilder.create().mirror(true).texOffs(112, 0)
                        .addBox(-16F, 0F, -16F, 32F, 32F, 32F),
                PartPose.offsetAndRotation(0F, -8F, 0F, 0F, 0.7853982F, 0F));
        root.addOrReplaceChild("shape7c",
                CubeListBuilder.create().mirror(true).texOffs(112, 0)
                        .addBox(-16F, 0F, -16F, 32F, 48F, 32F),
                PartPose.offsetAndRotation(0F, -8F, 0F, 0F, 0.3926991F, 0F));
        root.addOrReplaceChild("shape6f",
                CubeListBuilder.create().mirror(true).texOffs(0, 125)
                        .addBox(-2F, -7F, 12.5F, 4F, 14F, 115F),
                PartPose.offsetAndRotation(0F, 16F, 0F, 0F, 3.141593F, 0F));
        root.addOrReplaceChild("shape6a",
                CubeListBuilder.create().mirror(true).texOffs(0, 125)
                        .addBox(-2F, -7F, 12.5F, 4F, 14F, 115F),
                PartPose.offsetAndRotation(0F, 16F, 0F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape6b",
                CubeListBuilder.create().mirror(true).texOffs(0, 125)
                        .addBox(-2F, -7F, 12.5F, 4F, 14F, 115F),
                PartPose.offsetAndRotation(0F, 16F, 0F, 0F, 0.7853982F, 0F));
        root.addOrReplaceChild("shape6",
                CubeListBuilder.create().mirror(true).texOffs(0, 125)
                        .addBox(-2F, -7F, 12.5F, 4F, 14F, 115F),
                PartPose.offsetAndRotation(0F, 16F, 0F, 0F, 1.570796F, 0F));
        root.addOrReplaceChild("shape6c",
                CubeListBuilder.create().mirror(true).texOffs(0, 125)
                        .addBox(-2F, -7F, 12.5F, 4F, 14F, 115F),
                PartPose.offsetAndRotation(0F, 16F, 0F, 0F, 2.356194F, 0F));
        root.addOrReplaceChild("shape6d",
                CubeListBuilder.create().mirror(true).texOffs(0, 125)
                        .addBox(-2F, -7F, 12.5F, 4F, 14F, 115F),
                PartPose.offsetAndRotation(0F, 16F, 0F, 0F, -0.7853982F, 0F));
        root.addOrReplaceChild("shape6e",
                CubeListBuilder.create().mirror(true).texOffs(0, 125)
                        .addBox(-2F, -7F, 12.5F, 4F, 14F, 115F),
                PartPose.offsetAndRotation(0F, 16F, 0F, 0F, -1.570796F, 0F));
        root.addOrReplaceChild("shape8",
                CubeListBuilder.create().mirror(true).texOffs(0, 49)
                        .addBox(-12F, 0F, -12F, 24F, 4F, 24F),
                PartPose.offsetAndRotation(0F, -10F, 0F, 0F, 1.178097F, 0F));
        root.addOrReplaceChild("shape8a",
                CubeListBuilder.create().mirror(true).texOffs(0, 49)
                        .addBox(-12F, 0F, -12F, 24F, 4F, 24F),
                PartPose.offsetAndRotation(0F, -12F, 0F, 0F, 0F, 0F));
        root.addOrReplaceChild("shape8b",
                CubeListBuilder.create().mirror(true).texOffs(0, 49)
                        .addBox(-12F, 0F, -12F, 24F, 4F, 24F),
                PartPose.offsetAndRotation(0F, -11F, 0F, 0F, 0.7853982F, 0F));
        root.addOrReplaceChild("shape8c",
                CubeListBuilder.create().mirror(true).texOffs(0, 49)
                        .addBox(-12F, 0F, -12F, 24F, 4F, 24F),
                PartPose.offsetAndRotation(0F, -10F, 0F, 0F, 0.3926991F, 0F));
        return LayerDefinition.create(mesh, 256, 256);
    }

    public void renderAll(PoseStack stack, VertexConsumer vc, int light) {
        shape3.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape1a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape1b.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape1c.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape1d.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape1e.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape1f.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape1g.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape1h.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape1i.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape1j.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape1k.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape1l.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape1n.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape1o.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape1z.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3b.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3c.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3d.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3e.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3f.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3g.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape1m.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4b.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4c.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4d.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4e.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4f.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4g.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4h.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3h.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4i.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3i.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4j.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3j.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4k.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3k.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4l.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3l.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4m.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3m.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4n.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3n.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape4o.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape3o.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape6g.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape6f.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape6a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape6b.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape6.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape6c.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape6d.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape6e.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        // The hub column (shape7*/shape8*) is authored 3 blocks tall (model Y -8..40); the original
        // renderAll Y-squashes it (glTranslated(0, -0.1875, 0); glScaled(1, 0.67, 1)) so it fits the
        // 2-block hub with its base at the magnet TE. Without this the raw column extends one block
        // below the TE, clipping into the drive shaft under the hub.
        stack.pushPose();
        stack.translate(0.0, -0.1875, 0.0);
        stack.scale(1.0F, 0.67F, 1.0F);
        shape7.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape7a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape7b.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape7c.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape8.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape8a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape8b.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shape8c.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        stack.popPose();
    }
}
