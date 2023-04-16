package work.lclpnet.illwalls.struct;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import work.lclpnet.kibu.structure.BlockStructure;

import java.util.Objects;
import java.util.function.BiConsumer;

public class ListenerStructureWrapper extends FabricStructureWrapper {

    private final BiConsumer<BlockPos, BlockState> listener;

    public ListenerStructureWrapper(BlockStructure structure, BiConsumer<BlockPos, BlockState> listener) {
        super(structure);
        this.listener = Objects.requireNonNull(listener);
    }

    @Override
    public void setBlockState(BlockPos pos, BlockState state) {
        super.setBlockState(pos, state);

        this.listener.accept(pos, state);
    }
}
