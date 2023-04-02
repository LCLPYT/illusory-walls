package work.lclpnet.illwalls.impl;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public interface FabricStructureView extends BlockView {

    BlockState getBlockState(BlockPos pos);

    Iterable<BlockPos> getBlockPositions();
}
