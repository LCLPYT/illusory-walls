package work.lclpnet.illwalls.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;

public interface EntityTrackingUpdatable {

    void illwalls$updateTrackedStatus(Entity entity, ServerPlayer player);
}
