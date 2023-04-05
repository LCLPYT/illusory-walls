package work.lclpnet.illwalls.wall;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import work.lclpnet.illwalls.entity.StructureEntity;

import java.util.Optional;

public interface IllusoryWallLookup {

    Optional<StructureEntity> getWallAt(ServerWorld world, BlockPos pos);
}
