package work.lclpnet.illwalls.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import work.lclpnet.illwalls.structure.BlockStorage;
import work.lclpnet.illwalls.structure.BlockStructure;
import work.lclpnet.illwalls.structure.BlockStructureHandler;
import work.lclpnet.illwalls.structure.MapStructure;

import javax.annotation.Nonnull;
import java.util.Objects;

public class IllusoryWallEntity extends DisplayEntity.BlockDisplayEntity implements BlockStorage {

    public static final String FADING_NBT_KEY = "fading";
    private static final TrackedData<Boolean> FADING = DataTracker.registerData(IllusoryWallEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<BlockStructure> STRUCTURE = DataTracker.registerData(IllusoryWallEntity.class, BlockStructureHandler.BLOCK_STRUCTURE);
    public static final int FADE_DURATION_TICKS = 20;
    public static final int FADE_DURATION_MS = FADE_DURATION_TICKS * 50;

    @Environment(EnvType.CLIENT)
    private long fadeStartMs = 0L;
    private final BlockStructure structure = new MapStructure();

    public IllusoryWallEntity(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(FADING, false);
        this.dataTracker.startTracking(STRUCTURE, BlockStructure.EMPTY);
    }

    public boolean isFading() {
        return this.dataTracker.get(FADING);
    }

    public void setFading(boolean fading) {
        this.dataTracker.set(FADING, fading);

        if (world.isClient() && fading) {
            fadeStartMs = System.currentTimeMillis();
        }
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);

        if (world.isClient()) {
            if (data.equals(FADING) && isFading()) {
                fadeStartMs = System.currentTimeMillis();
            }
        }
    }

    @Environment(EnvType.CLIENT)
    public long getFadeStartMs() {
        return fadeStartMs;
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setFading(nbt.getBoolean(FADING_NBT_KEY));
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean(FADING_NBT_KEY, isFading());
    }

    @Override
    public void setBlockState(BlockPos pos, BlockState state) {
        Objects.requireNonNull(pos, "Position is null");
        Objects.requireNonNull(state, "State is null");

        structure.setBlockState(pos, state);

        this.dataTracker.set(STRUCTURE, structure);
    }

    @Override
    public @Nonnull BlockState getBlockState(BlockPos pos) {
        return structure.getBlockState(pos);
    }

    @Override
    public Iterable<BlockPos> getBlockPositions() {
        return structure.getBlockPositions();
    }
}
