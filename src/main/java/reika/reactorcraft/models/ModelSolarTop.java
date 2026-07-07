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
 * 26.2 port of the Techne ModelSolarTop (solar-tower top, {@code solartop.png}, 128x128). Two groups:
 * the static frame (base plate, four angled corner legs, four corner posts, the central column) drawn
 * plain, and the "coil" group ({@code heat}, legacy Shape5*) drawn with the caller-supplied ARGB tint
 * (legacy: blackbody color for the tower's current temperature, with entity lighting disabled so the
 * tint reads at full intensity — see RenderSolarTop).
 */
public class ModelSolarTop {

    private final ModelPart base;
    private final ModelPart heat;

    public ModelSolarTop(ModelPart root) {
        this.base = root.getChild("base");
        this.heat = root.getChild("heat");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition base = root.addOrReplaceChild("base", CubeListBuilder.create(), PartPose.ZERO);
        base.addOrReplaceChild("plate",
                CubeListBuilder.create().mirror(true).texOffs(0, 55).addBox(-8F, 23F, -8F, 16F, 1F, 16F),
                PartPose.ZERO);
        base.addOrReplaceChild("leg_a",
                CubeListBuilder.create().mirror(true).texOffs(30, 0).addBox(-1F, -8.5F, -1F, 2F, 10F, 2F),
                PartPose.offsetAndRotation(4F, 23F, 4F, -0.3141593F, 0F, 0.3141593F));
        base.addOrReplaceChild("leg_b",
                CubeListBuilder.create().mirror(true).texOffs(0, 0).addBox(-1F, -8.5F, -1F, 2F, 10F, 2F),
                PartPose.offsetAndRotation(4F, 23F, -4F, 0.3141593F, 0F, 0.3141593F));
        base.addOrReplaceChild("leg_c",
                CubeListBuilder.create().mirror(true).texOffs(10, 0).addBox(-1F, -8.5F, -1F, 2F, 10F, 2F),
                PartPose.offsetAndRotation(-4F, 23F, -4F, 0.3141593F, 0F, -0.3141593F));
        base.addOrReplaceChild("leg_d",
                CubeListBuilder.create().mirror(true).texOffs(20, 0).addBox(-1F, -8.5F, -1F, 2F, 10F, 2F),
                PartPose.offsetAndRotation(-4F, 23F, 4F, -0.3141593F, 0F, -0.3141593F));
        base.addOrReplaceChild("post_a",
                CubeListBuilder.create().mirror(true).texOffs(30, 13).addBox(0F, 0F, 0F, 2F, 8F, 2F),
                PartPose.offset(5.5F, 8F, -7.5F));
        base.addOrReplaceChild("post_b",
                CubeListBuilder.create().mirror(true).texOffs(0, 13).addBox(0F, 0F, 0F, 2F, 8F, 2F),
                PartPose.offset(-7.5F, 8F, -7.5F));
        base.addOrReplaceChild("post_c",
                CubeListBuilder.create().mirror(true).texOffs(10, 13).addBox(0F, 0F, 0F, 2F, 8F, 2F),
                PartPose.offset(-7.5F, 8F, 5.5F));
        base.addOrReplaceChild("post_d",
                CubeListBuilder.create().mirror(true).texOffs(20, 13).addBox(0F, 0F, 0F, 2F, 8F, 2F),
                PartPose.offset(5.5F, 8F, 5.5F));
        base.addOrReplaceChild("column",
                CubeListBuilder.create().mirror(true).texOffs(40, 0).addBox(0F, 0F, 0F, 8F, 15F, 8F),
                PartPose.offset(-4F, 8F, -4F));

        // Coil group: 29 thin bars, all texOffs(0,24), all zero rotation — tinted by temperature at
        // render time. Encoded as (x, y, z, w, h, d) with the legacy rotationPoint as the box origin.
        CubeListBuilder heatCubes = CubeListBuilder.create().mirror(true).texOffs(0, 24)
                .addBox(4F, 16F, 1F, 1F, 7F, 1F)
                .addBox(4F, 11F, -3F, 1F, 8F, 1F)
                .addBox(4F, 8F, 2F, 1F, 9F, 1F)
                .addBox(4F, 20F, -1F, 1F, 3F, 1F)
                .addBox(-5F, 18F, 0F, 2F, 1F, 1F)
                .addBox(4F, 18F, -2F, 1F, 3F, 1F)
                .addBox(4F, 8F, -2F, 1F, 4F, 1F)
                .addBox(0F, 14F, -5F, 1F, 4F, 1F)
                .addBox(-5F, 13F, 1F, 1F, 10F, 1F)
                .addBox(-5F, 20F, -2F, 1F, 3F, 1F)
                .addBox(-5F, 13F, 0F, 2F, 1F, 1F)
                .addBox(-5F, 12F, -1F, 1F, 2F, 1F)
                .addBox(-5F, 18F, -1F, 1F, 3F, 1F)
                .addBox(-5F, 8F, -2F, 1F, 5F, 1F)
                .addBox(-3F, 8F, -5F, 1F, 4F, 1F)
                .addBox(-2F, 11F, -5F, 1F, 2F, 1F)
                .addBox(-1F, 15F, 4F, 1F, 5F, 1F)
                .addBox(-1F, 12F, -5F, 1F, 3F, 1F)
                .addBox(2F, 18F, -5F, 1F, 5F, 1F)
                .addBox(-1F, 17F, -5F, 1F, 3F, 1F)
                .addBox(1F, 17F, -5F, 1F, 2F, 1F)
                .addBox(-2F, 19F, -5F, 1F, 4F, 1F)
                .addBox(-3F, 8F, 4F, 1F, 7F, 1F)
                .addBox(1F, 19F, 4F, 1F, 4F, 1F)
                .addBox(0F, 11F, 4F, 1F, 4F, 1F)
                .addBox(2F, 16F, 4F, 1F, 4F, 1F)
                .addBox(-2F, 19F, 4F, 1F, 4F, 1F)
                .addBox(-2F, 14F, 4F, 1F, 2F, 1F)
                .addBox(1F, 8F, 4F, 1F, 4F, 1F)
                .addBox(1F, 14F, 4F, 1F, 3F, 1F);
        root.addOrReplaceChild("heat", heatCubes, PartPose.ZERO);

        return LayerDefinition.create(mesh, 128, 128);
    }

    public void renderAll(PoseStack stack, VertexConsumer vc, int light, int tintARGB) {
        base.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        heat.render(stack, vc, light, OverlayTexture.NO_OVERLAY, tintARGB);
    }
}
