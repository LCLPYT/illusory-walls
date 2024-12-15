package work.lclpnet.illwalls.render;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.ColorHelper;
import work.lclpnet.illwalls.entity.IllusoryWallEntity;
import work.lclpnet.illwalls.mixin.client.WorldRendererAccessor;

public class IllusoryWallEntityRenderer extends EntityRenderer<IllusoryWallEntity, IllusoryWallRenderState> implements RenderLayerGetter {

    private final StructureRenderer structureRenderer;

    public IllusoryWallEntityRenderer(EntityRendererFactory.Context context) {
        super(context);

        var blockRenderManager = context.getBlockRenderManager();
        var blockIllusionRenderManager = new BlockIllusionRenderManager(blockRenderManager, this);
        this.structureRenderer = new CullStructureRenderer(blockIllusionRenderManager);
    }

    @Override
    public IllusoryWallRenderState createRenderState() {
        return new IllusoryWallRenderState();
    }

    @Override
    public void updateRenderState(IllusoryWallEntity entity, IllusoryWallRenderState state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);

        state.outlineColor = entity.getOutlineColor();
        state.structure = entity.getStructureContainer().getWrapper();
    }

    @Override
    public void render(IllusoryWallRenderState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        WorldRenderer worldRenderer = MinecraftClient.getInstance().worldRenderer;
        BufferBuilderStorage bufferBuilders = ((WorldRendererAccessor) worldRenderer).getBufferBuilders();
        OutlineVertexConsumerProvider outlineVertexConsumerProvider = bufferBuilders.getOutlineVertexConsumers();

        vertexConsumers = outlineVertexConsumerProvider;

        outlineVertexConsumerProvider.setColor(
                ColorHelper.getRed(state.outlineColor),
                ColorHelper.getGreen(state.outlineColor),
                ColorHelper.getBlue(state.outlineColor),
                ColorHelper.getAlpha(state.outlineColor));

        // override the outline rendering so the outline post processor is guaranteed to always render
        ((OutlineRenderOverride) worldRenderer).illwalls$markOverridden();  // TODO check if this is still needed

        structureRenderer.render(state.structure, state.x, state.y, state.z, matrices, vertexConsumers, light, 1F);
    }

    @SuppressWarnings("deprecation")
    @Override
    public RenderLayer getRenderLayer(BlockState state, float alpha) {
        return RenderLayer.getOutline(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
    }

    @Override
    protected boolean canBeCulled(IllusoryWallEntity entity) {
        return false;  // ignore camera frustum culling for now
    }
}
