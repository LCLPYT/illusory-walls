package work.lclpnet.illwalls.render;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.CameraRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import work.lclpnet.illwalls.entity.StructureEntity;

public class StructureEntityRenderer extends EntityRenderer<StructureEntity, StructureEntityRenderState> implements RenderLayerGetter {

    private final StructureEntityBatchRenderer batchRenderer;

    public StructureEntityRenderer(EntityRendererProvider.Context context) {
        super(context);

        EntityRenderDispatcher renderDispatcher = context.getEntityRenderDispatcher();

        batchRenderer = ((IllusoryBatchRendererProvider) renderDispatcher).illwalls$getStructureEntityRenderer();
    }

    @Override
    public StructureEntityRenderState createRenderState() {
        return new StructureEntityRenderState();
    }

    @Override
    public void extractRenderState(StructureEntity entity, StructureEntityRenderState state, float tickDelta) {
        super.extractRenderState(entity, state, tickDelta);

        state.fading = entity.isFading();
        state.fadeStartMs = entity.getFadeStartMs();
        state.fadeMode = entity.getFadeMode();
        state.fadingFrom = entity.getFadingFrom();

        if (state.fadingFrom != null) {
            state.blockLight = this.getBlockLightLevel(entity, state.fadingFrom);
            state.skyLight = this.getSkyLightLevel(entity, state.fadingFrom);
        }

        state.structure = entity.getStructureContainer().getWrapper();
    }

    @Override
    public void submit(StructureEntityRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        super.submit(state, matrices, queue, cameraState);

        batchRenderer.submit(matrices, state);
    }

    @Override
    public RenderType getRenderLayer(BlockState state, float alpha) {
        if (alpha >= 1.0f) {
            return ItemBlockRenderTypes.getRenderType(state);
        }

        return Sheets.translucentBlockItemSheet();
    }

    @Override
    protected boolean affectedByCulling(StructureEntity entity) {
        return false;  // ignore camera frustum culling for now
    }
}
