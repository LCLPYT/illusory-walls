package work.lclpnet.illwalls;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import work.lclpnet.illwalls.render.BlockIllusionEntityRenderer;

public class IllusoryWallsClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(IllusoryWalls.BLOCK_ILLUSION, BlockIllusionEntityRenderer::new);
	}
}