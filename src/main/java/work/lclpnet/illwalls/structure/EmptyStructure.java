package work.lclpnet.illwalls.structure;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

class EmptyStructure implements BlockStructure {

    private final List<BlockPos> positions = Collections.emptyList();

    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        return null;
    }

    @Override
    public BlockPos getOffset() {
        return BlockPos.ORIGIN;
    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public int getLength() {
        return 0;
    }

    @Override
    public void setBlockState(BlockPos pos, BlockState state) {
    }

    @NotNull
    @Override
    public BlockState getBlockState(BlockPos pos) {
        return Blocks.AIR.getDefaultState();
    }

    @Override
    public Iterable<BlockPos> getBlockPositions() {
        return positions;
    }
}
