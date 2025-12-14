package work.lclpnet.illwalls.wall;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.core.BlockPos;
import work.lclpnet.illwalls.entity.IllusoryWallEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public class NaiveWallLookup implements IllusoryWallLookup {

    @Override
    public Collection<IllusoryWallEntity> getAll(ServerLevel world) {
        var entities = new ArrayList<IllusoryWallEntity>();

        world.getEntities(
                EntityTypeTest.forClass(IllusoryWallEntity.class),
                entity -> true,
                entities
        );

        return entities;
    }

    @Override
    public Optional<IllusoryWallEntity> getWallAt(ServerLevel world, BlockPos pos) {
        var entities = new ArrayList<IllusoryWallEntity>();

        // checks every loaded entity O(n)
        world.getEntities(
                EntityTypeTest.forClass(IllusoryWallEntity.class),
                entity -> entity.getStructureContainer().getWrapper().isInBounds(pos),
                entities,
                1
        );

        return entities.isEmpty() ? Optional.empty() : Optional.ofNullable(entities.get(0));
    }
}
