package work.lclpnet.illwalls.render;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
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
    public void render(IllusoryWallRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        WorldRenderer worldRenderer = MinecraftClient.getInstance().worldRenderer;
        BufferBuilderStorage bufferBuilders = ((WorldRendererAccessor) worldRenderer).getBufferBuilders();
        OutlineVertexConsumerProvider outlineVertexConsumerProvider = bufferBuilders.getOutlineVertexConsumers();

        outlineVertexConsumerProvider.setColor(state.outlineColor);

        structureRenderer.render(state.structure, state.x, state.y, state.z, matrices, outlineVertexConsumerProvider, state.light, 1F);
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
