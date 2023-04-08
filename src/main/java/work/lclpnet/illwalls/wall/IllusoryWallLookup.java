package work.lclpnet.illwalls.wall;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import work.lclpnet.illwalls.entity.IllusoryWallEntity;

import java.util.Collection;
import java.util.Optional;

public interface IllusoryWallLookup {

    Collection<IllusoryWallEntity> getAll(ServerWorld world);

    Optional<IllusoryWallEntity> getWallAt(ServerWorld world, BlockPos pos);
}
