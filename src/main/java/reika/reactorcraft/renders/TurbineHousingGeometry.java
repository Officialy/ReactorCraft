/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.renders;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.core.Direction;
import net.minecraft.client.renderer.texture.OverlayTexture;

/** Original unformed turbine/flywheel caps, with standalone sprite UVs and native entity vertices. */
public final class TurbineHousingGeometry {
    private static final float[][][] FACES = {
        {{0,1,0,0,0},{1,1,0,1,0},{1,1,1,1,1},{0,1,1,0,1}},
        {{0,2,1,0,1},{1,2,1,1,1},{1,2,0,1,0},{0,2,0,0,0}},
        {{1,2,1,1,1},{1,1,1,1,0},{1,1,0,0,0},{1,2,0,0,1}},
        {{0,2,1,0,1},{0,1,1,0,0},{1,1,1,1,0},{1,2,1,1,1}},
        {{1,2,0,1,1},{1,1,0,1,0},{0,1,0,0,0},{0,2,0,0,1}},
        {{0,2,0,0,1},{0,1,0,0,0},{0,1,1,1,0},{0,2,1,1,1}}
    };
    private static final float[][] NORMALS = {{0,-1,0},{0,1,0},{1,0,0},{0,0,1},{0,0,-1},{-1,0,0}};
    private TurbineHousingGeometry() {}

    public static void orient(PoseStack pose, Direction facing) {
        pose.translate(0, 2, 1);
        pose.scale(1, -1, -1);
        pose.translate(.5, .5, .5);
        pose.rotate(Axis.YP.rotationDegrees(switch (facing) {
            case WEST -> 90F;
            case EAST -> 270F;
            case NORTH -> 180F;
            default -> 0F;
        }));
    }

    public static void render(PoseStack pose, VertexConsumer vertices, int light, Direction facing) {
        pose.pushPose();
        orient(pose, facing);
        pose.translate(-.5, -.5, -.5);
        for (int face = 0; face < FACES.length; face++) {
            float[] normal = NORMALS[face];
            for (float[] vertex : FACES[face]) {
                vertices.addVertex(pose.last(), vertex[0], vertex[1], vertex[2]).setColor(255,255,255,255)
                        .setUv(vertex[3], vertex[4]).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                        .setNormal(pose.last(), normal[0], normal[1], normal[2]);
            }
        }
        pose.popPose();
    }
}
