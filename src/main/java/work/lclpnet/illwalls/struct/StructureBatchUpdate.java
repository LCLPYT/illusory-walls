package work.lclpnet.illwalls.struct;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;

import java.util.Map;

public interface StructureBatchUpdate {

    void beginBatch();

    void endBatch();

    Map<BlockPos, BlockState> getBatch();
}
