package work.lclpnet.illwalls.wall;

import net.minecraft.block.Blocks;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import work.lclpnet.illwalls.IllusoryWallsMod;
import work.lclpnet.illwalls.entity.IllusoryWallEntity;
import work.lclpnet.illwalls.impl.FabricStructureWrapper;

import java.util.Objects;

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
        IllusoryWallEntity entity = null;
        var adjPos = new BlockPos.Mutable();

        for (Direction direction : Direction.values()) {
            adjPos.set(pos, direction);

            var opt = wallLookup.getWallAt(world, adjPos);
            if (opt.isEmpty()) continue;

            // found an adjacent wall
            entity = opt.get();
            break;
        }

        if (entity == null) {
            // there is no illusory wall nearby, create one
            IllusoryWallsMod.ILLUSORY_WALL_ENTITY.spawn(world, null, created -> {
                FabricStructureWrapper structure = created.getStructureContainer().getWrapper();
                structure.setBlockState(pos, world.getBlockState(pos));
            }, pos, SpawnReason.SPAWN_EGG, false, false);
        } else {
            // update nearby illusory wall
            FabricStructureWrapper structure = entity.getStructureContainer().getWrapper();
            structure.setBlockState(pos, world.getBlockState(pos));
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
