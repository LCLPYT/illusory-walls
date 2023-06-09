package work.lclpnet.illwalls.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.EntityTrackingListener;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import work.lclpnet.illwalls.entity.EntityConditionalTracking;

import java.util.Set;

@Mixin(ThreadedAnvilChunkStorage.EntityTracker.class)
public class ThreadedAnvilChunkStorageEntityTrackerMixin {

    @Shadow @Final
    Entity entity;

    @Shadow @Final private Set<EntityTrackingListener> listeners;

    @Shadow @Final
    EntityTrackerEntry entry;

    @Inject(
            method = "updateTrackedStatus(Lnet/minecraft/server/network/ServerPlayerEntity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Set;add(Ljava/lang/Object;)Z"
            ),
            cancellable = true
    )
    public void illwalls$loadServerOnlyEntity(ServerPlayerEntity player, CallbackInfo ci) {
        if (!(entity instanceof EntityConditionalTracking conditional)) return;

        if (!conditional.shouldBeTrackedBy(player)) {
            ci.cancel();

            if (this.listeners.remove(player.networkHandler)) {
                this.entry.stopTracking(player);
            }
        }
    }
}
