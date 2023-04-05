package work.lclpnet.illwalls.wall;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.BlockPos;
import work.lclpnet.illwalls.entity.StructureEntity;

import java.util.ArrayList;
import java.util.Optional;

public class NaiveWallLookup implements IllusoryWallLookup {

    @Override
    public Optional<StructureEntity> getWallAt(ServerWorld world, BlockPos pos) {
        var entities = new ArrayList<StructureEntity>();

        // checks every loaded entity O(n)
        world.collectEntitiesByType(
                TypeFilter.instanceOf(StructureEntity.class),
                entity -> entity.getStructure().isInBounds(pos),
                entities,
                1
        );

        return entities.isEmpty() ? Optional.empty() : Optional.ofNullable(entities.get(0));
    }
}
