package work.lclpnet.illwalls.render;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import work.lclpnet.illwalls.entity.IllusoryWallEntity;

public class IllusoryWallEntityRenderer extends EntityRenderer<IllusoryWallEntity> implements RenderLayerGetter {

    private final StructureRenderer structureRenderer;

    public IllusoryWallEntityRenderer(EntityRendererFactory.Context context) {
        super(context);

        var blockRenderManager = context.getBlockRenderManager();
        var blockIllusionRenderManager = new BlockIllusionRenderManager(blockRenderManager, this);
        this.structureRenderer = new CullStructureRenderer(blockIllusionRenderManager);
    }

    @SuppressWarnings("deprecation")
    @Override
    public Identifier getTexture(IllusoryWallEntity entity) {
        return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
    }

    @Override
    public void render(IllusoryWallEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        var structure = entity.getStructureContainer().getWrapper();

        structureRenderer.render(structure, entity.getPos(), matrices, vertexConsumers, light, 1F);
    }

    @SuppressWarnings("deprecation")
    @Override
    public RenderLayer getRenderLayer(BlockState state, float alpha) {
        return RenderLayer.getOutline(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
    }
}
