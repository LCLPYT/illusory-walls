package work.lclpnet.illwalls.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import work.lclpnet.illwalls.entity.IllusoryWallEntity;

/**
 * The purpose of this mixin is to render the outline post processor for illusory wall entities.
 * Normally, the outline would only be rendered if the entity has the glowing effect.
 * However, the {@link work.lclpnet.illwalls.render.IllusoryWallEntityRenderer} manually renders the outline, which requires custom handling.
 */
@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @WrapOperation(
            method = "getEntitiesToRender",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/MinecraftClient;hasOutline(Lnet/minecraft/entity/Entity;)Z"
            )
    )
    private boolean illwalls$modifyGlowing(MinecraftClient instance, Entity entity, Operation<Boolean> original) {
        return entity instanceof IllusoryWallEntity || original.call(instance, entity);
    }
}
