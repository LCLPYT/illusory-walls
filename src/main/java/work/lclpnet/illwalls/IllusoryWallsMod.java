package work.lclpnet.illwalls;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.Items;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.lclpnet.illwalls.entity.IllusoryWallEntity;
import work.lclpnet.illwalls.entity.StructureEntity;
import work.lclpnet.illwalls.wall.IllusoryWallLookup;
import work.lclpnet.illwalls.wall.NaiveWallLookup;
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
    public static final SchematicFormat SCHEMATIC_FORMAT = SchematicFormats.SPONGE_V2;
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

            final ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;

            if (entity == null) {
                IllusoryWallsMod.ILLUSORY_WALL_ENTITY.spawn(serverWorld, null, wall -> updateEntity(serverWorld, pos, wall, serverPlayer), pos, SpawnReason.SPAWN_EGG, false, false);
            } else {
                updateEntity(serverWorld, pos, entity, serverPlayer);
            }

            return ActionResult.CONSUME;
        });

        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (world.isClient || hand != Hand.MAIN_HAND) return ActionResult.PASS;

            ServerWorld serverWorld = (ServerWorld) world;
            var optWall = wallLookup.getWallAt(serverWorld, pos);
            if (optWall.isEmpty()) return ActionResult.PASS;

            var entity = optWall.get();
            entity.fade();

            return ActionResult.CONSUME;
        });
    }

    private void updateEntity(ServerWorld world, BlockPos pos, IllusoryWallEntity entity, ServerPlayerEntity invoker) {
        var structure = entity.getStructure();
        structure.setBlockState(pos, world.getBlockState(pos));

        Vec3d center = pos.toCenterPos();

        world.spawnParticles(invoker, new DustParticleEffect(Vec3d.unpackRgb(0x770077).toVector3f(), 0.6f),
                false, center.x, center.y, center.z, 100, 0.5f, 0.5f, 0.5f, 0.1);
    }
}