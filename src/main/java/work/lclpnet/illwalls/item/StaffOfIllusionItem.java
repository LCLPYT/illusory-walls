package work.lclpnet.illwalls.item;

import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import work.lclpnet.illwalls.IllusoryWallsApi;
import work.lclpnet.illwalls.IllusoryWallsMod;
import work.lclpnet.illwalls.entity.IllusoryWallEntity;
import work.lclpnet.illwalls.wall.IllusoryWallLookup;

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

        if (!world.isClient && player != null && !use((ServerPlayerEntity) player, (ServerWorld) world, pos)) {
            return ActionResult.FAIL;
        }

        return ActionResult.success(world.isClient);
    }

    private boolean use(ServerPlayerEntity player, ServerWorld world, BlockPos pos) {
        if (!player.isCreativeLevelTwoOp()) return false;

        IllusoryWallLookup wallLookup = IllusoryWallsApi.getInstance().lookup();

        if (wallLookup.getWallAt(world, pos).isPresent())
            return false;  // there is already a wall

        // check neighbours
        IllusoryWallEntity entity = null;
        var adjPos = new BlockPos.Mutable();

        for (Direction direction : Direction.values()) {
            adjPos.set(pos, direction);

            var opt = wallLookup.getWallAt(world, adjPos);
            if (opt.isEmpty()) continue;

            // found an adjacent wall
            entity = opt.get();
            break;
        }

        if (entity == null) {
            IllusoryWallsMod.ILLUSORY_WALL_ENTITY.spawn(world, null, wall -> updateEntity(world, pos, wall, player), pos, SpawnReason.SPAWN_EGG, false, false);
        } else {
            updateEntity(world, pos, entity, player);
        }

        return true;
    }

    private void updateEntity(ServerWorld world, BlockPos pos, IllusoryWallEntity entity, ServerPlayerEntity invoker) {
        var structure = entity.getStructureContainer().getWrapper();
        structure.setBlockState(pos, world.getBlockState(pos));

        Vec3d center = pos.toCenterPos();

        world.spawnParticles(invoker, new DustParticleEffect(Vec3d.unpackRgb(0x770077).toVector3f(), 0.6f),
                false, center.x, center.y, center.z, 100, 0.5f, 0.5f, 0.5f, 0.1);
    }
}
