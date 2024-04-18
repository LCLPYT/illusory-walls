package work.lclpnet.illwalls.network;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import work.lclpnet.illwalls.entity.IllusoryWallEntity;
import work.lclpnet.illwalls.util.PlayerInfo;
import work.lclpnet.illwalls.wall.IllusoryWallManager;
import work.lclpnet.illwalls.wall.IllusoryWallPlayerSettings;

import java.util.Collection;

import static net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver;
import static work.lclpnet.illwalls.IllusoryWallsMod.STAFF_OF_ILLUSION_ITEM;

public class ServerNetworkHandler {

    private final IllusoryWallManager wallManager;

    public ServerNetworkHandler(IllusoryWallManager wallManager) {
        this.wallManager = wallManager;
    }

    public void init() {
        registerGlobalReceiver(ApplyWallSettingsC2SPacket.ID, this::applyWallSettings);
        registerGlobalReceiver(AttackBlockAdventureC2SPacket.ID, this::attackBlockAdventure);
    }

    private void applyWallSettings(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        if (!player.isCreativeLevelTwoOp()) return;

        final var packet = new ApplyWallSettingsC2SPacket(buf);
        final ServerWorld world = player.getServerWorld();

        server.execute(() -> {
            int entityId = packet.getEntityId();
            IllusoryWallEntity wallEntity = null;

            if (entityId != -1) {
                Entity entity = world.getEntityById(entityId);

                if (entity instanceof IllusoryWallEntity) {
                    wallEntity = (IllusoryWallEntity) entity;
                }
            }

            final IllusoryWallPlayerSettings settings = packet.getSettings();

            if (wallEntity != null) {
                settings.applyTo(wallEntity);
            } else {
                PlayerInfo.get(player).getWallSettings().copyFrom(settings);
            }
        });
    }

    private <T extends FabricPacket> void attackBlockAdventure(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        final var packet = new AttackBlockAdventureC2SPacket(buf);
        final ServerWorld world = player.getServerWorld();

        server.submit(() -> {
            BlockPos pos = packet.getPos();

            if (player.isSpectator()) {
                // spectators should not be able to trigger illusory walls
                return;
            }

            if (player.getEyePos().squaredDistanceTo(Vec3d.ofCenter(pos)) > ServerPlayNetworkHandler.MAX_BREAK_SQUARED_DISTANCE) {
                // too far
                return;
            }

            if (!player.isBlockBreakingRestricted(world, pos, player.interactionManager.getGameMode())) {
                // handled by default attack block packet
                return;
            }

            ItemStack stack = player.getMainHandStack();

            if (stack.isOf(STAFF_OF_ILLUSION_ITEM)) {
                // staff of illusion has different behaviour
                return;
            }

            Direction direction = packet.getDirection();
            BlockPos from = pos.offset(direction);

            wallManager.fadeWallAtIfPresent(world, pos, from);
        });
    }

    public static void send(PacketSerializer packet, Collection<ServerPlayerEntity> players) {
        final var buf = PacketByteBufs.create();
        packet.writeTo(buf);

        players.forEach(player -> ServerPlayNetworking.send(player, packet.getIdentifier(), buf));
    }
}
