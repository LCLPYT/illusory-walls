package work.lclpnet.illwalls.mixin;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import work.lclpnet.illwalls.entity.EntityTrackingUpdatable;

@Mixin(ThreadedAnvilChunkStorage.class)
public class ThreadedAnvilChunkStorageMixin implements EntityTrackingUpdatable {

    @Shadow @Final private Int2ObjectMap<ThreadedAnvilChunkStorage.EntityTracker> entityTrackers;

    @Override
    public void illwalls$updateTrackedStatus(Entity entity, ServerPlayerEntity player) {
        ThreadedAnvilChunkStorage.EntityTracker tracker = entityTrackers.get(entity.getId());

        if (tracker != null) {
            tracker.updateTrackedStatus(player);
        }
    }
}
