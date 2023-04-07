package work.lclpnet.illwalls.mixin.client;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import work.lclpnet.illwalls.entity.BlockRenderOverrideView;

@Mixin(targets = "net.minecraft.client.render.chunk.RenderedChunk")
public class RenderedChunkMixin {

    @Unique
    private BlockRenderOverrideView overrideView;

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    public void onInit(WorldChunk chunk, CallbackInfo ci) {
        overrideView = (BlockRenderOverrideView) chunk.getWorld();
    }

    @Inject(
            method = "getBlockState",
            at = @At("HEAD"),
            cancellable = true
    )
    public void illswalls$illusoryWallBlockState(BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        if (overrideView.illwalls$isOverridden(pos)) {
            System.out.println("Overridden " + pos);
            cir.setReturnValue(Blocks.AIR.getDefaultState());
        }
    }
}
