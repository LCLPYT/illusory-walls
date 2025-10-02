package work.lclpnet.illwalls.render;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import work.lclpnet.illwalls.entity.IllusoryWallEntity;
import work.lclpnet.illwalls.entity.StructureEntity;

import java.util.ArrayList;
import java.util.List;

public class StructureEntityBatchRenderer implements RenderLayerGetter {

    private final List<Command> commands = new ArrayList<>();
    private final MatrixStack matrices = new MatrixStack();
    private final VertexConsumerProvider.Immediate vertexConsumers;
    private final StructureRenderer structureRenderer;

    public StructureEntityBatchRenderer(VertexConsumerProvider.Immediate vertexConsumers, BlockRenderManager blockRenderManager) {
        this.vertexConsumers = vertexConsumers;

        var blockIllusionRenderManager = new BlockIllusionRenderManager(blockRenderManager, this);
        this.structureRenderer = new CullStructureRenderer(blockIllusionRenderManager);
    }

    // this is like RenderCommandQueue::submitBlock, but with alpha parameter
    public void submit(MatrixStack matrices, StructureEntityRenderState state) {
        commands.add(new Command(matrices.peek().copy(), state));
    }

    public void render() {
        for (Command command : commands) {
            this.matrices.push();
            this.matrices.peek().copy(command.matricesEntry());

            render(command);

            this.matrices.pop();
        }

        commands.clear();
    }

    private void render(Command command) {
        float alpha = 1F;

        var state = command.renderState;

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
        int light = state.light;

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

    public record Command(MatrixStack.Entry matricesEntry, StructureEntityRenderState renderState) {}
}
