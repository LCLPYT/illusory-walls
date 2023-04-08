package work.lclpnet.illwalls.entity;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;

public interface EntityTrackingUpdatable {

    void illwalls$updateTrackedStatus(Entity entity, ServerPlayerEntity player);
}
