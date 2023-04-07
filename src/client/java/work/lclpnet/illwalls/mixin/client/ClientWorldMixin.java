package work.lclpnet.illwalls.mixin.client;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import work.lclpnet.illwalls.entity.BlockRenderOverrideView;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin implements BlockRenderOverrideView {

    @Shadow @Final private WorldRenderer worldRenderer;
    @Unique
    private LongSet overrides = null;

    @Override
    public boolean illwalls$isOverridden(BlockPos pos) {
        return overrides != null && overrides.contains(pos.asLong());
    }

    @Override
    public void illwalls$addOverride(BlockPos pos) {
        if (overrides == null) {
            illwalls$initOverrides();
        }

        overrides.add(pos.asLong());
        ((WorldRendererAccessor) worldRenderer).invokeScheduleChunkRender(pos.getX(), pos.getY(), pos.getZ(), true);
    }

    @Override
    public void illwalls$removeOverride(BlockPos pos) {
        if (overrides == null) return;

        overrides.remove(pos.asLong());
        ((WorldRendererAccessor) worldRenderer).invokeScheduleChunkRender(pos.getX(), pos.getY(), pos.getZ(), true);
    }

    private synchronized void illwalls$initOverrides() {
        if (overrides != null) return;

        overrides = new LongOpenHashSet();
    }
}
