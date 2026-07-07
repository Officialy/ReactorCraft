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

/**
 * 26.2 port of the Techne ModelDiffuser (steam diffuser, {@code diffuser.png}, 128x128). Purely a
 * static box collection — no rotation angles or moving parts in the legacy model — so every Shape*
 * bakes to one part with all its boxes, and rendering is a single flat pass.
 */
public class ModelDiffuser {

    private final ModelPart root;

    public ModelDiffuser(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        CubeListBuilder cubes = CubeListBuilder.create().mirror(true)
                .texOffs(66, 10).addBox(-6F, 13F, 7F, 12F, 7F, 1F)
                .texOffs(0, 18).addBox(-7.5F, 9F, -7.5F, 15F, 2F, 15F)
                .texOffs(66, 0).addBox(-6F, 12F, -8F, 12F, 7F, 1F)
                .texOffs(10, 36).addBox(3F, 11F, 5F, 2F, 7F, 2F)
                .texOffs(0, 51).addBox(-8F, 8F, -8F, 16F, 1F, 16F)
                .texOffs(0, 18).addBox(-7.5F, 21F, -7.5F, 15F, 2F, 15F)
                .texOffs(0, 70).addBox(-8F, 23F, -8F, 16F, 1F, 16F)
                .texOffs(22, 0).addBox(6.9F, 9F, 6.9F, 1F, 14F, 1F)
                .texOffs(22, 0).addBox(-7.9F, 9F, 6.9F, 1F, 14F, 1F)
                .texOffs(22, 0).addBox(6.9F, 9F, -7.9F, 1F, 14F, 1F)
                .texOffs(22, 0).addBox(-7.9F, 9F, -7.9F, 1F, 14F, 1F)
                .texOffs(0, 36).addBox(-1F, 14F, -7F, 2F, 7F, 2F)
                .texOffs(0, 0).addBox(4F, 11F, 3F, 1F, 10F, 1F)
                .texOffs(0, 36).addBox(-5F, 14F, -7F, 2F, 7F, 2F)
                .texOffs(10, 36).addBox(-5F, 11F, 5F, 2F, 7F, 2F)
                .texOffs(10, 36).addBox(-1F, 11F, 5F, 2F, 7F, 2F)
                .texOffs(0, 46).addBox(-4.5F, 12.5F, -7F, 1F, 2F, 1F)
                .texOffs(0, 0).addBox(4F, 11F, -4F, 1F, 10F, 1F)
                .texOffs(0, 0).addBox(1F, 11F, -4F, 1F, 10F, 1F)
                .texOffs(0, 0).addBox(-2F, 11F, -4F, 1F, 10F, 1F)
                .texOffs(0, 0).addBox(-5F, 11F, -4F, 1F, 10F, 1F)
                .texOffs(10, 0).addBox(4F, 11F, -1F, 2F, 10F, 2F)
                .texOffs(0, 0).addBox(-5F, 11F, 3F, 1F, 10F, 1F)
                .texOffs(0, 0).addBox(-2F, 11F, 3F, 1F, 10F, 1F)
                .texOffs(0, 0).addBox(1F, 11F, 3F, 1F, 10F, 1F)
                .texOffs(10, 0).addBox(-6F, 11F, -1F, 2F, 10F, 2F)
                .texOffs(10, 0).addBox(-1F, 11F, -1F, 2F, 10F, 2F)
                .texOffs(0, 36).addBox(3F, 14F, -7F, 2F, 7F, 2F)
                .texOffs(10, 46).addBox(-4.5F, 17.5F, 6F, 1F, 2F, 1F)
                .texOffs(0, 46).addBox(-0.5F, 12.5F, -7F, 1F, 2F, 1F)
                .texOffs(0, 46).addBox(3.5F, 12.5F, -7F, 1F, 2F, 1F)
                .texOffs(10, 46).addBox(3.5F, 17.5F, 6F, 1F, 2F, 1F)
                .texOffs(10, 46).addBox(-0.5F, 17.5F, 6F, 1F, 2F, 1F)
                .texOffs(64, 29).addBox(-6.5F, 14F, -1.5F, 13F, 1F, 3F)
                .texOffs(64, 24).addBox(-6.5F, 15F, 2.5F, 13F, 2F, 2F)
                .texOffs(64, 29).addBox(-6.5F, 19F, -1.5F, 13F, 1F, 3F)
                .texOffs(64, 29).addBox(-6.5F, 17F, -1.5F, 13F, 1F, 3F)
                .texOffs(64, 29).addBox(-6.5F, 12F, -1.5F, 13F, 1F, 3F)
                .texOffs(64, 20).addBox(-6.5F, 13F, 2.5F, 13F, 1F, 2F)
                .texOffs(64, 20).addBox(-6.5F, 18F, 2.5F, 13F, 1F, 2F)
                .texOffs(64, 20).addBox(-6.5F, 13F, -4.5F, 13F, 1F, 2F)
                .texOffs(64, 24).addBox(-6.5F, 15F, -4.5F, 13F, 2F, 2F)
                .texOffs(64, 20).addBox(-6.5F, 18F, -4.5F, 13F, 1F, 2F);
        root.addOrReplaceChild("main", cubes, PartPose.ZERO);
        return LayerDefinition.create(mesh, 128, 128);
    }

    public void renderAll(PoseStack stack, VertexConsumer vc, int light) {
        root.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
    }
}
