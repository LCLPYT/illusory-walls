package work.lclpnet.illwalls.render;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;

public class AlphaBlockModelRenderer {

    private static final Direction[] DIRECTIONS = Direction.values();

    public static void render(MatrixStack.Entry entry, VertexConsumer vertexConsumer, @Nullable BlockState state, BakedModel bakedModel, float red, float green, float blue, float alpha, int light, int overlay) {
        Random random = Random.create();
        long seed = 42L;

        for (Direction direction : DIRECTIONS) {
            random.setSeed(seed);
            AlphaBlockModelRenderer.renderQuads(entry, vertexConsumer, red, green, blue, alpha, bakedModel.getQuads(state, direction, random), light, overlay);
        }

        random.setSeed(seed);
        AlphaBlockModelRenderer.renderQuads(entry, vertexConsumer, red, green, blue, alpha, bakedModel.getQuads(state, null, random), light, overlay);
    }

    public static void renderQuads(MatrixStack.Entry entry, VertexConsumer vertexConsumer, float red, float green, float blue, float alpha, List<BakedQuad> quads, int light, int overlay) {
        float r;
        float g;
        float b;

        for (BakedQuad bakedQuad : quads) {
            if (bakedQuad.hasColor()) {
                b = MathHelper.clamp(red, 0.0f, 1.0f);
                g = MathHelper.clamp(green, 0.0f, 1.0f);
                r = MathHelper.clamp(blue, 0.0f, 1.0f);
            } else {
                b = 1.0f;
                g = 1.0f;
                r = 1.0f;
            }

            AlphaBlockModelRenderer.alphaQuad(vertexConsumer, entry, bakedQuad, b, g, r, alpha, light, overlay);
        }
    }

    public static void alphaQuad(VertexConsumer vertexConsumer, MatrixStack.Entry matrixEntry, BakedQuad quad, float red, float green, float blue, float alpha, int light, int overlay) {
        alphaQuad(vertexConsumer, matrixEntry, quad, new float[]{1.0f, 1.0f, 1.0f, 1.0f}, red, green, blue, alpha, new int[]{light, light, light, light}, overlay);
    }

    public static void alphaQuad(VertexConsumer vertexConsumer, MatrixStack.Entry matrixEntry, BakedQuad quad, float[] brightnesses, float red, float green, float blue, float alpha, int[] lights, int overlay) {
        final float[] brightness = new float[] {brightnesses[0], brightnesses[1], brightnesses[2], brightnesses[3]};
        final int[] lightValues = new int[] {lights[0], lights[1], lights[2], lights[3]};
        final int[] vertexData = quad.getVertexData();
        final Matrix4f posMatrix = matrixEntry.getPositionMatrix();

        final Vec3i dir = quad.getFace().getVector();
        final Vector3f normal = matrixEntry.getNormalMatrix().transform(new Vector3f(dir.getX(), dir.getY(), dir.getZ()));

        final int bytes = vertexData.length / 8;

        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            ByteBuffer buf = memoryStack.malloc(VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL.getVertexSizeByte());
            IntBuffer intBuf = buf.asIntBuffer();

            float r, g, b, u, v;

            for (int i = 0; i < bytes; ++i) {
                intBuf.clear();
                intBuf.put(vertexData, i * 8, 8);

                r = brightness[i] * red;
                g = brightness[i] * green;
                b = brightness[i] * blue;

                u = buf.getFloat(16);
                v = buf.getFloat(20);

                float relX = buf.getFloat(0);
                float relY = buf.getFloat(4);
                float relZ = buf.getFloat(8);

                var pos = posMatrix.transform(new Vector4f(relX, relY, relZ, 1.0f));

                vertexConsumer.vertex(
                        pos.x(), pos.y(), pos.z(),
                        r, g, b, alpha,
                        u, v,
                        overlay, lightValues[i],
                        normal.x(), normal.y(), normal.z()
                );
            }
        }
    }
}
