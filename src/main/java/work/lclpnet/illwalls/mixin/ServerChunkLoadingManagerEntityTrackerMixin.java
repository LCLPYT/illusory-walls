package work.lclpnet.illwalls.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ChunkMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import work.lclpnet.illwalls.entity.EntityConditionalTracking;

import java.util.Set;

@Mixin(ChunkMap.TrackedEntity.class)
public class ServerChunkLoadingManagerEntityTrackerMixin {

    @Shadow @Final
    Entity entity;

    @Shadow @Final private Set<ServerPlayerConnection> seenBy;

    @Shadow @Final
    ServerEntity serverEntity;

    @Inject(
            method = "updatePlayer(Lnet/minecraft/server/level/ServerPlayer;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Set;add(Ljava/lang/Object;)Z"
            ),
            cancellable = true
    )
    public void illwalls$loadServerOnlyEntity(ServerPlayer player, CallbackInfo ci) {
        if (!(entity instanceof EntityConditionalTracking conditional)) return;

        if (!conditional.shouldBeTrackedBy(player)) {
            ci.cancel();

            if (this.seenBy.remove(player.connection)) {
                this.serverEntity.removePairing(player);
            }
        }
    }
}
