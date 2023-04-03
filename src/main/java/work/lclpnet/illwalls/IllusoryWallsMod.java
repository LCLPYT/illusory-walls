package work.lclpnet.illwalls;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.lclpnet.illwalls.entity.IllusoryWallEntity;
import work.lclpnet.illwalls.wall.IllusoryWallLookup;
import work.lclpnet.illwalls.wall.NaiveWallLookup;

import javax.annotation.Nonnull;

public class IllusoryWallsMod implements ModInitializer, IllusoryWallsApi {

    public static final String MOD_ID = "illwalls";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final EntityType<IllusoryWallEntity> ILLUSORY_WALL = Registry.register(
            Registries.ENTITY_TYPE,
            identifier("illusory_wall"),
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, IllusoryWallEntity::new)
                    .dimensions(EntityDimensions.changing(0f, 0f))
                    .trackRangeChunks(10)
                    .trackedUpdateRate(1)
                    .build()
    );
    private static IllusoryWallsMod instance = null;
    private final IllusoryWallLookup wallLookup = new NaiveWallLookup();

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

        LOGGER.info("Initialized.");

        test();
    }

    @Override
    public IllusoryWallLookup lookup() {
        return wallLookup;
    }

    private void test() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient) return ActionResult.PASS;
            if (hand != Hand.MAIN_HAND || !player.getStackInHand(hand).isOf(Items.NETHER_STAR))
                return ActionResult.PASS;
            if (hitResult.getType() != HitResult.Type.BLOCK) return ActionResult.PASS;

            final var pos = hitResult.getBlockPos();
            final var serverWorld = (ServerWorld) world;

            if (wallLookup.getWallAt(serverWorld, pos).isPresent())
                return ActionResult.PASS;  // there is already a wall

            // check neighbours
            IllusoryWallEntity entity = null;
            var adjPos = new BlockPos.Mutable();

            for (Direction direction : Direction.values()) {
                adjPos.set(pos, direction);

                var opt = wallLookup.getWallAt(serverWorld, adjPos);
                if (opt.isEmpty()) continue;

                // found an adjacent wall
                entity = opt.get();
                break;
            }

            if (entity == null) {
                IllusoryWallsMod.ILLUSORY_WALL.spawn(serverWorld, null, wall -> updateEntity(world, pos, wall), pos, SpawnReason.SPAWN_EGG, false, false);
            } else {
                updateEntity(world, pos, entity);
            }

            return ActionResult.CONSUME;
        });
    }

    private void updateEntity(World world, BlockPos pos, IllusoryWallEntity entity) {
        var structure = entity.getStructure();
        structure.setBlockState(pos, world.getBlockState(pos));
    }
}