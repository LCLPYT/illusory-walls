package work.lclpnet.illwalls.wall;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import work.lclpnet.illwalls.entity.IllusoryWallEntity;

import java.util.Collection;
import java.util.Optional;

public interface IllusoryWallLookup {

    Collection<IllusoryWallEntity> getAll(ServerLevel world);

    Optional<IllusoryWallEntity> getWallAt(ServerLevel world, BlockPos pos);
}
