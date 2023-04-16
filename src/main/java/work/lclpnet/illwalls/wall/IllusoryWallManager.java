package work.lclpnet.illwalls.wall;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public interface IllusoryWallManager {

    default boolean fadeWallAtIfPresent(ServerWorld world, BlockPos pos) {
        return fadeWallAtIfPresent(world, pos, null);
    }

    boolean fadeWallAtIfPresent(ServerWorld world, BlockPos pos, @Nullable BlockPos from);

    boolean makeBlockIllusory(ServerWorld world, BlockPos pos);

    boolean removeIllusoryBlock(ServerWorld world, BlockPos pos);
}
