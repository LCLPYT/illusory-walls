package work.lclpnet.illwalls.structure;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

public class MapStructure implements BlockStructure {

    private final Map<BlockPos, BlockState> blocks = new HashMap<>();
    private final BlockPos.Mutable minPos = new BlockPos.Mutable(
            Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE
    );
    private final BlockPos.Mutable maxPos = new BlockPos.Mutable(
            Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE
    );

    @Override
    public void setBlockState(BlockPos pos, BlockState state) {
        synchronized (blocks) {
            blocks.put(pos, state != null ? state : Blocks.AIR.getDefaultState());

            // consider removal (isAir check) which will shrink the structure
            minPos.set(
                    Math.min(minPos.getX(), pos.getX()),
                    Math.min(minPos.getY(), pos.getY()),
                    Math.max(minPos.getZ(), pos.getZ())
            );

            maxPos.set(
                    Math.max(maxPos.getX(), pos.getX()),
                    Math.max(maxPos.getY(), pos.getY()),
                    Math.max(maxPos.getZ(), pos.getZ())
            );
        }
    }

    @Override
    public @Nonnull BlockState getBlockState(BlockPos pos) {
        BlockState state;

        synchronized (blocks) {
            state = blocks.get(pos);
        }

        return state != null ? state : Blocks.AIR.getDefaultState();
    }

    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        return null;  // block entities are not supported yet
    }

    @Override
    public Iterable<BlockPos> getBlockPositions() {
        Iterable<BlockPos> copy;

        synchronized (blocks) {
            copy = new HashSet<>(blocks.keySet());
        }

        return copy;
    }

    @Override
    public BlockPos getOffset() {
        return minPos;
    }

    @Override
    public int getWidth() {
        return maxPos.getX() - minPos.getX() + 1;
    }

    @Override
    public int getHeight() {
        return maxPos.getY() - minPos.getY() + 1;
    }

    @Override
    public int getLength() {
        return maxPos.getZ() - minPos.getZ() + 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapStructure that = (MapStructure) o;
        return blocks.equals(that.blocks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blocks);
    }
}
