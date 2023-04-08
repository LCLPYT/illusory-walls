package work.lclpnet.illwalls.entity;

import net.minecraft.server.network.ServerPlayerEntity;

public interface EntityConditionalTracking {

    boolean shouldBeTrackedBy(ServerPlayerEntity player);
}
