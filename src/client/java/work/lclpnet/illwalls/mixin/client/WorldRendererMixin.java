package work.lclpnet.illwalls.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import work.lclpnet.illwalls.render.OutlineRenderOverride;

/**
 * The purpose of this mixin is to render the outline post processor when a nested renderer requested it,
 * regardless if there are glowing entities rendered or not. By default, the outline post processor is only rendered,
 * if there are glowing entities in the scene, as the outline feature is intended for that.
 * <p>
 * The mixin consists out of three injections.
 * The first before it is checked, if the outline post processor should be rendered.
 * The second when the outline post processor is rendered.
 * And a third, after the outline post processor has rendered or not.
 * <p>
 * Based on that, it can be decided to draw the outline post processor or not; based on whether it was already drawn,
 * or if it should be rendered from an override.
 * This method requires side effects.
 *
 * @author LCLP
 */
@Mixin(WorldRenderer.class)
public class WorldRendererMixin implements OutlineRenderOverride {

    @Shadow private @Nullable PostEffectProcessor entityOutlinePostProcessor;
    @Shadow @Final private MinecraftClient client;
    @Unique
    private boolean renderedOutline = false;
    @Unique
    private boolean overrideOutline = false;

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/BufferBuilderStorage;getOutlineVertexConsumers()Lnet/minecraft/client/render/OutlineVertexConsumerProvider;"
            )
    )
    public void illwalls$beforeOutlineRender(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f positionMatrix, CallbackInfo ci) {
        renderedOutline = false;
    }

    @Inject(
            method = "render",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/render/WorldRenderer;entityOutlinePostProcessor:Lnet/minecraft/client/gl/PostEffectProcessor;",
                    opcode = Opcodes.GETFIELD
            )
    )
    public void illwalls$onOutlineRender(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f positionMatrix, CallbackInfo ci) {
        renderedOutline = true;
    }

    @Inject(
            method = "render",
            at = @At(
                    value = "CONSTANT",
                    args = "stringValue=destroyProgress"
            )
    )
    public void illwalls$afterOutlineMaybeRendered(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f positionMatrix, CallbackInfo ci) {
        if (renderedOutline || !overrideOutline || this.entityOutlinePostProcessor == null) return;

        this.overrideOutline = false;
        this.entityOutlinePostProcessor.render(tickDelta);
        this.client.getFramebuffer().beginWrite(false);
    }

    @Override
    public void illwalls$markOverridden() {
        overrideOutline = true;
    }
}
