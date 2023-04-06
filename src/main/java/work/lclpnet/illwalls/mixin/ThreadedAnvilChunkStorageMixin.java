package work.lclpnet.illwalls.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import work.lclpnet.illwalls.entity.ServerOnlyEntity;

@Mixin(ThreadedAnvilChunkStorage.class)
public class ThreadedAnvilChunkStorageMixin {

    @Inject(
            method = "loadEntity",
            at = @At("HEAD"),
            cancellable = true
    )
    public void illwalls$loadServerOnlyEntity(Entity entity, CallbackInfo ci) {
        if (entity instanceof ServerOnlyEntity) ci.cancel();
    }
}
