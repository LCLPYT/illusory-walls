package work.lclpnet.illwalls.render;

import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import work.lclpnet.illwalls.entity.IllusoryWallEntity;

public class IllusoryWallEntityRenderer extends EntityRenderer<IllusoryWallEntity> {

    private final StructureRenderer structureRenderer;

    public IllusoryWallEntityRenderer(EntityRendererFactory.Context context) {
        super(context);

        var blockRenderManager = context.getBlockRenderManager();
        var blockIllusionRenderManager = new BlockIllusionRenderManager(blockRenderManager);
        this.structureRenderer = new CullStructureRenderer(blockIllusionRenderManager);
    }

    @SuppressWarnings("deprecation")
    @Override
    public Identifier getTexture(IllusoryWallEntity entity) {
        return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
    }

    @Override
    public boolean shouldRender(IllusoryWallEntity entity, Frustum frustum, double x, double y, double z) {  // TODO remove
        return super.shouldRender(entity, frustum, x, y, z);
    }

    @Override
    public void render(IllusoryWallEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);

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

        var structure = entity.getStructure();

        structureRenderer.render(structure, entity.getPos(), matrices, vertexConsumers, light, alpha);
    }
}
