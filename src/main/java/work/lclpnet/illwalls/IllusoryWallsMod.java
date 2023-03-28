package work.lclpnet.illwalls;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.lclpnet.illwalls.entity.IllusoryWallEntity;
import work.lclpnet.illwalls.wall.IllusoryWallManager;
import work.lclpnet.illwalls.wall.IllusoryWallManagerImpl;

public class IllusoryWallsMod implements ModInitializer, IllusoryWallsApi {

    public static final String MOD_ID = "illwalls";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final EntityType<IllusoryWallEntity> BLOCK_ILLUSION = Registry.register(
            Registries.ENTITY_TYPE,
            identifier("block_illusion"),
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, IllusoryWallEntity::new)
                    .dimensions(EntityDimensions.changing(0f, 0f))
                    .trackRangeChunks(10)
                    .trackedUpdateRate(1)
                    .build()
    );
    static IllusoryWallsMod INSTANCE = null;
    private final IllusoryWallManager wallManager = new IllusoryWallManagerImpl();

    public static Identifier identifier(String path) {
        return new Identifier(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        INSTANCE = this;
        LOGGER.info("Initialized.");
    }

    @Override
    public IllusoryWallManager getWallManager() {
        return wallManager;
    }
}