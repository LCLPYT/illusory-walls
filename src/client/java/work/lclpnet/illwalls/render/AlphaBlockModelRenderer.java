package work.lclpnet.illwalls.render;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

import java.util.List;

// BlockModelRenderer with alpha support
public class AlphaBlockModelRenderer {

    private static final Direction[] DIRECTIONS = Direction.values();
    private static final long SEED = 42L;
    private final Random random = Random.create(SEED);

    public void render(MatrixStack.Entry entry, VertexConsumer vertexConsumer, BlockStateModel model, float red, float green, float blue, float alpha, int light, int overlay) {
        random.setSeed(SEED);

        for (BlockModelPart part : model.getParts(random)) {
            for (Direction side : DIRECTIONS) {
                renderQuads(entry, vertexConsumer, red, green, blue, alpha, part.getQuads(side), light, overlay);
            }

            renderQuads(entry, vertexConsumer, red, green, blue, alpha, part.getQuads(null), light, overlay);
        }
    }

    public void renderWithCulling(MatrixStack.Entry entry, VertexConsumer vertexConsumer, BlockState state, CullInfo cullInfo, BlockStateModel model, float red, float green, float blue, float alpha, int light, int overlay) {
        final var pos = cullInfo.pos();
        final var adjPos = new BlockPos.Mutable();
        final var view = cullInfo.blockView();

        random.setSeed(SEED);

        for (BlockModelPart part : model.getParts(random)) {
            for (Direction side : DIRECTIONS) {
                adjPos.set(pos, side);
                if (!Block.shouldDrawSide(state, view.getBlockState(pos.offset(side)), side)) continue;

                renderQuads(entry, vertexConsumer, red, green, blue, alpha, part.getQuads(side), light, overlay);
            }

            renderQuads(entry, vertexConsumer, red, green, blue, alpha, part.getQuads(null), light, overlay);
        }
    }

    public static void renderQuads(MatrixStack.Entry entry, VertexConsumer vertexConsumer,
                                   final float red, final float green, final float blue, final float alpha,
                                   List<BakedQuad> quads, int light, int overlay) {
        float r;
        float g;
        float b;

        for (BakedQuad bakedQuad : quads) {
            if (bakedQuad.hasTint()) {
                b = MathHelper.clamp(red, 0.0f, 1.0f);
                g = MathHelper.clamp(green, 0.0f, 1.0f);
                r = MathHelper.clamp(blue, 0.0f, 1.0f);
            } else {
                b = 1.0f;
                g = 1.0f;
                r = 1.0f;
            }

            vertexConsumer.quad(entry, bakedQuad, r, g, b, alpha, light, overlay);
        }
    }
}
