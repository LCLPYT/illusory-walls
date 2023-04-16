package work.lclpnet.illwalls.struct;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

import java.util.Map;

public interface StructureBatchUpdate {

    void beginBatch();

    void endBatch();

    Map<BlockPos, BlockState> getBatch();
}
