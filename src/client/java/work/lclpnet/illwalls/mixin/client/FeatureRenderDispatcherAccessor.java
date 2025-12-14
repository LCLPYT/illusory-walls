package work.lclpnet.illwalls.mixin.client;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FeatureRenderDispatcher.class)
public interface FeatureRenderDispatcherAccessor {

    @Accessor
    MultiBufferSource.BufferSource getBufferSource();

    @Accessor
    BlockRenderDispatcher getBlockRenderDispatcher();
}
