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
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.lclpnet.illwalls.entity.IllusoryWallEntity;

import java.util.Comparator;

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
    static IllusoryWallsMod INSTANCE = null;

    public static Identifier identifier(String path) {
        return new Identifier(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        INSTANCE = this;
        LOGGER.info("Initialized.");

        test();
    }

    private static void updateEntity(World world, BlockPos pos, IllusoryWallEntity entity) {
        var structure = entity.getStructure();
        structure.setBlockState(pos, world.getBlockState(pos));
    }

    private void test() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient) return ActionResult.PASS;
            if (hand != Hand.MAIN_HAND || !player.getStackInHand(hand).isOf(Items.NETHER_STAR))
                return ActionResult.PASS;
            if (hitResult.getType() != HitResult.Type.BLOCK) return ActionResult.PASS;

            var pos = hitResult.getBlockPos();

            Vec3d center = pos.toCenterPos();

            ServerWorld sw = (ServerWorld) world;
            Box box = Box.of(center, 3, 3, 3);
            var entities = sw.getEntitiesByClass(IllusoryWallEntity.class, box, e -> true);
            var closest = entities.stream().min(Comparator.comparing(entity -> entity.squaredDistanceTo(center)));

            if (closest.isEmpty()) {
                IllusoryWallsMod.ILLUSORY_WALL.spawn(sw, null, entity -> updateEntity(world, pos, entity), pos, SpawnReason.SPAWN_EGG, false, false);
            } else {
                updateEntity(world, pos, closest.get());
            }

            return ActionResult.CONSUME;
        });
    }
}