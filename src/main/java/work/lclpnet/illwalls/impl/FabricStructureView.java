package work.lclpnet.illwalls.impl;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public interface FabricStructureView {

    BlockState getBlockState(BlockPos pos);

    Iterable<BlockPos> getBlockPositions();
}
