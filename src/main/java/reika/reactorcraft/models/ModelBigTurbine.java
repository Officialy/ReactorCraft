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
 * 26.2 port of the Techne ModelBigTurbine (high-pressure turbine, {@code bigturbine.png}, 128x128) —
 * same blade-ring layout as {@link ModelTurbine} but with per-stage blade sizing (the legacy overrides
 * of getBladeLength/getAngularSeparation/getScaleFactor) plus an outer housing ring built from two
 * boxes ({@code housing}/{@code housing2}) repeated around the shaft every {@code getHousingSegments()}
 * degrees. The legacy Shape4/Shape4a/Shape4b parts were commented out upstream (stage 0 only, dead
 * code) and the Shape220b "cover plate" (also stage-0-only, cosmetic) is skipped as a minor deferral.
 */
public class ModelBigTurbine {

    private static final double VO = 0.9375;   // pivot height (legacy)
    private static final double DD = 0.25;     // ring Z-separation

    /** Highest big-turbine stage (TileEntityHiPTurbine.getMaxStage()); models baked for 0..MAX_STAGE. */
    public static final int MAX_STAGE = 6;

    private final ModelPart shaft1;
    private final ModelPart shaft1a;
    private final ModelPart blade;
    private final ModelPart housing;
    private final ModelPart housing2;
    private final int stage;

    public ModelBigTurbine(ModelPart root, int stage) {
        this.shaft1 = root.getChild("shaft1");
        this.shaft1a = root.getChild("shaft1a");
        this.blade = root.getChild("blade");
        this.housing = root.getChild("housing");
        this.housing2 = root.getChild("housing2");
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

        int w1 = housingLength(stage);
        int w2 = stage == 0 ? 12 : housingLength(stage) + 1;
        int l1 = len + 2;
        int l2 = (int) Math.ceil(len * scaleFactor(stage)) + 2;
        int d = housingDepth(stage);
        root.addOrReplaceChild("housing",
                CubeListBuilder.create().mirror(true).texOffs(58, 11).addBox(-w1, l1, 0F, w1 * 2, 1, d),
                PartPose.offset(0F, 15F, 0F));
        root.addOrReplaceChild("housing2",
                CubeListBuilder.create().mirror(true).texOffs(58, 11).addBox(-w2, l2, -8F, w2 * 2, 1, d),
                PartPose.offset(0F, 15F, 0F));

        return LayerDefinition.create(mesh, 128, 128);
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

        renderHousing(stack, vc, light);
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

    private void renderHousing(PoseStack stack, VertexConsumer vc, int light) {
        int a = 360 / housingSegments(stage);
        for (int i = 0; i < 360; i += a) {
            stack.pushPose();
            stack.translate(0, 1, 0);
            stack.mulPose(Axis.ZP.rotationDegrees(i));
            stack.translate(0, -1, 0);
            housing.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
            housing2.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
            stack.popPose();
        }
    }

    // --- legacy per-stage blade parameters (Reika.ReactorCraft.Models.ModelBigTurbine) ---
    static int bladeLength(int s) {
        return switch (s) {
            case 0 -> 20;
            case 1 -> 31;
            case 2 -> 38;
            case 3 -> 46;
            case 4 -> 53;
            case 5 -> 62;
            case 6 -> 72;
            default -> 4;
        };
    }

    static int bladeWidth(int s) {
        // ModelBigTurbine inherits ModelTurbine.getBladeWidth() unchanged.
        return ModelTurbine.bladeWidth(s);
    }

    static int angularSep(int s) {
        return switch (s) {
            case 0, 6 -> 4;
            default -> 3; // 1-5
        };
    }

    static float bladeTwist(int s) {
        // ModelBigTurbine inherits ModelTurbine.getBladeTwist() unchanged.
        return ModelTurbine.bladeTwist(s);
    }

    static double scaleFactor(int s) {
        double d = s == 0 ? 1.3 : 1.1;
        if (s == 3)
            d = 1.075;
        return s >= 5 ? d * (1 - (s - 2) * 0.01) : d;
    }

    static int housingDepth(int s) {
        return switch (s) {
            case 0, 1, 2 -> 6;
            case 3, 4 -> 7;
            case 5 -> 8;
            case 6 -> 9;
            default -> 6;
        };
    }

    static int housingSegments(int s) {
        return switch (s) {
            case 0 -> 8;
            case 1 -> 10;
            case 2, 3 -> 12;
            case 4, 5 -> 18;
            case 6 -> 20;
            default -> 10;
        };
    }

    static int housingLength(int s) {
        return switch (s) {
            case 0 -> 9;
            case 1, 2 -> 11;
            case 3 -> 13;
            case 4 -> 10;
            case 5, 6 -> 12;
            default -> 4;
        };
    }
}
