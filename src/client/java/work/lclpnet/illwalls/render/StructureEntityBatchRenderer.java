package work.lclpnet.illwalls.render;

import net.minecraft.client.renderer.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import work.lclpnet.illwalls.entity.IllusoryWallEntity;
import work.lclpnet.illwalls.entity.StructureEntity;

import java.util.ArrayList;
import java.util.List;

public class StructureEntityBatchRenderer implements RenderLayerGetter {

    private final List<Command> commands = new ArrayList<>();
    private final PoseStack matrices = new PoseStack();
    private final MultiBufferSource.BufferSource vertexConsumers;
    private final StructureRenderer structureRenderer;

    public StructureEntityBatchRenderer(MultiBufferSource.BufferSource vertexConsumers, BlockRenderDispatcher blockRenderManager) {
        this.vertexConsumers = vertexConsumers;

        var blockIllusionRenderManager = new BlockIllusionRenderManager(blockRenderManager, this);
        this.structureRenderer = new CullStructureRenderer(blockIllusionRenderManager);
    }

    // this is like RenderCommandQueue::submitBlock, but with alpha parameter
    public void submit(PoseStack matrices, StructureEntityRenderState state) {
        commands.add(new Command(matrices.last().copy(), state));
    }

    public void render() {
        for (Command command : commands) {
            this.matrices.pushPose();
            this.matrices.last().set(command.matricesEntry());

            render(command);

            this.matrices.popPose();
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

            alpha = Mth.clamp(alpha, 0F, 1F);
        }

        BlockPos fadingFrom = state.fadingFrom;
        int light = state.lightCoords;

        if (fadingFrom != null) {
            light = LightTexture.pack(state.blockLight, state.skyLight);
        }

        structureRenderer.render(state.structure, state.x, state.y, state.z, matrices, vertexConsumers, light, alpha);
    }

    @Override
    public RenderType getRenderLayer(BlockState state, float alpha) {
        if (alpha >= 1.0f) {
            return ItemBlockRenderTypes.getRenderType(state);
        }

        return Sheets.translucentItemSheet();
    }

    public record Command(PoseStack.Pose matricesEntry, StructureEntityRenderState renderState) {}
}
