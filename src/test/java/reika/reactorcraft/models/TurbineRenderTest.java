package reika.reactorcraft.models;

import java.util.ArrayList;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.Direction;
import reika.reactorcraft.renders.TurbineHousingGeometry;
import static org.junit.jupiter.api.Assertions.*;

class TurbineRenderTest {
    @Test void wornBladesUseOriginalAngularSpacingAtEveryStage() {
        for (int stage = 0; stage <= ModelTurbine.MAX_STAGE; stage++) {
            var model = new ModelTurbine(ModelTurbine.createLayer(stage).bakeRoot(), stage);
            for (int damage : new int[]{0, 1, 3, Integer.MAX_VALUE}) {
                var vertices = new Vertices(); var pose = new PoseStack();
                model.renderAll(pose, vertices, 0, 0, damage);
                long spacing = (long)ModelTurbine.angularSep(stage) * (damage + 1L);
                assertEquals(48 + 48 * ((359 / spacing) + 1), vertices.positions.size());
                assertTrue(pose.last().pose().equals(new org.joml.Matrix4f(), 0.00001F), "model must restore the caller's pose");
            }
        }
    }
    @Test void bigTurbineWearKeepsItsStationaryHousingAndStageZeroCoverPlate() {
        for (int stage = 0; stage <= ModelBigTurbine.MAX_STAGE; stage++) {
            var model = new ModelBigTurbine(ModelBigTurbine.createLayer(stage).bakeRoot(), stage);
            var healthy = new Vertices(); var worn = new Vertices();
            model.renderAll(new PoseStack(), healthy, 0, 0, 0);
            model.renderAll(new PoseStack(), worn, 0, 45, 1);
            int bladeCount = (359 / ModelBigTurbine.angularSep(stage)) + 1;
            int wornCount = (359 / (2 * ModelBigTurbine.angularSep(stage))) + 1;
            int housing = ModelBigTurbine.housingSegments(stage) * (stage == 0 ? 72 : 48);
            assertEquals(48 + 48 * bladeCount + housing, healthy.positions.size());
            assertEquals(48 + 48 * wornCount + housing, worn.positions.size());
            assertEquals(healthy.positions.subList(48 + 48 * bladeCount, healthy.positions.size()),
                    worn.positions.subList(48 + 48 * wornCount, worn.positions.size()), "housing must not spin with the wheel");
            assertNotEquals(healthy.positions.subList(0,48), worn.positions.subList(0,48), "shafts rotate with the wheel");
        }
    }
    @Test void unformedCapsFillTheBlockWithSixTexturedFacesInEveryOrientation() {
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            var vertices = new Vertices(); var pose = new PoseStack();
            TurbineHousingGeometry.render(pose, vertices, 0, facing);
            assertEquals(24, vertices.positions.size()); assertEquals(24, vertices.uvs.size());
            for (var point : vertices.positions) {
                assertTrue(point.x >= -0.00001 && point.x <= 1.00001);
                assertTrue(point.y >= -0.00001 && point.y <= 1.00001);
                assertTrue(point.z >= -0.00001 && point.z <= 1.00001);
            }
            for (var uv : vertices.uvs) assertTrue(uv[0] >= 0 && uv[0] <= 1 && uv[1] >= 0 && uv[1] <= 1);
            assertTrue(pose.last().pose().equals(new org.joml.Matrix4f(), 0.00001F));
        }
    }
    private static class Vertices implements VertexConsumer {
        final ArrayList<Vector3f> positions = new ArrayList<>();
        final ArrayList<float[]> uvs = new ArrayList<>();
        public VertexConsumer addVertex(float x,float y,float z) { positions.add(new Vector3f(x,y,z)); return this; }
        public VertexConsumer setColor(int r,int g,int b,int a) { return this; }
        public VertexConsumer setColor(int color) { return this; }
        public VertexConsumer setUv(float u,float v) { uvs.add(new float[]{u,v}); return this; }
        public VertexConsumer setUv1(int u,int v) { return this; }
        public VertexConsumer setUv2(int u,int v) { return this; }
        public VertexConsumer setUv3(float u,float v) { return this; }
        public VertexConsumer setNormal(float x,float y,float z) { return this; }
        public VertexConsumer setLineWidth(float width) { return this; }
    }
}
