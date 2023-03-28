package work.lclpnet.illwalls.render;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.DisplayEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import work.lclpnet.illwalls.entity.IllusoryWallEntity;

public class BlockIllusionEntityRenderer extends DisplayEntityRenderer<IllusoryWallEntity> {

    private final BlockIllusionRenderManager blockIllusionRenderManager;

    public BlockIllusionEntityRenderer(EntityRendererFactory.Context context) {
        super(context);

        var blockRenderManager = context.getBlockRenderManager();
        this.blockIllusionRenderManager = new BlockIllusionRenderManager(blockRenderManager);
    }

    @Override
    protected void render(IllusoryWallEntity entity, MatrixStack matrices, VertexConsumerProvider vertices, int light, float delta) {
        float alpha = 1F;

        if (entity.isFading()) {
            long start = entity.getFadeStartMs();

            if (start == 0L) {
                // in case the render is invoked between the set of fadeStartMs
                start = System.currentTimeMillis();
            }

            long now = System.currentTimeMillis();

            alpha = 1F - (now - start) / (float) IllusoryWallEntity.FADE_DURATION_MS;
            alpha = MathHelper.clamp(alpha, 0F, 1F);
        }

        if (alpha > 0F) {
            blockIllusionRenderManager.renderBlockAsEntity(entity.getBlockState(), matrices, vertices, light, OverlayTexture.DEFAULT_UV, alpha);
        }
    }
}
