package work.lclpnet.illwalls.render;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import work.lclpnet.kibu.schematic.FabricStructureView;

public class CullStructureRenderer implements StructureRenderer {

    private final BlockIllusionRenderManager renderManager;

    public CullStructureRenderer(BlockIllusionRenderManager renderManager) {
        this.renderManager = renderManager;
    }

    @Override
    public void render(FabricStructureView structure, double x, double y, double z, MatrixStack matrices, VertexConsumerProvider vertices, int light, float alpha) {
        if (alpha <= 0F) return;

        var positions = structure.getBlockPositions();
        for (var pos : positions) {
            var state = structure.getBlockState(pos);
            if (state.isAir()) continue;

            matrices.push();
            matrices.translate(pos.getX() - x, pos.getY() - y, pos.getZ() - z);

            var cullInfo = new CullInfo(structure, pos);
            renderManager.renderBlockAsEntity(state, cullInfo, matrices, vertices, light, OverlayTexture.DEFAULT_UV, alpha);

            matrices.pop();
        }
    }
}
