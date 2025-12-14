package work.lclpnet.illwalls.render;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.RenderType;

public interface RenderLayerGetter {

    RenderType getRenderLayer(BlockState state, float alpha);
}
