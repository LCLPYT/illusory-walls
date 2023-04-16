package work.lclpnet.illwalls.render;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import work.lclpnet.illwalls.entity.IllusoryWallEntity;
import work.lclpnet.illwalls.entity.StructureEntity;
import work.lclpnet.illwalls.struct.FabricStructureWrapper;

public class StructureEntityRenderer extends EntityRenderer<StructureEntity> implements RenderLayerGetter {

    private final StructureRenderer structureRenderer;

    public StructureEntityRenderer(EntityRendererFactory.Context context) {
        super(context);

        var blockRenderManager = context.getBlockRenderManager();
        var blockIllusionRenderManager = new BlockIllusionRenderManager(blockRenderManager, this);
        this.structureRenderer = new CullStructureRenderer(blockIllusionRenderManager);
    }

    @SuppressWarnings("deprecation")
    @Override
    public Identifier getTexture(StructureEntity entity) {
        return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
    }

    @Override
    public void render(StructureEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
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

        BlockPos fadingFrom = entity.getFadingFrom();
        if (fadingFrom != null) {
            int blockLight = this.getBlockLight(entity, fadingFrom);
            int skyLight = this.getSkyLight(entity, fadingFrom);
            light = LightmapTextureManager.pack(blockLight, skyLight);
        }

        FabricStructureWrapper structure = entity.getStructureContainer().getWrapper();

        structureRenderer.render(structure, entity.getPos(), matrices, vertexConsumers, light, alpha);
    }

    @Override
    public RenderLayer getRenderLayer(BlockState state, float alpha) {
        if (alpha >= 1.0f) {
            return RenderLayers.getEntityBlockLayer(state, false);
        }

        return MinecraftClient.isFabulousGraphicsOrBetter()
                ? TexturedRenderLayers.getItemEntityTranslucentCull()
                : TexturedRenderLayers.getEntityTranslucentCull();
    }
}
