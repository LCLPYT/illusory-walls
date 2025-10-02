package work.lclpnet.illwalls.mixin.client;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderManager;
import net.minecraft.client.render.command.RenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.state.WorldRenderState;
import net.minecraft.client.util.Handle;
import net.minecraft.util.profiler.Profiler;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import work.lclpnet.illwalls.render.IllusoryBatchRendererProvider;
import work.lclpnet.illwalls.render.StructureEntityBatchRenderer;

/**
 * The purpose of this mixin is to render the outline post processor for illusory wall entities.
 * Normally, the outline would only be rendered if the entity has the glowing effect.
 * However, the {@link work.lclpnet.illwalls.render.IllusoryWallEntityRenderer} manually renders the outline, which requires custom handling.
 */
@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Shadow
    @Final
    private EntityRenderManager entityRenderManager;

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    public void illwalls$injectBatchRenderers(MinecraftClient client,
                                              EntityRenderManager entityRenderManager,
                                              BlockEntityRenderManager blockEntityRenderManager,
                                              BufferBuilderStorage bufferBuilders,
                                              WorldRenderState worldRenderState,
                                              RenderDispatcher entityRenderDispatcher,
                                              CallbackInfo ci) {

        var access = (RenderDispatcherAccessor) entityRenderDispatcher;
        var vertexConsumers = access.getVertexConsumers();
        var blockRenderManager = access.getBlockRenderManager();

        var provider = (IllusoryBatchRendererProvider) entityRenderManager;

        provider.illwalls$setStructureEntityRenderer(new StructureEntityBatchRenderer(vertexConsumers, blockRenderManager));
    }

    @Inject(
            method = "method_62214",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/command/RenderDispatcher;render()V"
            )
    )
    public void illwalls$renderIllusory(GpuBufferSlice gpuBufferSlice, WorldRenderState worldRenderState,
                                        Profiler profiler, Matrix4f matrix4f, Handle<Framebuffer> handle,
                                        Handle<Framebuffer> handle2, boolean bl, Frustum frustum,
                                        Handle<Framebuffer> handle3, Handle<Framebuffer> handle4, CallbackInfo ci) {

        var provider = (IllusoryBatchRendererProvider) entityRenderManager;

        provider.illwalls$getStructureEntityRenderer().render();
    }
}
