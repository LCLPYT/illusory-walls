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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.lclpnet.illwalls.entity.IllusoryWallEntity;

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

    private void test() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient) return ActionResult.PASS;
            if (hand != Hand.MAIN_HAND || !player.getStackInHand(hand).isOf(Items.NETHER_STAR))
                return ActionResult.PASS;
            if (hitResult.getType() != HitResult.Type.BLOCK) return ActionResult.PASS;

            var pos = hitResult.getBlockPos();

            IllusoryWallsMod.ILLUSORY_WALL.spawn((ServerWorld) world, null, entity -> {
                var structure = entity.getStructure();
                structure.setBlockState(pos, world.getBlockState(pos));
//                structure.setBlockState(pos.add(0, 1, 0), Blocks.GLASS.getDefaultState());
//                structure.setBlockState(pos.add(1, 0, 0), Blocks.ACACIA_STAIRS.getDefaultState().with(StairsBlock.SHAPE, StairShape.OUTER_LEFT));
            }, pos, SpawnReason.SPAWN_EGG, false, false);

            return ActionResult.CONSUME;
        });
    }
}