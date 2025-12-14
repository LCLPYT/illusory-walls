package work.lclpnet.illwalls.render;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.level.block.state.BlockState;

public interface RenderLayerGetter {

    RenderType getRenderLayer(BlockState state, float alpha);
}
