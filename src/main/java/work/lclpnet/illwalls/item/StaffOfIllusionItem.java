package work.lclpnet.illwalls.item;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import work.lclpnet.illwalls.IllusoryWallsApi;
import work.lclpnet.illwalls.wall.IllusoryWallManager;

public class StaffOfIllusionItem extends Item {

    public StaffOfIllusionItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();

        if (!world.isClient && player != null && !makeIllusoryWall((ServerPlayerEntity) player, (ServerWorld) world, pos)) {
            return ActionResult.FAIL;
        }

        return ActionResult.success(world.isClient);
    }

    @Override
    public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) {
        if (!world.isClient) {
            this.destroyIllusoryWall((ServerPlayerEntity) miner, (ServerWorld) world, pos);
        }

        return false;
    }

    private void destroyIllusoryWall(ServerPlayerEntity player, ServerWorld world, BlockPos pos) {
        if (!player.isCreativeLevelTwoOp()) return;

        IllusoryWallManager wallManager = IllusoryWallsApi.getInstance().manager();
        wallManager.removeIllusoryBlock(world, pos);

        // spawn visual particles
        Vec3d center = pos.toCenterPos();

        world.spawnParticles(player, ParticleTypes.FLAME,
                false, center.x, center.y, center.z, 25, 0.5f, 0.5f, 0.5f, 0.05);
    }

    private boolean makeIllusoryWall(ServerPlayerEntity player, ServerWorld world, BlockPos pos) {
        if (!player.isCreativeLevelTwoOp()) return false;

        IllusoryWallManager wallManager = IllusoryWallsApi.getInstance().manager();
        boolean created = wallManager.makeBlockIllusory(world, pos);

        if (created) {
            Vec3d center = pos.toCenterPos();

            DustParticleEffect effect = new DustParticleEffect(Vec3d.unpackRgb(0x770077).toVector3f(), 0.6f);
            world.spawnParticles(player, effect, false, center.x, center.y, center.z, 100,
                    0.5f, 0.5f, 0.5f, 0.1);
        }

        return created;
    }
}
