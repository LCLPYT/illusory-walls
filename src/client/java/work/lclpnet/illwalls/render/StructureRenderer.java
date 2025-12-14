package work.lclpnet.illwalls.render;

import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.phys.Vec3;
import work.lclpnet.kibu.schematic.FabricStructureView;

public interface StructureRenderer {

    void render(FabricStructureView structure, double x, double y, double z, PoseStack matrices, MultiBufferSource vertices, int light, float alpha);
}
