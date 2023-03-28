package work.lclpnet.illwalls.structure;

import net.minecraft.block.BlockState;

import javax.annotation.Nullable;

public interface BlockStateGetter {

    @Nullable
    BlockState getBlockState(String string);
}
