package work.lclpnet.illwalls.render;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import work.lclpnet.illwalls.entity.IllusoryWallEntity;
import work.lclpnet.illwalls.entity.StructureEntity;

public class StructureEntityRenderer extends EntityRenderer<StructureEntity, StructureEntityRenderState> implements RenderLayerGetter {

    private final StructureRenderer structureRenderer;

    public StructureEntityRenderer(EntityRendererFactory.Context context) {
        super(context);

        var blockRenderManager = context.getBlockRenderManager();
        var blockIllusionRenderManager = new BlockIllusionRenderManager(blockRenderManager, this);
        this.structureRenderer = new CullStructureRenderer(blockIllusionRenderManager);
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
    public void render(StructureEntityRenderState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        super.render(state, matrices, vertexConsumers, light);

        float alpha = 1F;

        if (state.fading) {
            long start = state.fadeStartMs;

            if (start == 0L) {
                // in case the render is invoked between the set of fadeStartMs
                start = System.currentTimeMillis();
            }

            long now = System.currentTimeMillis();

            alpha = (now - start) / (float) IllusoryWallEntity.FADE_DURATION_MS;

            if (state.fadeMode != StructureEntity.FADE_IN) {
                alpha = 1F - alpha;
            }

            alpha = MathHelper.clamp(alpha, 0F, 1F);
        }

        BlockPos fadingFrom = state.fadingFrom;

        if (fadingFrom != null) {
            light = LightmapTextureManager.pack(state.blockLight, state.skyLight);
        }

        structureRenderer.render(state.structure, state.x, state.y, state.z, matrices, vertexConsumers, light, alpha);
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
