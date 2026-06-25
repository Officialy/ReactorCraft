/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 ******************************************************************************/
package reika.reactorcraft.renders;

import java.util.ArrayList;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;

import reika.reactorcraft.base.ReactorTERenderer;
import reika.reactorcraft.tileentities.TileEntityFusionMarker;
import reika.reactorcraft.tileentities.fusion.TileEntityToroidMagnet.Aim;
import reika.rotarycraft.renders.RotaryRenderPipelines;

/**
 * 26.2 port of the immediate-mode fusion-marker renderer (the first ReactorCraft BER). When the
 * marker has a redstone signal ({@link TileEntityFusionMarker#renderLines()}) it draws the tokamak
 * build-guide as coloured line loops — yellow toroid-magnet boxes, cyan plasma-injector boxes, and a
 * red central solenoid ring — so the player can see where to place each part. A small blue body box
 * keeps the (otherwise model-less, BER-rendered) block visible.
 *
 * <p>The legacy GL11/Tessellator line draws become {@link RotaryRenderPipelines#NO_DEPTH_LINES_TYPE}
 * (and {@code NO_DEPTH_FILLED_BOX_TYPE}) submissions using the same {@code line()} pattern as
 * RotaryCraft's {@code IORenderer}. Per-box rotation+offset (legacy {@code glRotated}/{@code glTranslated})
 * is baked into the vertex coordinates in Java; the base transform (the legacy
 * {@code translate(0,2,1) · scale(1,-1,-1) · translate(0.5,0.5,0.5)}) stays on the PoseStack.
 */
public class RenderFusionMarker extends ReactorTERenderer<TileEntityFusionMarker> {

    public RenderFusionMarker(BlockEntityRendererProvider.Context context) {
    }

    // The build guide spans the whole tokamak (14+ blocks out), so don't frustum-cull it to the
    // marker block's own cell — otherwise the far toroid/injector lines vanish when the marker
    // itself is off-screen.
    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public void submit(BlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null)
            return;
        BlockEntity be = mc.level.getBlockEntity(state.blockPos);
        if (!(be instanceof TileEntityFusionMarker tile))
            return;

        PoseStack ps = new PoseStack();
        ps.last().set(poseStack.last());
        // Legacy base transform from renderTileEntityFusionMarkerAt (par2/4/6 == 0 in the BER).
        ps.translate(0.0, 2.0, 1.0);
        ps.scale(1.0F, -1.0F, -1.0F);
        ps.translate(0.5, 0.5, 0.5);

        // Always-visible marker body (substitutes the legacy redstone-torch cross billboard).
        int body = rgba(0, 51, 255, 255);
        collector.submitCustomGeometry(ps, RotaryRenderPipelines.NO_DEPTH_FILLED_BOX_TYPE,
                (pose, buf) -> emitBox(pose, buf, -0.12F, -0.5F, -0.12F, 0.12F, 0.5F, 0.12F, rgba(0, 51, 255, 160)));

        if (tile.renderLines()) {
            collector.submitCustomGeometry(ps, RotaryRenderPipelines.NO_DEPTH_LINES_TYPE,
                    (pose, buf) -> emitGuides(pose, buf, tile));
        }
    }

    /** Walks the aim-point ring (legacy {@code renderPositions}) emitting one box per part + the solenoid. */
    private static void emitGuides(PoseStack.Pose pose, VertexConsumer b, TileEntityFusionMarker tile) {
        ArrayList<Aim> li = tile.getAimPoints();
        int dx = 0;
        int dz = 14; // legacy: z += 14 before the loop
        for (int i = 0; i < li.size(); i++) {
            Aim a = li.get(i);
            double ang = Math.toRadians(a.angle);
            double cos = Math.cos(ang);
            double sin = Math.sin(ang);
            if (i % 10 != 0)
                emitToroidBox(pose, b, dx, dz, cos, sin);
            else
                emitInjectorBox(pose, b, dx, dz, cos, sin);
            dx += a.xOffset;
            dz += a.zOffset;
        }
        emitSolenoid(pose, b);
    }

    // ---- per-part geometry (legacy LINE_LOOP -> consecutive segments; LINES -> as-is) ----

    private static final int TOROID = rgba(255, 255, 0, 255);
    private static final int INJECTOR = rgba(0, 200, 255, 255);
    private static final int SOLENOID = rgba(255, 0, 0, 255);

    private static void emitToroidBox(PoseStack.Pose p, VertexConsumer b, int dx, int dz, double c, double s) {
        double y = 1.499;
        // +X face loop
        double[] f1 = {0.25, 0, -0.5, 0.25, 0, 0.5, 0.25, -1, 1.5, 0.25, -2, 1.5, 0.25, -3, 0.5, 0.25, -3, -0.5, 0.25, -2, -1.5, 0.25, -1, -1.5};
        loop(p, b, dx, dz, c, s, y, f1, TOROID);
        // -X face loop
        double[] f2 = {-0.25, 0, -0.5, -0.25, 0, 0.5, -0.25, -1, 1.5, -0.25, -2, 1.5, -0.25, -3, 0.5, -0.25, -3, -0.5, -0.25, -2, -1.5, -0.25, -1, -1.5};
        loop(p, b, dx, dz, c, s, y, f2, TOROID);
        // connecting struts
        for (int i = 0; i < f1.length; i += 3)
            seg(p, b, dx, dz, c, s, y, f1[i], f1[i + 1], f1[i + 2], f2[i], f2[i + 1], f2[i + 2], TOROID);
    }

    private static void emitInjectorBox(PoseStack.Pose p, VertexConsumer b, int dx, int dz, double c, double s) {
        double y = 1.499;
        double[] top = {-1.5, 0, -2.5, 1.5, 0, -2.5, 1.5, 0, 6.5, -1.5, 0, 6.5};
        loop(p, b, dx, dz, c, s, y, top, INJECTOR);
        double[] bot = {-1.5, -5, -2.5, 1.5, -5, -2.5, 1.5, -3, 6.5, -1.5, -3, 6.5};
        loop(p, b, dx, dz, c, s, y, bot, INJECTOR);
        for (int i = 0; i < top.length; i += 3)
            seg(p, b, dx, dz, c, s, y, bot[i], bot[i + 1], bot[i + 2], top[i], top[i + 1], top[i + 2], INJECTOR);
    }

    private static void emitSolenoid(PoseStack.Pose p, VertexConsumer b) {
        double y = 0.499;
        double[] ring = {8, 3, 8, -3, 7, -5, 6, -6, 5, -7, 3, -8, -3, -8, -5, -7, -6, -6, -7, -5, -8, -3, -8, 3, -7, 5, -6, 6, -5, 7, -3, 8, 3, 8, 5, 7, 6, 6, 7, 5};
        // top ring at +1, bottom ring at -2 (legacy y values), no rotation/offset
        ringLoop(p, b, ring, 1, y);
        ringLoop(p, b, ring, -2, y);
        for (int i = 0; i < ring.length; i += 2)
            line(p, b, (float) ring[i], (float) (-2 + y), (float) ring[i + 1], (float) ring[i], (float) (1 + y), (float) ring[i + 1], SOLENOID);
        // cross spokes (legacy GL_LINES)
        spoke(p, b, 8, 0, -8, 0, y);
        spoke(p, b, 0, 8, 0, -8, y);
        spoke(p, b, 6, 6, -6, -6, y);
        spoke(p, b, -6, 6, 6, -6, y);
    }

    private static void ringLoop(PoseStack.Pose p, VertexConsumer b, double[] ring, double yLvl, double yBase) {
        int n = ring.length / 2;
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            line(p, b, (float) ring[i * 2], (float) (yLvl + yBase), (float) ring[i * 2 + 1],
                    (float) ring[j * 2], (float) (yLvl + yBase), (float) ring[j * 2 + 1], SOLENOID);
        }
    }

    private static void spoke(PoseStack.Pose p, VertexConsumer b, double x1, double z1, double x2, double z2, double yBase) {
        line(p, b, (float) x1, (float) (1 + yBase), (float) z1, (float) x2, (float) (1 + yBase), (float) z2, SOLENOID);
        line(p, b, (float) x1, (float) (-2 + yBase), (float) z1, (float) x2, (float) (-2 + yBase), (float) z2, SOLENOID);
    }

    /** Emits a closed line loop of (x,y,z) triples, rotated by (c,s) and offset by (dx, yBase, dz). */
    private static void loop(PoseStack.Pose p, VertexConsumer b, int dx, int dz, double c, double s, double yBase, double[] v, int rgba) {
        int n = v.length / 3;
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            seg(p, b, dx, dz, c, s, yBase, v[i * 3], v[i * 3 + 1], v[i * 3 + 2], v[j * 3], v[j * 3 + 1], v[j * 3 + 2], rgba);
        }
    }

    /** One segment, baking the per-box Y-rotation (c=cos,s=sin) and (dx,yBase,dz) translation into the coords. */
    private static void seg(PoseStack.Pose p, VertexConsumer b, int dx, int dz, double c, double s, double yBase,
                            double ax, double ay, double az, double bx, double by, double bz, int rgba) {
        double rax = ax * c + az * s, raz = -ax * s + az * c;
        double rbx = bx * c + bz * s, rbz = -bx * s + bz * c;
        line(p, b, (float) (dx + rax), (float) (yBase + ay), (float) (dz + raz),
                (float) (dx + rbx), (float) (yBase + by), (float) (dz + rbz), rgba);
    }

    // ---- primitives (mirror RotaryCraft IORenderer) ----

    private static void line(PoseStack.Pose pose, VertexConsumer b, float x1, float y1, float z1, float x2, float y2, float z2, int rgba) {
        float nx = x2 - x1, ny = y2 - y1, nz = z2 - z1;
        float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (len > 0) { nx /= len; ny /= len; nz /= len; }
        b.addVertex(pose, x1, y1, z1).setColor(rgba).setNormal(pose, nx, ny, nz).setLineWidth(3.0F);
        b.addVertex(pose, x2, y2, z2).setColor(rgba).setNormal(pose, nx, ny, nz).setLineWidth(3.0F);
    }

    private static void emitBox(PoseStack.Pose pose, VertexConsumer b, float x0, float y0, float z0, float x1, float y1, float z1, int rgba) {
        b.addVertex(pose, x0, y0, z0).setColor(rgba); b.addVertex(pose, x1, y0, z0).setColor(rgba); b.addVertex(pose, x1, y0, z1).setColor(rgba); b.addVertex(pose, x0, y0, z1).setColor(rgba);
        b.addVertex(pose, x0, y1, z1).setColor(rgba); b.addVertex(pose, x1, y1, z1).setColor(rgba); b.addVertex(pose, x1, y1, z0).setColor(rgba); b.addVertex(pose, x0, y1, z0).setColor(rgba);
        b.addVertex(pose, x0, y0, z0).setColor(rgba); b.addVertex(pose, x0, y1, z0).setColor(rgba); b.addVertex(pose, x1, y1, z0).setColor(rgba); b.addVertex(pose, x1, y0, z0).setColor(rgba);
        b.addVertex(pose, x1, y0, z1).setColor(rgba); b.addVertex(pose, x1, y1, z1).setColor(rgba); b.addVertex(pose, x0, y1, z1).setColor(rgba); b.addVertex(pose, x0, y0, z1).setColor(rgba);
        b.addVertex(pose, x0, y0, z1).setColor(rgba); b.addVertex(pose, x0, y1, z1).setColor(rgba); b.addVertex(pose, x0, y1, z0).setColor(rgba); b.addVertex(pose, x0, y0, z0).setColor(rgba);
        b.addVertex(pose, x1, y0, z0).setColor(rgba); b.addVertex(pose, x1, y1, z0).setColor(rgba); b.addVertex(pose, x1, y1, z1).setColor(rgba); b.addVertex(pose, x1, y0, z1).setColor(rgba);
    }

    private static int rgba(int r, int g, int bl, int a) {
        return (a << 24) | (r << 16) | (g << 8) | bl;
    }
}
