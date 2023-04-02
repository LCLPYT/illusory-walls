package work.lclpnet.illwalls.render;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import work.lclpnet.illwalls.impl.FabricStructureView;

public class CullStructureRenderer implements StructureRenderer {

    private final BlockIllusionRenderManager renderManager;

    public CullStructureRenderer(BlockIllusionRenderManager renderManager) {
        this.renderManager = renderManager;
    }

    @Override
    public void render(FabricStructureView structure, Vec3d origin, MatrixStack matrices, VertexConsumerProvider vertices, int light, float alpha) {
        if (alpha <= 0F) return;

        var positions = structure.getBlockPositions();
        for (var pos : positions) {
            var state = structure.getBlockState(pos);
            if (state.isAir()) continue;

            matrices.push();
            matrices.translate(pos.getX() - origin.x, pos.getY() - origin.y, pos.getZ() - origin.z);

            var cullInfo = new CullInfo(structure, pos);
            renderManager.renderBlockAsEntity(state, cullInfo, matrices, vertices, light, OverlayTexture.DEFAULT_UV, alpha);

            matrices.pop();
        }
    }
}
