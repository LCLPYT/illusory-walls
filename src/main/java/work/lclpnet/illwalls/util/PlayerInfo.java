package work.lclpnet.illwalls.util;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import work.lclpnet.illwalls.IllusoryWallsApi;
import work.lclpnet.illwalls.IllusoryWallsMod;
import work.lclpnet.illwalls.entity.EntityTrackingUpdatable;
import work.lclpnet.illwalls.entity.IllusoryWallEntity;
import work.lclpnet.illwalls.wall.IllusoryWallPlayerSettings;

public class PlayerInfo {

    private final ServerPlayerEntity player;
    private boolean seeIllusoryWalls = false;
    private volatile IllusoryWallPlayerSettings wallSettings = null;

    private PlayerInfo(ServerPlayerEntity player) {
        this.player = player;
    }

    public boolean canSeeIllusoryWalls() {
        return seeIllusoryWalls;
    }

    public void setCanSeeIllusoryWalls(boolean canSeeIllusoryWalls) {
        if (this.seeIllusoryWalls == canSeeIllusoryWalls) return;

        this.seeIllusoryWalls = canSeeIllusoryWalls;

        // update tracking status for all illusory walls in the players world
        ServerWorld world = player.getEntityWorld();
        ServerChunkLoadingManager chunkManager = world.getChunkManager().chunkLoadingManager;
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
        if (!player.isCreativeLevelTwoOp()) return false;

        ItemStack stack = this.player.getStackInHand(Hand.MAIN_HAND);

        if (stack.isOf(IllusoryWallsMod.STAFF_OF_ILLUSION_ITEM)) {
            return true;
        }

        stack = this.player.getStackInHand(Hand.OFF_HAND);

        return stack.isOf(IllusoryWallsMod.STAFF_OF_ILLUSION_ITEM);
    }

    public IllusoryWallPlayerSettings getWallSettings() {
        if (wallSettings != null) return wallSettings;

        synchronized (this) {
            if (wallSettings != null) return wallSettings;

            wallSettings = new IllusoryWallPlayerSettings();
        }

        return wallSettings;
    }

    public static PlayerInfo create(ServerPlayerEntity player) {
        return new PlayerInfo(player);
    }

    public static PlayerInfo get(ServerPlayerEntity player) {
        return ((PlayerInfoView) player).illwalls$getPlayerInfo();
    }
}
