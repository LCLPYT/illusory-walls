package work.lclpnet.illwalls.item;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import work.lclpnet.illwalls.IllusoryWallsApi;
import work.lclpnet.illwalls.entity.IllusoryWallEntity;
import work.lclpnet.illwalls.network.EditWallScreenS2CPacket;
import work.lclpnet.illwalls.network.PacketSerializer;
import work.lclpnet.illwalls.wall.IllusoryWallLookup;
import work.lclpnet.illwalls.wall.IllusoryWallManager;

import javax.annotation.Nullable;

public class StaffOfIllusionItem extends Item {

    public StaffOfIllusionItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!user.isSneaking()) return super.use(world, user, hand);

        if (!world.isClient && user instanceof ServerPlayerEntity player) {
            openEditScreen(player, null);
        }

        return TypedActionResult.success(user.getStackInHand(hand), world.isClient);
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
        if (!wallManager.removeIllusoryBlock(world, pos)) return;

        // spawn visual particles
        Vec3d center = pos.toCenterPos();

        world.spawnParticles(player, ParticleTypes.FLAME,
                false, center.x, center.y, center.z, 25, 0.5f, 0.5f, 0.5f, 0.05);
    }

    private static void openEditScreen(ServerPlayerEntity player, @Nullable IllusoryWallEntity wall) {
        PacketSerializer packet;

        if (wall == null) {
            packet = new EditWallScreenS2CPacket(player);
        } else {
            packet = new EditWallScreenS2CPacket(wall);
        }

        player.networkHandler.sendPacket(packet.toVanillaS2CPacket());
    }

    @Nullable
    public static ActionResult onRightClickBlockEarlyServer(ServerPlayerEntity player, ServerWorld world, BlockPos pos) {
        // fired before vanilla block click handlers

        if (player.isSneaking()) {
            IllusoryWallLookup lookup = IllusoryWallsApi.getInstance().lookup();
            openEditScreen(player, lookup.getWallAt(world, pos).orElse(null));

            return null;
        }

        if (!makeIllusoryWall(player, world, pos)) {
            return ActionResult.FAIL;
        }

        return null;
    }

    private static boolean makeIllusoryWall(ServerPlayerEntity player, ServerWorld world, BlockPos pos) {
        if (!player.isCreativeLevelTwoOp()) return false;

        IllusoryWallManager wallManager = IllusoryWallsApi.getInstance().manager();
        boolean created = wallManager.makeBlockIllusory(world, pos, player);

        if (created) {
            Vec3d center = pos.toCenterPos();

            DustParticleEffect effect = new DustParticleEffect(Vec3d.unpackRgb(0x770077).toVector3f(), 0.6f);
            world.spawnParticles(player, effect, false, center.x, center.y, center.z, 100,
                    0.5f, 0.5f, 0.5f, 0.1);
        }

        return created;
    }
}
