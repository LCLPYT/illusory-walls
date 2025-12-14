package work.lclpnet.illwalls.entity;

import net.minecraft.server.level.ServerPlayer;

public interface EntityConditionalTracking {

    boolean shouldBeTrackedBy(ServerPlayer player);
}
