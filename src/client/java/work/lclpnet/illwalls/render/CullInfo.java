package work.lclpnet.illwalls.render;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;

public record CullInfo(BlockGetter blockView, BlockPos pos) {
}
