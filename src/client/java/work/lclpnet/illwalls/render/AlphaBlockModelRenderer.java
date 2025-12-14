package work.lclpnet.illwalls.render;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import java.util.List;

// BlockModelRenderer with alpha support
public class AlphaBlockModelRenderer {

    private static final Direction[] DIRECTIONS = Direction.values();
    private static final long SEED = 42L;
    private final RandomSource random = RandomSource.create(SEED);

    public void render(PoseStack.Pose entry, VertexConsumer vertexConsumer, BlockStateModel model, float red, float green, float blue, float alpha, int light, int overlay) {
        random.setSeed(SEED);

        for (BlockModelPart part : model.collectParts(random)) {
            for (Direction side : DIRECTIONS) {
                renderQuads(entry, vertexConsumer, red, green, blue, alpha, part.getQuads(side), light, overlay);
            }

            renderQuads(entry, vertexConsumer, red, green, blue, alpha, part.getQuads(null), light, overlay);
        }
    }

    public void renderWithCulling(PoseStack.Pose entry, VertexConsumer vertexConsumer, BlockState state, CullInfo cullInfo, BlockStateModel model, float red, float green, float blue, float alpha, int light, int overlay) {
        final var pos = cullInfo.pos();
        final var adjPos = new BlockPos.MutableBlockPos();
        final var view = cullInfo.blockView();

        random.setSeed(SEED);

        for (BlockModelPart part : model.collectParts(random)) {
            for (Direction side : DIRECTIONS) {
                adjPos.setWithOffset(pos, side);
                if (!Block.shouldRenderFace(state, view.getBlockState(pos.relative(side)), side)) continue;

                renderQuads(entry, vertexConsumer, red, green, blue, alpha, part.getQuads(side), light, overlay);
            }

            renderQuads(entry, vertexConsumer, red, green, blue, alpha, part.getQuads(null), light, overlay);
        }
    }

    public static void renderQuads(PoseStack.Pose entry, VertexConsumer vertexConsumer,
                                   final float red, final float green, final float blue, final float alpha,
                                   List<BakedQuad> quads, int light, int overlay) {
        float r;
        float g;
        float b;

        for (BakedQuad bakedQuad : quads) {
            if (bakedQuad.isTinted()) {
                b = Mth.clamp(red, 0.0f, 1.0f);
                g = Mth.clamp(green, 0.0f, 1.0f);
                r = Mth.clamp(blue, 0.0f, 1.0f);
            } else {
                b = 1.0f;
                g = 1.0f;
                r = 1.0f;
            }

            vertexConsumer.putBulkData(entry, bakedQuad, r, g, b, alpha, light, overlay);
        }
    }
}
