package work.lclpnet.illwalls.wall;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public interface IllusoryWallManager {

    boolean fadeWallAtIfPresent(ServerWorld world, BlockPos pos);
}
