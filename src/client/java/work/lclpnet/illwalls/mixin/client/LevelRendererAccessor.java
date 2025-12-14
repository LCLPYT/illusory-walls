package work.lclpnet.illwalls.mixin.client;

import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LevelRenderer.class)
public interface LevelRendererAccessor {

    @Accessor
    RenderBuffers getRenderBuffers();
}
