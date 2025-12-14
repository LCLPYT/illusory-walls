package work.lclpnet.illwalls;

import net.fabricmc.api.ModInitializer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Rarity;
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
    private static final ResourceKey<EntityType<?>>
            ILLUSORY_WALL_ENTITY_KEY = ResourceKey.create(Registries.ENTITY_TYPE, identifier("illusory_wall")),
            STRUCTURE_ENTITY_KEY = ResourceKey.create(Registries.ENTITY_TYPE, identifier("structure"));

    public static final EntityType<IllusoryWallEntity> ILLUSORY_WALL_ENTITY = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            ILLUSORY_WALL_ENTITY_KEY,
            EntityType.Builder.of(IllusoryWallEntity::new, MobCategory.MISC)
                    .sized(0f, 0f)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .build(ILLUSORY_WALL_ENTITY_KEY));

    public static final EntityType<StructureEntity> STRUCTURE_ENTITY = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            STRUCTURE_ENTITY_KEY,
            EntityType.Builder.of(StructureEntity::new, MobCategory.MISC)
                    .sized(0f, 0f)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .build(STRUCTURE_ENTITY_KEY));

    // items
    private static final ResourceKey<Item>
            STAFF_OF_ILLUSION_ITEM_KEY = ResourceKey.create(Registries.ITEM, identifier("staff_of_illusion"));

    public static final StaffOfIllusionItem STAFF_OF_ILLUSION_ITEM = Registry.register(
            BuiltInRegistries.ITEM,
            STAFF_OF_ILLUSION_ITEM_KEY,
            new StaffOfIllusionItem(new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.EPIC)
                    .setId(STAFF_OF_ILLUSION_ITEM_KEY)));

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

    public static ResourceLocation identifier(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
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

    private static SoundEvent registerSound(ResourceLocation id) {
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }
}