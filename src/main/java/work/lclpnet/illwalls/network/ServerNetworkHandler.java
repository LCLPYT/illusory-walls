package work.lclpnet.illwalls.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;

public class ServerNetworkHandler {

    public static void send(PacketSerializer packet, Collection<ServerPlayerEntity> players) {
        final var buf = PacketByteBufs.create();
        packet.writeTo(buf);

        players.forEach(player -> ServerPlayNetworking.send(player, packet.getIdentifier(), buf));
    }
}
