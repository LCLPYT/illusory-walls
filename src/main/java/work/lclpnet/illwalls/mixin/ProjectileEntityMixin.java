package work.lclpnet.illwalls.mixin;

import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import work.lclpnet.illwalls.IllusoryWallsApi;
import work.lclpnet.illwalls.wall.IllusoryWallManager;

@Mixin(ProjectileEntity.class)
public class ProjectileEntityMixin {

    @Inject(
            method = "onBlockHit",
            at = @At("TAIL")
    )
    public void illwalls$onBlockHit(BlockHitResult blockHitResult, CallbackInfo ci) {
        ProjectileEntity self = (ProjectileEntity) (Object) this;
        if (self.world.isClient) return;

        BlockPos pos = blockHitResult.getBlockPos();
        IllusoryWallManager manager = IllusoryWallsApi.getInstance().manager();

        manager.fadeWallAtIfPresent((ServerWorld) self.world, pos);
    }
}
