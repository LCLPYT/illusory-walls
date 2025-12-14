package work.lclpnet.illwalls.mixin.client;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.state.LevelRenderState;
import com.mojang.blaze3d.resource.ResourceHandle;
import net.minecraft.util.profiling.ProfilerFiller;
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
@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Shadow
    @Final
    private EntityRenderDispatcher entityRenderDispatcher;

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    public void illwalls$injectBatchRenderers(Minecraft client,
                                              EntityRenderDispatcher entityRenderManager,
                                              BlockEntityRenderDispatcher blockEntityRenderManager,
                                              RenderBuffers bufferBuilders,
                                              LevelRenderState worldRenderState,
                                              FeatureRenderDispatcher entityRenderDispatcher,
                                              CallbackInfo ci) {

        var access = (FeatureRenderDispatcherAccessor) entityRenderDispatcher;
        var vertexConsumers = access.getBufferSource();
        var blockRenderManager = access.getBlockRenderDispatcher();

        var provider = (IllusoryBatchRendererProvider) entityRenderManager;

        provider.illwalls$setStructureEntityRenderer(new StructureEntityBatchRenderer(vertexConsumers, blockRenderManager));
    }

    @Inject(
            method = "method_62214",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher;renderAllFeatures()V"
            )
    )
    public void illwalls$renderIllusory(GpuBufferSlice gpuBufferSlice, LevelRenderState worldRenderState,
                                        ProfilerFiller profiler, Matrix4f matrix4f, ResourceHandle<RenderTarget> handle,
                                        ResourceHandle<RenderTarget> handle2, boolean bl, Frustum frustum,
                                        ResourceHandle<RenderTarget> handle3, ResourceHandle<RenderTarget> handle4, CallbackInfo ci) {

        var provider = (IllusoryBatchRendererProvider) entityRenderDispatcher;

        provider.illwalls$getStructureEntityRenderer().render();
    }
}
