package work.lclpnet.illwalls.entity;

import net.minecraft.util.math.BlockPos;

public interface BlockRenderOverrideView {

    boolean illwalls$isOverridden(BlockPos pos);

    void illwalls$addOverride(BlockPos pos);

    void illwalls$removeOverride(BlockPos pos);
}
