package work.lclpnet.illwalls.impl;

import com.google.common.collect.Streams;
import net.minecraft.SharedConstants;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import work.lclpnet.kibu.structure.BlockStructure;
import work.lclpnet.kibu.structure.SimpleBlockStructure;

import javax.annotation.Nonnull;
import java.util.Objects;

public class FabricStructureWrapper implements FabricStructureView {

    protected final FabricBlockStateAdapter adapter = FabricBlockStateAdapter.getInstance();
    private final BlockStructure structure;

    public FabricStructureWrapper(BlockStructure structure) {
        this.structure = Objects.requireNonNull(structure);
    }

    public static SimpleBlockStructure createSimpleStructure() {
        int dataVersion = SharedConstants.getGameVersion().getSaveVersion().getId();
        return new SimpleBlockStructure(dataVersion);
    }

    @Nonnull
    public BlockStructure getStructure() {
        return structure;
    }

    public void setBlockState(BlockPos pos, BlockState state) {
        structure.setBlockState(adapter.adapt(pos), adapter.adapt(state));
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        var state = structure.getBlockState(adapter.adapt(pos));
        return state != null ? adapter.revert(state) : null;
    }

    @Override
    public Iterable<BlockPos> getBlockPositions() {
        return Streams.stream(structure.getBlockPositions())
                .map(adapter::revert)
                .toList();
    }
}
