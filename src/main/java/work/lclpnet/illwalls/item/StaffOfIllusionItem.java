package work.lclpnet.illwalls.item;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import work.lclpnet.illwalls.IllusoryWallsApi;
import work.lclpnet.illwalls.entity.IllusoryWallEntity;
import work.lclpnet.illwalls.network.EditWallScreenS2CPacket;
import work.lclpnet.illwalls.wall.IllusoryWallLookup;
import work.lclpnet.illwalls.wall.IllusoryWallManager;

import javax.annotation.Nullable;

public class StaffOfIllusionItem extends Item {

    public StaffOfIllusionItem(Properties settings) {
        super(settings);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        if (!user.isShiftKeyDown()) return super.use(world, user, hand);

        if (!world.isClientSide() && user instanceof ServerPlayer player) {
            openEditScreen(player, null);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean canDestroyBlock(ItemStack stack, BlockState state, Level world, BlockPos pos, LivingEntity user) {
        if (!world.isClientSide() && user instanceof ServerPlayer player) {
            this.destroyIllusoryWall(player, (ServerLevel) world, pos);
        }

        return false;
    }

    private void destroyIllusoryWall(ServerPlayer player, ServerLevel world, BlockPos pos) {
        if (!player.canUseGameMasterBlocks()) return;

        IllusoryWallManager wallManager = IllusoryWallsApi.getInstance().manager();
        if (!wallManager.removeIllusoryBlock(world, pos)) return;

        // spawn visual particles
        Vec3 center = pos.getCenter();

        world.sendParticles(player, ParticleTypes.FLAME, false, false,
                center.x, center.y, center.z, 25, 0.5f, 0.5f, 0.5f, 0.05);
    }

    private static void openEditScreen(ServerPlayer player, @Nullable IllusoryWallEntity wall) {
        CustomPacketPayload packet;

        if (wall == null) {
            packet = new EditWallScreenS2CPacket(player);
        } else {
            packet = new EditWallScreenS2CPacket(wall);
        }

        ServerPlayNetworking.send(player, packet);
    }

    @Nullable
    public static InteractionResult onRightClickBlockEarlyServer(ServerPlayer player, ServerLevel world, BlockPos pos) {
        // fired before vanilla block click handlers

        if (player.isShiftKeyDown()) {
            IllusoryWallLookup lookup = IllusoryWallsApi.getInstance().lookup();
            openEditScreen(player, lookup.getWallAt(world, pos).orElse(null));

            return null;
        }

        if (!makeIllusoryWall(player, world, pos)) {
            return InteractionResult.FAIL;
        }

        return null;
    }

    private static boolean makeIllusoryWall(ServerPlayer player, ServerLevel world, BlockPos pos) {
        if (!player.canUseGameMasterBlocks()) return false;

        IllusoryWallManager wallManager = IllusoryWallsApi.getInstance().manager();
        boolean created = wallManager.makeBlockIllusory(world, pos, player);

        if (created) {
            Vec3 center = pos.getCenter();

            DustParticleOptions effect = new DustParticleOptions(0x770077, 0.6f);
            world.sendParticles(player, effect, false, false,
                    center.x, center.y, center.z, 100, 0.5f, 0.5f, 0.5f, 0.1);
        }

        return created;
    }
}
