package work.lclpnet.illwalls.wall;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;

public interface IllusoryWallManager {

    default boolean fadeWallAtIfPresent(ServerLevel world, BlockPos pos) {
        return fadeWallAtIfPresent(world, pos, null);
    }

    boolean fadeWallAtIfPresent(ServerLevel world, BlockPos pos, @Nullable BlockPos from);

    boolean makeBlockIllusory(ServerLevel world, BlockPos pos, @Nullable ServerPlayer player);

    boolean removeIllusoryBlock(ServerLevel world, BlockPos pos);

    default boolean makeBlockIllusory(ServerLevel world, BlockPos pos) {
        return makeBlockIllusory(world, pos, null);
    }
}
