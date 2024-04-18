package work.lclpnet.illwalls.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import work.lclpnet.illwalls.entity.ClientEntityManager;
import work.lclpnet.illwalls.entity.IllusoryWallEntity;
import work.lclpnet.illwalls.screen.EditWallScreen;

import static net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver;

public class ClientNetworkHandler {

    private final ClientEntityManager entityManager;

    public ClientNetworkHandler(ClientEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void init() {
        registerGlobalReceiver(EntityExtraSpawnS2CPacket.ID, this::spawn);
        registerGlobalReceiver(StructureUpdateS2CPacket.ID, this::illusoryWallUpdate);
        registerGlobalReceiver(EditWallScreenS2CPacket.ID, this::editWallScreen);
    }

    private void spawn(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        final var packet = new EntityExtraSpawnS2CPacket(buf);
        final var world = handler.getWorld();

        // execute in main thread
        client.execute(() -> entityManager.spawnEntity(packet, world));
    }

    private void illusoryWallUpdate(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        final var packet = new StructureUpdateS2CPacket(buf);
        final var world = handler.getWorld();

        client.execute(() -> entityManager.updateIllusoryWall(packet, world));
    }

    private void editWallScreen(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        final var packet = new EditWallScreenS2CPacket(buf);
        final var world = handler.getWorld();

        client.execute(() -> {
            int entityId = packet.getEntityId();
            IllusoryWallEntity wallEntity = null;

            if (entityId != -1) {
                Entity entity = world.getEntityById(entityId);

                if (entity instanceof IllusoryWallEntity) {
                    wallEntity = (IllusoryWallEntity) entity;
                }
            }

            EditWallScreen screen = new EditWallScreen(packet.getSettings(), wallEntity);
            client.setScreen(screen);
        });
    }

    public static void send(PacketSerializer packet) {
        final var buf = PacketByteBufs.create();

        packet.writeTo(buf);

        ClientPlayNetworking.send(packet.getIdentifier(), buf);
    }
}
