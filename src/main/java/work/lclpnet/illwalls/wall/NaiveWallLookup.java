package work.lclpnet.illwalls.wall;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.BlockPos;
import work.lclpnet.illwalls.entity.IllusoryWallEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public class NaiveWallLookup implements IllusoryWallLookup {

    @Override
    public Collection<IllusoryWallEntity> getAll(ServerWorld world) {
        var entities = new ArrayList<IllusoryWallEntity>();

        world.collectEntitiesByType(
                TypeFilter.instanceOf(IllusoryWallEntity.class),
                entity -> true,
                entities
        );

        return entities;
    }

    @Override
    public Optional<IllusoryWallEntity> getWallAt(ServerWorld world, BlockPos pos) {
        var entities = new ArrayList<IllusoryWallEntity>();

        // checks every loaded entity O(n)
        world.collectEntitiesByType(
                TypeFilter.instanceOf(IllusoryWallEntity.class),
                entity -> entity.getStructure().isInBounds(pos),
                entities,
                1
        );

        return entities.isEmpty() ? Optional.empty() : Optional.ofNullable(entities.get(0));
    }
}
