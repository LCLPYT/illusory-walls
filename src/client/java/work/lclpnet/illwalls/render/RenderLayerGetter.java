package work.lclpnet.illwalls.render;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;

public interface RenderLayerGetter {

    RenderLayer getRenderLayer(BlockState state, float alpha);
}
