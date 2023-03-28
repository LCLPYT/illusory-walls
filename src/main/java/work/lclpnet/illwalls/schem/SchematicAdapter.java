package work.lclpnet.illwalls.schem;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public interface SchematicAdapter {

    BlockState getBlockState(BlockPos pos);

    BlockEntity getBlockEntity(BlockPos pos);
}
