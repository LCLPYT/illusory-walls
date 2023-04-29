package work.lclpnet.illwalls.struct;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import work.lclpnet.kibu.structure.BlockStructure;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

public class ListenerStructureWrapper extends ExtendedStructureWrapper implements StructureBatchUpdate {

    private final BiConsumer<BlockPos, BlockState> listener;
    private Map<BlockPos, BlockState> batch = null;

    public ListenerStructureWrapper(BlockStructure structure, BiConsumer<BlockPos, BlockState> listener) {
        super(structure);
        this.listener = Objects.requireNonNull(listener);
    }

    @Override
    public void setBlockState(BlockPos pos, BlockState state) {
        super.setBlockState(pos, state);

        if (batch != null) {
            batch.put(pos, state);
        } else {
            this.listener.accept(pos, state);
        }
    }

    @Override
    public void beginBatch() {
        this.batch = new HashMap<>();
    }

    @Override
    public void endBatch() {
        this.listener.accept(null, null);
        this.batch = null;
    }

    @Override
    public Map<BlockPos, BlockState> getBatch() {
        return batch;
    }
}
