package work.lclpnet.illwalls.entity;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import work.lclpnet.illwalls.IllusoryWallsApi;

public class PlayerInfo {

    private final ServerPlayerEntity player;
    private boolean seeIllusoryWalls = false;

    private PlayerInfo(ServerPlayerEntity player) {
        this.player = player;
    }

    public boolean canSeeIllusoryWalls() {
        return seeIllusoryWalls;
    }

    public void setCanSeeIllusoryWalls(boolean canSeeIllusoryWalls) {
        if (this.seeIllusoryWalls == canSeeIllusoryWalls) return;

        this.seeIllusoryWalls = canSeeIllusoryWalls;

        // TODO debounce 1 sec
        // update tracking status for all illusory walls in the players world
        ServerWorld world = player.getWorld();
        ThreadedAnvilChunkStorage chunkManager = world.getChunkManager().threadedAnvilChunkStorage;
        if (!(chunkManager instanceof EntityTrackingUpdatable entityTrackingUpdatable)) return;

        var entities = IllusoryWallsApi.getInstance().lookup().getAll(world);
        for (IllusoryWallEntity entity : entities) {
            entityTrackingUpdatable.illwalls$updateTrackedStatus(entity, player);
        }
    }

    public static PlayerInfo create(ServerPlayerEntity player) {
        return new PlayerInfo(player);
    }

    public static PlayerInfo get(ServerPlayerEntity player) {
        return ((PlayerInfoView) player).illwalls$getPlayerInfo();
    }
}
