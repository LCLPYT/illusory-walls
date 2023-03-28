package work.lclpnet.illwalls.structure;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

public interface BlockStorage {

    void setBlockState(BlockPos pos, BlockState state);

    @Nonnull
    BlockState getBlockState(BlockPos pos);

    Iterable<BlockPos> getBlockPositions();
}
