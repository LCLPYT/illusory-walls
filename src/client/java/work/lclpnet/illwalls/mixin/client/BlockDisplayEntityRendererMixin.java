package work.lclpnet.illwalls.mixin.client;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.DisplayEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.DisplayEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import work.lclpnet.illwalls.render.IllusionBlockRenderManager;

@Mixin(DisplayEntityRenderer.BlockDisplayEntityRenderer.class)
public class BlockDisplayEntityRendererMixin {

	@Mutable
	@Unique
	@Final
	private IllusionBlockRenderManager illusionBlockRenderManager;

	@Inject(
			method = "<init>",
			at = @At("RETURN")
	)
	public void onConstruct(EntityRendererFactory.Context context, CallbackInfo ci) {
		this.illusionBlockRenderManager = new IllusionBlockRenderManager(context.getBlockRenderManager());
	}

	@Inject(
			method = "render(Lnet/minecraft/entity/decoration/DisplayEntity$BlockDisplayEntity;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IF)V",
			at = @At("HEAD"),
			cancellable = true
	)
	public void onRender(DisplayEntity.BlockDisplayEntity blockDisplayEntity, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, float f, CallbackInfo ci) {
		this.illusionBlockRenderManager.renderBlockAsEntity(blockDisplayEntity.getBlockState(), matrixStack, vertexConsumerProvider, i, OverlayTexture.DEFAULT_UV);
		ci.cancel();
	}
}