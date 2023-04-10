package work.lclpnet.illwalls;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.lclpnet.illwalls.entity.IllusoryWallEntity;
import work.lclpnet.illwalls.entity.StructureEntity;
import work.lclpnet.illwalls.item.StaffOfIllusionItem;
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
    public static final EntityType<IllusoryWallEntity> ILLUSORY_WALL_ENTITY = Registry.register(
            Registries.ENTITY_TYPE,
            identifier("illusory_wall"),
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, IllusoryWallEntity::new)
                    .dimensions(EntityDimensions.changing(0f, 0f))
                    .trackRangeChunks(10)
                    .trackedUpdateRate(1)
                    .build()
    );
    public static final EntityType<StructureEntity> STRUCTURE_ENTITY = Registry.register(
            Registries.ENTITY_TYPE,
            identifier("structure"),
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, StructureEntity::new)
                    .dimensions(EntityDimensions.changing(0f, 0f))
                    .trackRangeChunks(10)
                    .trackedUpdateRate(1)
                    .build()
    );
    public static final StaffOfIllusionItem STAFF_OF_ILLUSION_ITEM = Registry.register(
            Registries.ITEM,
            identifier("staff_of_illusion"),
            new StaffOfIllusionItem(new FabricItemSettings().maxCount(1).rarity(Rarity.EPIC))
    );
    public static final SchematicFormat SCHEMATIC_FORMAT = SchematicFormats.SPONGE_V2;
    private static IllusoryWallsMod instance = null;
    private final IllusoryWallLookup wallLookup;
    private final IllusoryWallManager wallManager;

    public IllusoryWallsMod() {
        this.wallLookup = new NaiveWallLookup();
        this.wallManager = new SimpleIllusoryWallManager(wallLookup);
    }

    public static Identifier identifier(String path) {
        return new Identifier(MOD_ID, path);
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

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.OPERATOR).register(entries -> {
            //noinspection UnstableApiUsage
            if (!entries.getContext().hasPermissions()) return;

            entries.add(STAFF_OF_ILLUSION_ITEM);
        });

        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (world.isClient || hand != Hand.MAIN_HAND) return ActionResult.PASS;

            ItemStack stack = player.getStackInHand(hand);
            if (stack.isOf(STAFF_OF_ILLUSION_ITEM)) return ActionResult.PASS;

            boolean success = wallManager.fadeWallAtIfPresent((ServerWorld) world, pos);
            return success ? ActionResult.CONSUME : ActionResult.PASS;
        });

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
}