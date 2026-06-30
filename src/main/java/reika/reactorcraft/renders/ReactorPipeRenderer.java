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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.joml.Matrix4f;

import reika.reactorcraft.base.ReactorTERenderer;
import reika.reactorcraft.base.TileEntityReactorPiping;

/**
 * 26.2 BER for the ReactorCraft fluid ducts (gas duct, magnetic pipe, waste pipe). Port of the legacy
 * {@code DuctRenderer} → {@code PipeRenderer.renderLiquid}: the iron-cross shell is drawn by the
 * multipart blockstate JSON (reusing RotaryCraft's {@code block/pipe/*} models), and ONLY the fluid
 * surface is BER-rendered here because it updates every tick from the BE's {@code fluid}/{@code level}.
 *
 * <p>For each side, draws either a sealed cap at the pipe inner edge (side not connected) or four
 * "tube wall" quads extending to the block face (side connected) — exactly the legacy geometry, so
 * the visual is the slim translucent fluid tube. Geometry is copied verbatim from
 * {@link reika.rotarycraft.renders.PipeRenderer}; only the connection source ({@link
 * TileEntityReactorPiping#isConnectedDirectly}) and the per-fluid tint/sprite differ.</p>
 */
public class ReactorPipeRenderer extends ReactorTERenderer<TileEntityReactorPiping> {

    // Legacy 1.7 constants — see Reika.RotaryCraft.Renders.PipeRenderer.
    private static final float SIZE = 0.75F / 2F;          // 0.375 — half-width of the pipe
    private static final double IN = 0.5 + SIZE - 0.01;    // 0.865 — inner-far edge
    private static final double IN2 = 0.5 - SIZE + 0.01;   // 0.135 — inner-near edge
    static final double DD2 = IN - IN2;                    // 0.730 — inner span (shared with ReactorLineRenderer)

    private final SpriteGetter sprites;

    public ReactorPipeRenderer(BlockEntityRendererProvider.Context context) {
        this.sprites = context.sprites();
    }

    @Override
    public void submit(BlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;
        BlockEntity be = level.getBlockEntity(state.blockPos);
        if (!(be instanceof TileEntityReactorPiping tile)) return;

        Fluid fluid = tile.getFluidType();
        boolean hasFluid = fluid != null && fluid != Fluids.EMPTY && tile.getFluidLevel() > 0;

        // Full pipe → translucent fluid tube; empty pipe → neutral grey shell tube so it's still
        // visible when placed before fluid flows. One geometry path either way.
        TextureAtlasSprite sprite = hasFluid ? stillSpriteFor(fluid) : shellSprite();
        int tint = hasFluid ? fluidTint(fluid) : 0xFFB0B0B0;

        int light = state.lightCoords;
        int overlay = OverlayTexture.NO_OVERLAY;

        RenderType rt = RenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS);
        PoseStack snapped = new PoseStack();
        snapped.last().set(poseStack.last());

        collector.submitCustomGeometry(poseStack, rt, (pose2, vc) -> {
            float u = sprite.getU0();
            float v = sprite.getV0();
            float u2 = sprite.getU1();
            float v2 = sprite.getV1();
            double du = DD2 * (u2 - u) / 4D;

            Matrix4f m = snapped.last().pose();
            for (Direction dir : Direction.values()) {
                boolean connected = tile.isConnectedDirectly(dir);
                if (connected)
                    emitConnectedFluid(m, vc, dir, u, v, u2, v2, (float) du, IN, IN2, tint, light, overlay);
                else
                    emitCap(m, vc, dir, u, v, u2, v2, IN, IN2, tint, light, overlay);
            }
        });
    }

    static void emitCap(Matrix4f m, VertexConsumer vc, Direction dir,
                        float u, float v, float u2, float v2, double in, double in2, int tint, int light, int overlay) {
        switch (dir) {
            case UP -> quad(m, vc, in2, in, in, u, v2, in, in, in, u2, v2, in, in, in2, u2, v, in2, in, in2, u, v, tint, light, overlay, 0, 1, 0);
            case DOWN -> quad(m, vc, in2, in2, in2, u, v, in, in2, in2, u2, v, in, in2, in, u2, v2, in2, in2, in, u, v2, tint, light, overlay, 0, -1, 0);
            case SOUTH -> quad(m, vc, in, in, in, u, v, in2, in, in, u2, v, in2, in2, in, u2, v2, in, in2, in, u, v2, tint, light, overlay, 0, 0, 1);
            case NORTH -> quad(m, vc, in, in2, in2, u, v2, in2, in2, in2, u2, v2, in2, in, in2, u2, v, in, in, in2, u, v, tint, light, overlay, 0, 0, -1);
            case EAST -> quad(m, vc, in, in2, in, u, v2, in, in2, in2, u2, v2, in, in, in2, u2, v, in, in, in, u, v, tint, light, overlay, 1, 0, 0);
            case WEST -> quad(m, vc, in2, in, in, u, v, in2, in, in2, u2, v, in2, in2, in2, u2, v2, in2, in2, in, u, v2, tint, light, overlay, -1, 0, 0);
        }
    }

    static void emitConnectedFluid(Matrix4f m, VertexConsumer vc, Direction dir,
                                   float u, float v, float u2, float v2, float du, double in, double in2,
                                   int tint, int light, int overlay) {
        switch (dir) {
            case DOWN -> {
                quad(m, vc, in2, in2, in, u, v, in2, in2, in2, u2, v, in2, 0, in2, u2, v + du, in2, 0, in, u, v + du, tint, light, overlay, -1, 0, 0);
                quad(m, vc, in, 0, in, u, v + du, in, 0, in2, u2, v + du, in, in2, in2, u2, v, in, in2, in, u, v, tint, light, overlay, 1, 0, 0);
                quad(m, vc, in, 0, in2, u, v + du, in2, 0, in2, u2, v + du, in2, in2, in2, u2, v, in, in2, in2, u, v, tint, light, overlay, 0, 0, -1);
                quad(m, vc, in, in2, in, u, v, in2, in2, in, u2, v, in2, 0, in, u2, v + du, in, 0, in, u, v + du, tint, light, overlay, 0, 0, 1);
            }
            case UP -> {
                quad(m, vc, in2, 1, in, u, v + du, in2, 1, in2, u2, v + du, in2, in, in2, u2, v, in2, in, in, u, v, tint, light, overlay, -1, 0, 0);
                quad(m, vc, in, in, in, u, v, in, in, in2, u2, v, in, 1, in2, u2, v + du, in, 1, in, u, v + du, tint, light, overlay, 1, 0, 0);
                quad(m, vc, in, in, in2, u, v, in2, in, in2, u2, v, in2, 1, in2, u2, v + du, in, 1, in2, u, v + du, tint, light, overlay, 0, 0, -1);
                quad(m, vc, in, 1, in, u, v + du, in2, 1, in, u2, v + du, in2, in, in, u2, v, in, in, in, u, v, tint, light, overlay, 0, 0, 1);
            }
            case NORTH -> {
                quad(m, vc, in2, in2, 0, u, v2, in2, in2, in2, u + du, v2, in2, in, in2, u + du, v, in2, in, 0, u, v, tint, light, overlay, -1, 0, 0);
                quad(m, vc, in, in, 0, u, v, in, in, in2, u + du, v, in, in2, in2, u + du, v2, in, in2, 0, u, v2, tint, light, overlay, 1, 0, 0);
                quad(m, vc, in2, in, 0, u, v2, in2, in, in2, u + du, v2, in, in, in2, u + du, v, in, in, 0, u, v, tint, light, overlay, 0, 1, 0);
                quad(m, vc, in, in2, 0, u, v, in, in2, in2, u + du, v, in2, in2, in2, u + du, v2, in2, in2, 0, u, v2, tint, light, overlay, 0, -1, 0);
            }
            case SOUTH -> {
                quad(m, vc, in2, in, 1, u, v, in2, in, in, u + du, v, in2, in2, in, u + du, v2, in2, in2, 1, u, v2, tint, light, overlay, -1, 0, 0);
                quad(m, vc, in, in2, 1, u, v2, in, in2, in, u + du, v2, in, in, in, u + du, v, in, in, 1, u, v, tint, light, overlay, 1, 0, 0);
                quad(m, vc, in, in, 1, u, v, in, in, in, u + du, v, in2, in, in, u + du, v2, in2, in, 1, u, v2, tint, light, overlay, 0, 1, 0);
                quad(m, vc, in2, in2, 1, u, v2, in2, in2, in, u + du, v2, in, in2, in, u + du, v, in, in2, 1, u, v, tint, light, overlay, 0, -1, 0);
            }
            case EAST -> {
                quad(m, vc, 1, in, in, u, v, in, in, in, u + du, v, in, in2, in, u + du, v2, 1, in2, in, u, v2, tint, light, overlay, 0, 0, 1);
                quad(m, vc, 1, in2, in2, u, v2, in, in2, in2, u + du, v2, in, in, in2, u + du, v, 1, in, in2, u, v, tint, light, overlay, 0, 0, -1);
                quad(m, vc, 1, in, in2, u, v2, in, in, in2, u + du, v2, in, in, in, u + du, v, 1, in, in, u, v, tint, light, overlay, 0, 1, 0);
                quad(m, vc, 1, in2, in, u, v, in, in2, in, u + du, v, in, in2, in2, u + du, v2, 1, in2, in2, u, v2, tint, light, overlay, 0, -1, 0);
            }
            case WEST -> {
                quad(m, vc, 0, in2, in, u, v2, in2, in2, in, u + du, v2, in2, in, in, u + du, v, 0, in, in, u, v, tint, light, overlay, 0, 0, 1);
                quad(m, vc, 0, in, in2, u, v, in2, in, in2, u + du, v, in2, in2, in2, u + du, v2, 0, in2, in2, u, v2, tint, light, overlay, 0, 0, -1);
                quad(m, vc, 0, in, in, u, v, in2, in, in, u + du, v, in2, in, in2, u + du, v2, 0, in, in2, u, v2, tint, light, overlay, 0, 1, 0);
                quad(m, vc, 0, in2, in2, u, v2, in2, in2, in2, u + du, v2, in2, in2, in, u + du, v, 0, in2, in, u, v, tint, light, overlay, 0, -1, 0);
            }
        }
    }

    private static void quad(Matrix4f pose, VertexConsumer vc,
                             double x1, double y1, double z1, float u1, float v1,
                             double x2, double y2, double z2, float u2_, float v2_,
                             double x3, double y3, double z3, float u3, float v3,
                             double x4, double y4, double z4, float u4, float v4,
                             int rgba, int light, int overlay,
                             float nx, float ny, float nz) {
        vc.addVertex(pose, (float) x1, (float) y1, (float) z1).setColor(rgba).setUv(u1, v1).setOverlay(overlay).setLight(light).setNormal(nx, ny, nz);
        vc.addVertex(pose, (float) x2, (float) y2, (float) z2).setColor(rgba).setUv(u2_, v2_).setOverlay(overlay).setLight(light).setNormal(nx, ny, nz);
        vc.addVertex(pose, (float) x3, (float) y3, (float) z3).setColor(rgba).setUv(u3, v3).setOverlay(overlay).setLight(light).setNormal(nx, ny, nz);
        vc.addVertex(pose, (float) x4, (float) y4, (float) z4).setColor(rgba).setUv(u4, v4).setOverlay(overlay).setLight(light).setNormal(nx, ny, nz);
    }

    private TextureAtlasSprite stillSpriteFor(Fluid fluid) {
        return sprites.get(new SpriteId(TextureAtlas.LOCATION_BLOCKS, stillTextureId(fluid)));
    }

    /** Neutral metal sprite for an empty pipe's shell. */
    private TextureAtlasSprite shellSprite() {
        return sprites.get(new SpriteId(TextureAtlas.LOCATION_BLOCKS, Identifier.withDefaultNamespace("block/iron_block")));
    }

    private static Identifier stillTextureId(Fluid fluid) {
        if (fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER)
            return Identifier.withDefaultNamespace("block/water_still");
        if (fluid == Fluids.LAVA || fluid == Fluids.FLOWING_LAVA)
            return Identifier.withDefaultNamespace("block/lava_still");
        // Hot fluids (steam, sodium) read better against the lava still; everything else uses water still.
        return fluid.getFluidType().getTemperature() > 500
                ? Identifier.withDefaultNamespace("block/lava_still")
                : Identifier.withDefaultNamespace("block/water_still");
    }

    /**
     * Approximate ARGB tint per fluid. Vanilla water keeps its blue; otherwise tint by temperature so
     * hot gases read warm and cold gases read cool, without hard-coding every ReactorCraft fluid.
     */
    private static int fluidTint(Fluid f) {
        if (f == Fluids.WATER || f == Fluids.FLOWING_WATER) return 0xFF3050E0;
        if (f == Fluids.LAVA || f == Fluids.FLOWING_LAVA) return 0xFFE04010;
        int temp = f.getFluidType().getTemperature();
        if (temp > 800) return 0xC0E0E0E0;   // steam / very hot — pale grey
        if (temp > 500) return 0xFFD0D050;    // hot (sodium) — yellow
        return 0xC020A0C0;                    // generic cool gas/liquid — teal, semi-transparent
    }
}
