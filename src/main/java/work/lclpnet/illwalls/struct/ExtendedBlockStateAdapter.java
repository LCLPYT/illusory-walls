package work.lclpnet.illwalls.struct;

import net.minecraft.block.Blocks;
import org.jetbrains.annotations.Nullable;
import work.lclpnet.kibu.mc.BlockState;
import work.lclpnet.kibu.schematic.FabricBlockStateAdapter;

/**
 * An extended version of {@link work.lclpnet.kibu.schematic.FabricBlockStateAdapter} that supports special block states,
 * such as {@link EmptyBlockState}.
 */
public class ExtendedBlockStateAdapter extends FabricBlockStateAdapter {

    protected ExtendedBlockStateAdapter() {}

    @Nullable
    @Override
    public BlockState getBlockState(String string) {
        // handle special block states
        if (EmptyBlockState.ID.equals(string)) {
            return EmptyBlockState.INSTANCE;
        }

        return super.getBlockState(string);
    }

    @Nullable
    @Override
    public net.minecraft.block.BlockState revert(BlockState state) {
        // handle special block states
        if (EmptyBlockState.INSTANCE == state) {
            return Blocks.AIR.getDefaultState();
        }

        return super.revert(state);
    }

    public static ExtendedBlockStateAdapter getInstance() {
        return InstanceHolder.instance;
    }

    private static final class InstanceHolder {
        private static final ExtendedBlockStateAdapter instance = new ExtendedBlockStateAdapter();
    }
}
