package work.lclpnet.illwalls.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;

public class BlockIllusionEntity extends DisplayEntity.BlockDisplayEntity {

    public static final String FADING_NBT_KEY = "fading";
    private static final TrackedData<Boolean> FADING = DataTracker.registerData(BlockIllusionEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    public static final int FADE_DURATION_TICKS = 20;
    public static final int FADE_DURATION_MS = FADE_DURATION_TICKS * 50;

    @Environment(EnvType.CLIENT)
    private long fadeStartMs = 0L;

    public BlockIllusionEntity(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(FADING, false);
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

        if (world.isClient() && data.equals(FADING) && isFading()) {
            fadeStartMs = System.currentTimeMillis();
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
}
