package work.lclpnet.illwalls.util;

import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import work.lclpnet.illwalls.IllusoryWallsApi;
import work.lclpnet.illwalls.IllusoryWallsMod;
import work.lclpnet.illwalls.entity.EntityTrackingUpdatable;
import work.lclpnet.illwalls.entity.IllusoryWallEntity;
import work.lclpnet.illwalls.wall.IllusoryWallPlayerSettings;

public class PlayerInfo {

    private final ServerPlayer player;
    private boolean seeIllusoryWalls = false;
    private volatile IllusoryWallPlayerSettings wallSettings = null;

    private PlayerInfo(ServerPlayer player) {
        this.player = player;
    }

    public boolean canSeeIllusoryWalls() {
        return seeIllusoryWalls;
    }

    public void setCanSeeIllusoryWalls(boolean canSeeIllusoryWalls) {
        if (this.seeIllusoryWalls == canSeeIllusoryWalls) return;

        this.seeIllusoryWalls = canSeeIllusoryWalls;

        // update tracking status for all illusory walls in the players world
        ServerLevel world = player.level();
        ChunkMap chunkManager = world.getChunkSource().chunkMap;
        if (!(chunkManager instanceof EntityTrackingUpdatable entityTrackingUpdatable)) return;

        var entities = IllusoryWallsApi.getInstance().lookup().getAll(world);
        for (IllusoryWallEntity entity : entities) {
            entityTrackingUpdatable.illwalls$updateTrackedStatus(entity, player);
        }
    }

    public void updatePlayerCanSeeIllusoryWalls() {
        boolean shouldSeeWalls = shouldSeeIllusoryWalls();
        setCanSeeIllusoryWalls(shouldSeeWalls);
    }

    private boolean shouldSeeIllusoryWalls() {
        if (!player.canUseGameMasterBlocks()) return false;

        ItemStack stack = this.player.getItemInHand(InteractionHand.MAIN_HAND);

        if (stack.is(IllusoryWallsMod.STAFF_OF_ILLUSION_ITEM)) {
            return true;
        }

        stack = this.player.getItemInHand(InteractionHand.OFF_HAND);

        return stack.is(IllusoryWallsMod.STAFF_OF_ILLUSION_ITEM);
    }

    public IllusoryWallPlayerSettings getWallSettings() {
        if (wallSettings != null) return wallSettings;

        synchronized (this) {
            if (wallSettings != null) return wallSettings;

            wallSettings = new IllusoryWallPlayerSettings();
        }

        return wallSettings;
    }

    public static PlayerInfo create(ServerPlayer player) {
        return new PlayerInfo(player);
    }

    public static PlayerInfo get(ServerPlayer player) {
        return ((PlayerInfoView) player).illwalls$getPlayerInfo();
    }
}
