package work.lclpnet.illwalls.render;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.color.block.BlockColors;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import com.mojang.blaze3d.vertex.PoseStack;
import work.lclpnet.illwalls.mixin.client.BlockRenderDispatcherAccessor;

import javax.annotation.Nullable;

public class BlockIllusionRenderManager {

    private final BlockRenderDispatcher blockRenderManager;
    private final RenderLayerGetter renderLayerGetter;
    private final AlphaBlockModelRenderer blockModelRenderer = new AlphaBlockModelRenderer();

    public BlockIllusionRenderManager(BlockRenderDispatcher blockRenderManager, RenderLayerGetter renderLayerGetter) {
        this.blockRenderManager = blockRenderManager;
        this.renderLayerGetter = renderLayerGetter;
    }

    // net.minecraft.client.render.block.BlockRenderManager.renderBlockAsEntity with alpha support
    public void renderBlockAsEntity(BlockState state, @Nullable CullInfo cullInfo, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay, float alpha) {
        RenderShape renderType = state.getRenderShape();

        if (renderType != RenderShape.MODEL) return;

        BlockStateModel bakedModel = blockRenderManager.getBlockModel(state);
        BlockColors blockColors = ((BlockRenderDispatcherAccessor) blockRenderManager).getBlockColors();

        int i = blockColors.getColor(state, null, null, 0);
        float r = (float) (i >> 16 & 0xFF) / 255.0f;
        float g = (float) (i >> 8 & 0xFF) / 255.0f;
        float b = (float) (i & 0xFF) / 255.0f;

        RenderType renderLayer = renderLayerGetter.getRenderLayer(state, alpha);
        VertexConsumer buffer = vertexConsumers.getBuffer(renderLayer);

        if (cullInfo != null) {
            blockModelRenderer.renderWithCulling(matrices.last(), buffer, state, cullInfo, bakedModel, r, g, b, alpha, light, overlay);
        } else {
            blockModelRenderer.render(matrices.last(), buffer, bakedModel, r, g, b, alpha, light, overlay);
        }
    }
}
