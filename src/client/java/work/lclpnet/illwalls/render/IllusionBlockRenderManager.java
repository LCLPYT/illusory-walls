package work.lclpnet.illwalls.render;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import work.lclpnet.illwalls.mixin.client.BlockRenderManagerAccessor;

public class IllusionBlockRenderManager {

    private final BlockRenderManager blockRenderManager;

    public IllusionBlockRenderManager(BlockRenderManager blockRenderManager) {
        this.blockRenderManager = blockRenderManager;
    }

    public void renderBlockAsEntity(BlockState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        BlockRenderType blockRenderType = state.getRenderType();
        if (blockRenderType == BlockRenderType.INVISIBLE) return;

        switch (blockRenderType) {
            case MODEL -> {
                BakedModel bakedModel = blockRenderManager.getModel(state);
                var blockColors = ((BlockRenderManagerAccessor) blockRenderManager).getBlockColors();

                int i = blockColors.getColor(state, null, null, 0);
                float r = (float) (i >> 16 & 0xFF) / 255.0f;
                float g = (float) (i >> 8 & 0xFF) / 255.0f;
                float b = (float) (i & 0xFF) / 255.0f;
                float alpha = 0.5f;

                RenderLayer renderLayer;
                if (alpha >= 1.0f) {
                    renderLayer = RenderLayers.getEntityBlockLayer(state, false);

                    var buffer = vertexConsumers.getBuffer(renderLayer);
                    this.blockRenderManager.getModelRenderer().render(matrices.peek(), buffer, state, bakedModel, r, g, b, light, overlay);
                } else {
                    renderLayer = MinecraftClient.isFabulousGraphicsOrBetter()
                            ? TexturedRenderLayers.getItemEntityTranslucentCull()
                            : TexturedRenderLayers.getEntityTranslucentCull();

                    var buffer = vertexConsumers.getBuffer(renderLayer);
                    AlphaBlockModelRenderer.render(matrices.peek(), buffer, state, bakedModel, r, g, b, alpha, light, overlay);
                }

            }
            case ENTITYBLOCK_ANIMATED -> {
                var renderer = ((BlockRenderManagerAccessor) blockRenderManager).getBuiltinModelItemRenderer();
                var itemStack = new ItemStack(state.getBlock());

                renderer.render(itemStack, ModelTransformationMode.NONE, matrices, vertexConsumers, light, overlay);
            }
        }
    }
}
