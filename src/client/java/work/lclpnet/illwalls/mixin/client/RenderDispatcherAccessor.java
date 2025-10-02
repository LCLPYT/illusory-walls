package work.lclpnet.illwalls.mixin.client;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.command.RenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderDispatcher.class)
public interface RenderDispatcherAccessor {

    @Accessor
    VertexConsumerProvider.Immediate getVertexConsumers();

    @Accessor
    BlockRenderManager getBlockRenderManager();
}
