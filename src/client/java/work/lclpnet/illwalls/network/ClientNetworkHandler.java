package work.lclpnet.illwalls.network;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import work.lclpnet.illwalls.entity.ClientEntityManager;

import static net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver;

public class ClientNetworkHandler {

    private final ClientEntityManager entityManager;

    public ClientNetworkHandler(ClientEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void init() {
        registerGlobalReceiver(EntityExtraSpawnPacket.ID, this::spawn);
        registerGlobalReceiver(IllusoryWallUpdatePacket.ID, this::illusoryWallUpdate);
    }

    private void illusoryWallUpdate(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        final var packet = new IllusoryWallUpdatePacket(buf);
        final var world = handler.getWorld();

        client.execute(() -> entityManager.updateIllusoryWall(packet, world));
    }

    private void spawn(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        final var packet = new EntityExtraSpawnPacket(buf);
        final var world = handler.getWorld();

        // execute in main thread
        client.execute(() -> entityManager.spawnEntity(packet, world));
    }
}
