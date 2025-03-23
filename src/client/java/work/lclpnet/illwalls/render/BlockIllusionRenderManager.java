package work.lclpnet.illwalls.render;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.util.math.MatrixStack;
import work.lclpnet.illwalls.mixin.client.BlockRenderManagerAccessor;

import javax.annotation.Nullable;

public class BlockIllusionRenderManager {

    private final BlockRenderManager blockRenderManager;
    private final RenderLayerGetter renderLayerGetter;
    private final AlphaBlockModelRenderer blockModelRenderer = new AlphaBlockModelRenderer();

    public BlockIllusionRenderManager(BlockRenderManager blockRenderManager, RenderLayerGetter renderLayerGetter) {
        this.blockRenderManager = blockRenderManager;
        this.renderLayerGetter = renderLayerGetter;
    }

    // net.minecraft.client.render.block.BlockRenderManager.renderBlockAsEntity with alpha support
    public void renderBlockAsEntity(BlockState state, @Nullable CullInfo cullInfo, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, float alpha) {
        BlockRenderType renderType = state.getRenderType();

        if (renderType != BlockRenderType.MODEL) return;

        BlockStateModel bakedModel = blockRenderManager.getModel(state);
        BlockColors blockColors = ((BlockRenderManagerAccessor) blockRenderManager).getBlockColors();

        int i = blockColors.getColor(state, null, null, 0);
        float r = (float) (i >> 16 & 0xFF) / 255.0f;
        float g = (float) (i >> 8 & 0xFF) / 255.0f;
        float b = (float) (i & 0xFF) / 255.0f;

        RenderLayer renderLayer = renderLayerGetter.getRenderLayer(state, alpha);
        VertexConsumer buffer = vertexConsumers.getBuffer(renderLayer);

        if (cullInfo != null) {
            blockModelRenderer.renderWithCulling(matrices.peek(), buffer, state, cullInfo, bakedModel, r, g, b, alpha, light, overlay);
        } else {
            blockModelRenderer.render(matrices.peek(), buffer, bakedModel, r, g, b, alpha, light, overlay);
        }
    }
}
