package work.lclpnet.illwalls.wall;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

public class IllusoryWallManager {

    private final IllusoryWallLookup wallLookup;

    public IllusoryWallManager(IllusoryWallLookup wallLookup) {
        this.wallLookup = Objects.requireNonNull(wallLookup);
    }

    public boolean fadeWallAtIfPresent(ServerWorld world, BlockPos pos) {
        var optWall = wallLookup.getWallAt(world, pos);
        if (optWall.isEmpty()) return false;

        var entity = optWall.get();
        entity.fade();

        return true;
    }
}
