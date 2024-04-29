package work.lclpnet.illwalls.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
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

    private void spawn(EntityExtraSpawnS2CPacket payload, ClientPlayNetworking.Context context) {
        final var world = context.player().clientWorld;

        // execute in main thread
        context.client().execute(() -> entityManager.spawnEntity(payload, world));
    }

    private void illusoryWallUpdate(StructureUpdateS2CPacket payload, ClientPlayNetworking.Context context) {
        final var world = context.player().clientWorld;

        context.client().execute(() -> entityManager.updateIllusoryWall(payload, world));
    }

    private void editWallScreen(EditWallScreenS2CPacket payload, ClientPlayNetworking.Context context) {
        final var world = context.player().clientWorld;
        MinecraftClient client = context.client();

        client.execute(() -> {
            int entityId = payload.entityId();
            IllusoryWallEntity wallEntity = null;

            if (entityId != -1) {
                Entity entity = world.getEntityById(entityId);

                if (entity instanceof IllusoryWallEntity) {
                    wallEntity = (IllusoryWallEntity) entity;
                }
            }

            EditWallScreen screen = new EditWallScreen(payload.settings(), wallEntity);
            client.setScreen(screen);
        });
    }
}
