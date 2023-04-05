package work.lclpnet.illwalls.impl;

import com.google.common.collect.Streams;
import net.minecraft.SharedConstants;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.Nullable;
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

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        return null;  // no block entities
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return Fluids.EMPTY.getDefaultState();  // no fluids atm
    }

    @Override
    public int getHeight() {
        return structure.getHeight();
    }

    @Override
    public int getBottomY() {
        return structure.getOrigin().getY();
    }

    public boolean isInBounds(Vec3i pos) {
        var state = structure.getBlockState(adapter.adapt(pos));
        return state != null && !state.isAir();
    }

    public BlockPos getCenter() {
        var origin = structure.getOrigin();

        return new BlockPos(
                origin.getX() + structure.getWidth() / 2,
                origin.getY() + structure.getHeight() / 2,
                origin.getZ() + structure.getLength() / 2
        );
    }
}
