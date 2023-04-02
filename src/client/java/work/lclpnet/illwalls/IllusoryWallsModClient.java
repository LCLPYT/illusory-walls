package work.lclpnet.illwalls;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import work.lclpnet.illwalls.entity.ClientEntityManager;
import work.lclpnet.illwalls.network.ClientNetworkHandler;
import work.lclpnet.illwalls.render.IllusoryWallEntityRenderer;

public class IllusoryWallsModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(IllusoryWallsMod.ILLUSORY_WALL, IllusoryWallEntityRenderer::new);

        final var entityManager = new ClientEntityManager(IllusoryWallsMod.LOGGER);
        final var networkHandler = new ClientNetworkHandler(entityManager);

        networkHandler.init();
    }
}