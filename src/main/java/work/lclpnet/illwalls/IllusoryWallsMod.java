package work.lclpnet.illwalls;

import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.lclpnet.illwalls.entity.IllusoryWallEntity;
import work.lclpnet.illwalls.entity.StructureEntity;
import work.lclpnet.illwalls.event.ModEventListener;
import work.lclpnet.illwalls.item.StaffOfIllusionItem;
import work.lclpnet.illwalls.network.ServerNetworkHandler;
import work.lclpnet.illwalls.wall.IllusoryWallLookup;
import work.lclpnet.illwalls.wall.IllusoryWallManager;
import work.lclpnet.illwalls.wall.NaiveWallLookup;
import work.lclpnet.illwalls.wall.SimpleIllusoryWallManager;
import work.lclpnet.kibu.schematic.SchematicFormats;
import work.lclpnet.kibu.schematic.api.SchematicFormat;

import javax.annotation.Nonnull;

public class IllusoryWallsMod implements ModInitializer, IllusoryWallsApi {

    public static final String MOD_ID = "illwalls";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // entity types
    private static final RegistryKey<EntityType<?>>
            ILLUSORY_WALL_ENTITY_KEY = RegistryKey.of(RegistryKeys.ENTITY_TYPE, identifier("illusory_wall")),
            STRUCTURE_ENTITY_KEY = RegistryKey.of(RegistryKeys.ENTITY_TYPE, identifier("structure"));

    public static final EntityType<IllusoryWallEntity> ILLUSORY_WALL_ENTITY = Registry.register(
            Registries.ENTITY_TYPE,
            ILLUSORY_WALL_ENTITY_KEY,
            EntityType.Builder.create(IllusoryWallEntity::new, SpawnGroup.MISC)
                    .dimensions(0f, 0f)
                    .maxTrackingRange(10)
                    .trackingTickInterval(1)
                    .build(ILLUSORY_WALL_ENTITY_KEY));

    public static final EntityType<StructureEntity> STRUCTURE_ENTITY = Registry.register(
            Registries.ENTITY_TYPE,
            STRUCTURE_ENTITY_KEY,
            EntityType.Builder.create(StructureEntity::new, SpawnGroup.MISC)
                    .dimensions(0f, 0f)
                    .maxTrackingRange(10)
                    .trackingTickInterval(1)
                    .build(STRUCTURE_ENTITY_KEY));

    // items
    private static final RegistryKey<Item>
            STAFF_OF_ILLUSION_ITEM_KEY = RegistryKey.of(RegistryKeys.ITEM, identifier("staff_of_illusion"));

    public static final StaffOfIllusionItem STAFF_OF_ILLUSION_ITEM = Registry.register(
            Registries.ITEM,
            STAFF_OF_ILLUSION_ITEM_KEY,
            new StaffOfIllusionItem(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
                    .registryKey(STAFF_OF_ILLUSION_ITEM_KEY)));

    // sounds
    public static final SoundEvent ILLUSORY_WALL_FADE_SOUND = registerSound(identifier("entity.illusory_wall.fade"));

    public static final SchematicFormat SCHEMATIC_FORMAT = SchematicFormats.SPONGE_V2;
    private static IllusoryWallsMod instance = null;
    private final IllusoryWallLookup wallLookup;
    private final IllusoryWallManager wallManager;

    public IllusoryWallsMod() {
        this.wallLookup = new NaiveWallLookup();
        this.wallManager = new SimpleIllusoryWallManager(wallLookup);
    }

    public static Identifier identifier(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Nonnull
    static IllusoryWallsMod getInstance() {
        synchronized (IllusoryWallsMod.class) {
            if (instance == null) throw new IllegalStateException("Called too early");
            return instance;
        }
    }

    @Override
    public void onInitialize() {
        synchronized (IllusoryWallsMod.class) {
            instance = this;
        }

        new ModEventListener(wallManager, wallLookup).register();
        new ServerNetworkHandler(wallManager).init();

        LOGGER.info("Initialized.");
    }

    @Override
    public IllusoryWallLookup lookup() {
        return wallLookup;
    }

    @Override
    public IllusoryWallManager manager() {
        return wallManager;
    }

    private static SoundEvent registerSound(Identifier id) {
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }
}