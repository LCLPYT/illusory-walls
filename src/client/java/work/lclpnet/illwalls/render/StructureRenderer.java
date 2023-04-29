package work.lclpnet.illwalls.render;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import work.lclpnet.kibu.schematic.FabricStructureView;

public interface StructureRenderer {

    void render(FabricStructureView structure, Vec3d origin, MatrixStack matrices, VertexConsumerProvider vertices, int light, float alpha);
}
