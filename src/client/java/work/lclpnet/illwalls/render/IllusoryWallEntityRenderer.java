package work.lclpnet.illwalls.render;

import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import com.mojang.blaze3d.vertex.PoseStack;
import work.lclpnet.illwalls.entity.IllusoryWallEntity;
import work.lclpnet.illwalls.mixin.client.LevelRendererAccessor;

public class IllusoryWallEntityRenderer extends EntityRenderer<IllusoryWallEntity, IllusoryWallRenderState> implements RenderLayerGetter {

    private final StructureRenderer structureRenderer;

    public IllusoryWallEntityRenderer(EntityRendererProvider.Context context) {
        super(context);

        var blockRenderManager = context.getBlockRenderDispatcher();
        var blockIllusionRenderManager = new BlockIllusionRenderManager(blockRenderManager, this);
        this.structureRenderer = new CullStructureRenderer(blockIllusionRenderManager);
    }

    @Override
    public IllusoryWallRenderState createRenderState() {
        return new IllusoryWallRenderState();
    }

    @Override
    public void extractRenderState(IllusoryWallEntity entity, IllusoryWallRenderState state, float tickDelta) {
        super.extractRenderState(entity, state, tickDelta);

        state.outlineColor = entity.getOutlineColor();
        state.structure = entity.getStructureContainer().getWrapper();
    }

    @Override
    public void submit(IllusoryWallRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        LevelRenderer worldRenderer = Minecraft.getInstance().levelRenderer;
        RenderBuffers bufferBuilders = ((LevelRendererAccessor) worldRenderer).getRenderBuffers();
        OutlineBufferSource outlineVertexConsumerProvider = bufferBuilders.outlineBufferSource();

        outlineVertexConsumerProvider.setColor(state.outlineColor);

        structureRenderer.render(state.structure, state.x, state.y, state.z, matrices, outlineVertexConsumerProvider, state.lightCoords, 1F);
    }

    @SuppressWarnings("deprecation")
    @Override
    public RenderType getRenderLayer(BlockState state, float alpha) {
        return RenderTypes.outline(TextureAtlas.LOCATION_BLOCKS);
    }

    @Override
    protected boolean affectedByCulling(IllusoryWallEntity entity) {
        return false;  // ignore camera frustum culling for now
    }
}
