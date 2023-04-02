package work.lclpnet.illwalls.render;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public record CullInfo(BlockView blockView, BlockPos pos) {
}
