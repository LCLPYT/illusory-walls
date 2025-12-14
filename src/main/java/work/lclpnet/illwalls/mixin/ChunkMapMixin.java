package work.lclpnet.illwalls.mixin;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ChunkMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import work.lclpnet.illwalls.entity.EntityTrackingUpdatable;

@Mixin(ChunkMap.class)
public class ChunkMapMixin implements EntityTrackingUpdatable {

    @Shadow @Final private Int2ObjectMap<ChunkMap.TrackedEntity> entityMap;

    @Override
    public void illwalls$updateTrackedStatus(Entity entity, ServerPlayer player) {
        ChunkMap.TrackedEntity tracker = entityMap.get(entity.getId());

        if (tracker != null) {
            tracker.updatePlayer(player);
        }
    }
}
