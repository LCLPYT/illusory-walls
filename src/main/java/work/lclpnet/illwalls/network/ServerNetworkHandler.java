package work.lclpnet.illwalls.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import work.lclpnet.illwalls.entity.IllusoryWallEntity;
import work.lclpnet.illwalls.util.PlayerInfo;
import work.lclpnet.illwalls.wall.IllusoryWallPlayerSettings;

import java.util.Collection;

import static net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver;

public class ServerNetworkHandler {

    public void init() {
        registerGlobalReceiver(ApplyWallSettingsC2SPacket.ID, this::applyWallSettings);
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

    public static void send(PacketSerializer packet, Collection<ServerPlayerEntity> players) {
        final var buf = PacketByteBufs.create();
        packet.writeTo(buf);

        players.forEach(player -> ServerPlayNetworking.send(player, packet.getIdentifier(), buf));
    }
}
