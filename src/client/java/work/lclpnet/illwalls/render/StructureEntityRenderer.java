package work.lclpnet.illwalls.render;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.*;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import work.lclpnet.illwalls.entity.StructureEntity;

public class StructureEntityRenderer extends EntityRenderer<StructureEntity, StructureEntityRenderState> implements RenderLayerGetter {

    private final StructureEntityBatchRenderer batchRenderer;

    public StructureEntityRenderer(EntityRendererFactory.Context context) {
        super(context);

        EntityRenderManager renderDispatcher = context.getRenderDispatcher();

        batchRenderer = ((IllusoryBatchRendererProvider) renderDispatcher).illwalls$getStructureEntityRenderer();
    }

    @Override
    public StructureEntityRenderState createRenderState() {
        return new StructureEntityRenderState();
    }

    @Override
    public void updateRenderState(StructureEntity entity, StructureEntityRenderState state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);

        state.fading = entity.isFading();
        state.fadeStartMs = entity.getFadeStartMs();
        state.fadeMode = entity.getFadeMode();
        state.fadingFrom = entity.getFadingFrom();

        if (state.fadingFrom != null) {
            state.blockLight = this.getBlockLight(entity, state.fadingFrom);
            state.skyLight = this.getSkyLight(entity, state.fadingFrom);
        }

        state.structure = entity.getStructureContainer().getWrapper();
    }

    @Override
    public void render(StructureEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        super.render(state, matrices, queue, cameraState);

        batchRenderer.submit(matrices, state);
    }

    @Override
    public RenderLayer getRenderLayer(BlockState state, float alpha) {
        if (alpha >= 1.0f) {
            return RenderLayers.getEntityBlockLayer(state);
        }

        return TexturedRenderLayers.getItemEntityTranslucentCull();
    }

    @Override
    protected boolean canBeCulled(StructureEntity entity) {
        return false;  // ignore camera frustum culling for now
    }
}
