package work.lclpnet.illwalls.wall;

import net.minecraft.block.Blocks;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import work.lclpnet.illwalls.IllusoryWallsMod;
import work.lclpnet.illwalls.entity.IllusoryWallEntity;
import work.lclpnet.illwalls.struct.FabricStructureWrapper;
import work.lclpnet.illwalls.struct.StructureBatchUpdate;

import java.util.*;

public class SimpleIllusoryWallManager implements IllusoryWallManager {

    private final IllusoryWallLookup wallLookup;

    public SimpleIllusoryWallManager(IllusoryWallLookup wallLookup) {
        this.wallLookup = Objects.requireNonNull(wallLookup);
    }

    @Override
    public boolean fadeWallAtIfPresent(ServerWorld world, BlockPos pos) {
        var optWall = wallLookup.getWallAt(world, pos);
        if (optWall.isEmpty()) return false;

        var entity = optWall.get();
        entity.fade();

        return true;
    }

    @Override
    public boolean makeBlockIllusory(ServerWorld world, BlockPos pos) {
        if (wallLookup.getWallAt(world, pos).isPresent()) return false;  // there is already a wall

        // check neighbours for any existing illusory walls
        Set<IllusoryWallEntity> nearbyWalls = new HashSet<>();
        var adjPos = new BlockPos.Mutable();

        for (Direction direction : Direction.values()) {
            adjPos.set(pos, direction);

            var opt = wallLookup.getWallAt(world, adjPos);
            opt.ifPresent(nearbyWalls::add);
        }

        if (nearbyWalls.isEmpty()) {
            // there is no illusory wall nearby, create one
            IllusoryWallsMod.ILLUSORY_WALL_ENTITY.spawn(world, null, created -> {
                FabricStructureWrapper structure = created.getStructureContainer().getWrapper();
                structure.setBlockState(pos, world.getBlockState(pos));
            }, pos, SpawnReason.SPAWN_EGG, false, false);
        } else {
            var iterator = nearbyWalls.iterator();

            // get one nearby wall (there is at least one wall present)
            IllusoryWallEntity wall = iterator.next();
            FabricStructureWrapper structure = wall.getStructureContainer().getWrapper();

            if (!iterator.hasNext()) {
                // there is only one wall nearby
                structure.setBlockState(pos, world.getBlockState(pos));
            } else {
                // there are more than one neighbouring walls
                StructureBatchUpdate batchUpdate = structure instanceof StructureBatchUpdate ? (StructureBatchUpdate) structure : null;
                if (batchUpdate != null) {
                    batchUpdate.beginBatch();
                }

                structure.setBlockState(pos, world.getBlockState(pos));

                // merge other nearby walls
                while (iterator.hasNext()) {
                    IllusoryWallEntity other = iterator.next();
                    FabricStructureWrapper otherStructure = other.getStructureContainer().getWrapper();

                    otherStructure.copyTo(structure);

                    other.discard();
                }

                if (batchUpdate != null) {
                    batchUpdate.endBatch();
                }
            }
        }

        return true;
    }

    @Override
    public boolean removeIllusoryBlock(ServerWorld world, BlockPos pos) {
        var optWall = wallLookup.getWallAt(world, pos);
        if (optWall.isEmpty()) return false;  // there is no wall

        IllusoryWallEntity entity = optWall.get();

        FabricStructureWrapper structure = entity.getStructureContainer().getWrapper();
        structure.setBlockState(pos, Blocks.AIR.getDefaultState());
        return true;
    }
}
