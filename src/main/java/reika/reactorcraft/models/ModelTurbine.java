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
 * 26.2 port of the Techne ModelTurbine — the steam-turbine wheel. PROCEDURAL: one {@code blade} is
 * drawn once per {@code angularSep} degrees around the shaft (Z axis, pivot y=VO), twisted about Y,
 * for two stacked rings (the back ring scaled by {@code scaleFactor}). Blade length/width/separation/
 * twist all depend on the multiblock STAGE — that's what makes a row of turbine cores render as one
 * big tapered turbine (small fast blades at the inlet, large blades downstream) rather than identical
 * wheels. Each stage gets its own baked {@link LayerDefinition} (the blade box size is baked); the
 * renderer picks the model for {@code TileEntityTurbineCore.getStage()}.
 */
public class ModelTurbine {

    private static final double VO = 0.9375;   // pivot height (legacy)
    private static final double DD = 0.25;     // ring Z-separation

    /** Highest base-turbine stage (TileEntityTurbineCore.getMaxStage()); models baked for 0..MAX_STAGE. */
    public static final int MAX_STAGE = 4;

    private final ModelPart shaft1;
    private final ModelPart shaft1a;
    private final ModelPart blade;
    private final int stage;

    public ModelTurbine(ModelPart root, int stage) {
        this.shaft1 = root.getChild("shaft1");
        this.shaft1a = root.getChild("shaft1a");
        this.blade = root.getChild("blade");
        this.stage = stage;
    }

    public static LayerDefinition createLayer(int stage) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("shaft1",
                CubeListBuilder.create().mirror(true).texOffs(0, 106).addBox(-2F, -2F, 0F, 4F, 4F, 16F),
                PartPose.offsetAndRotation(0F, 15F, -8F, 0F, 0F, 0.7853982F));
        root.addOrReplaceChild("shaft1a",
                CubeListBuilder.create().mirror(true).texOffs(0, 106).addBox(-2F, -2F, 0F, 4F, 4F, 16F),
                PartPose.offsetAndRotation(0F, 15F, -8F, 0F, 0F, 0F));
        int len = bladeLength(stage);
        int wid = bladeWidth(stage);
        root.addOrReplaceChild("blade",
                CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, -len, -wid / 2F, 1F, len, wid),
                PartPose.offset(0F, 15F, 0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    public void renderAll(PoseStack stack, VertexConsumer vc, int light) {
        this.renderAll(stack, vc, light, 0F);
    }

    /** {@code phi} is the current spin angle (deg) of the blade wheel about the shaft (Z) axis. */
    public void renderAll(PoseStack stack, VertexConsumer vc, int light, float phi) {
        shaft1.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        shaft1a.render(stack, vc, light, OverlayTexture.NO_OVERLAY);

        float sc = (float) scaleFactor(stage);

        // front ring
        stack.pushPose();
        stack.translate(0, 0, DD);
        renderBlades(stack, vc, light, phi);
        stack.popPose();

        // back ring (offset the other way, scaled up about the pivot)
        stack.pushPose();
        stack.translate(0, 0, -DD);
        stack.translate(0, VO, 0);
        stack.scale(sc, sc, 1F);
        stack.translate(0, -VO, 0);
        renderBlades(stack, vc, light, phi);
        stack.popPose();
    }

    private void renderBlades(PoseStack stack, VertexConsumer vc, int light, float phi) {
        int da = angularSep(stage);
        for (int i = 0; i < 360; i += da) {
            stack.pushPose();
            stack.translate(0, VO, 0);
            stack.mulPose(Axis.ZP.rotationDegrees(i + phi));
            stack.translate(0, -VO, 0);
            stack.mulPose(Axis.YP.rotationDegrees(-bladeTwist(stage)));
            blade.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
            stack.popPose();
        }
    }

    // --- legacy per-stage blade parameters (Reika.ReactorCraft.Models.ModelTurbine) ---
    static int bladeLength(int s) {
        return switch (s) {
            case 0 -> 16;
            case 1 -> 24;
            case 2 -> 28;
            case 3 -> 33;
            case 4 -> 40;
            default -> 4;
        };
    }

    static int bladeWidth(int s) {
        return switch (s) {
            case 3 -> 3;
            case 4 -> 4;
            case 5 -> 6;
            case 6 -> 8;
            default -> 2;
        };
    }

    static int angularSep(int s) {
        return switch (s) {
            case 1 -> 5;
            case 5 -> 9;
            case 6 -> 10;
            default -> 8; // 0, 2, 3, 4
        };
    }

    static float bladeTwist(int s) {
        return switch (s) {
            case 0 -> 10;
            case 1 -> 15;
            case 2 -> 20;
            case 3 -> 30;
            case 4, 5 -> 45;
            case 6 -> 50;
            default -> 10;
        };
    }

    static double scaleFactor(int s) {
        return s < 1 ? 1.3 : 1.1;
    }
}
