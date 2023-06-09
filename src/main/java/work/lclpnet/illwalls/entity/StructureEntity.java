package work.lclpnet.illwalls.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import work.lclpnet.illwalls.IllusoryWallsMod;
import work.lclpnet.illwalls.network.EntityExtraSpawnPacket;
import work.lclpnet.illwalls.network.PacketBufUtils;
import work.lclpnet.illwalls.struct.ExtendedBlockStateAdapter;
import work.lclpnet.illwalls.struct.StructureContainer;
import work.lclpnet.illwalls.struct.StructureHolder;
import work.lclpnet.kibu.jnbt.CompoundTag;
import work.lclpnet.kibu.nbt.FabricNbtConversion;
import work.lclpnet.kibu.structure.BlockStructure;

import javax.annotation.Nullable;
import java.util.Optional;

public class StructureEntity extends Entity implements ExtraSpawnData, StructureHolder {

    public static final String
            FADING_NBT_KEY = "fading",
            VIEW_RANGE_NBT_KEY = "view_range",
            STRUCTURE_NBT_KEY = "structure";
    private static final TrackedData<Boolean> FADING = DataTracker.registerData(StructureEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Float> VIEW_RANGE = DataTracker.registerData(StructureEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Optional<BlockPos>> FADING_FROM = DataTracker.registerData(StructureEntity.class, TrackedDataHandlerRegistry.OPTIONAL_BLOCK_POS);

    private transient int fadeEnd = 0;
    @Environment(EnvType.CLIENT)
    private transient long fadeStartMs = 0L;
    private final StructureContainer structureContainer = new StructureContainer(this);

    public StructureEntity(EntityType<?> entityType, World world) {
        super(entityType, world);
        this.ignoreCameraFrustum = true;
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(FADING, false);
        this.dataTracker.startTracking(VIEW_RANGE, 1f);
        this.dataTracker.startTracking(FADING_FROM, Optional.empty());
    }

    public boolean isFading() {
        return this.dataTracker.get(FADING);
    }

    public void setFading(boolean fading) {
        boolean wasFading = isFading();
        this.dataTracker.set(FADING, fading);

        if (!wasFading && fading) {
            startFading();
        }
    }

    @Nullable
    public BlockPos getFadingFrom() {
        return this.dataTracker.get(FADING_FROM).orElse(null);
    }

    public void setFadingFrom(@Nullable BlockPos pos) {
        this.dataTracker.set(FADING_FROM, Optional.ofNullable(pos));
    }

    private void startFading() {
        fadeEnd = age + IllusoryWallEntity.FADE_DURATION_TICKS;

        if (getWorld().isClient()) {
            fadeStartMs = System.currentTimeMillis();
        }
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);

        if (data.equals(FADING) && isFading()) {
            startFading();
        }
    }

    @Environment(EnvType.CLIENT)
    public long getFadeStartMs() {
        return fadeStartMs;
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        this.setFading(nbt.getBoolean(FADING_NBT_KEY));
        this.setViewRange(nbt.getFloat(VIEW_RANGE_NBT_KEY));

        NbtCompound structureNbt = nbt.getCompound(STRUCTURE_NBT_KEY);
        CompoundTag structureTag = FabricNbtConversion.convert(structureNbt, CompoundTag.class);

        var adapter = ExtendedBlockStateAdapter.getInstance();
        BlockStructure structure = IllusoryWallsMod.SCHEMATIC_FORMAT.deserializer().deserialize(structureTag, adapter);

        this.structureContainer.setStructure(structure);
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putBoolean(FADING_NBT_KEY, isFading());
        nbt.putFloat(VIEW_RANGE_NBT_KEY, getViewRange());

        BlockStructure structure = this.structureContainer.getWrapper().getStructure();
        CompoundTag structureTag = IllusoryWallsMod.SCHEMATIC_FORMAT.serializer().serialize(structure);
        NbtCompound structureNbt = FabricNbtConversion.convert(structureTag, NbtCompound.class);
        nbt.put(STRUCTURE_NBT_KEY, structureNbt);
    }

    @Override
    public void tick() {
        super.tick();

        if (getWorld().isClient || !isFading() || age < fadeEnd) return;

        this.discard();
    }

    @Override
    public StructureContainer getStructureContainer() {
        return structureContainer;
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket() {
        var packet = new EntityExtraSpawnPacket(this);
        return packet.toVanillaS2CPacket();
    }

    @Override
    public void writeExtraSpawnData(PacketByteBuf buf) {
        PacketBufUtils.writeBlockStructure(buf, structureContainer.getWrapper().getStructure(), IllusoryWallsMod.SCHEMATIC_FORMAT);
        buf.writeBoolean(isFading());
        buf.writeBlockPos(getFadingFrom());
    }

    @Override
    public void readExtraSpawnData(PacketByteBuf buf) {
        BlockStructure structure = PacketBufUtils.readBlockStructure(buf, IllusoryWallsMod.SCHEMATIC_FORMAT);

        this.structureContainer.setStructure(structure);
        setFading(buf.readBoolean());
        setFadingFrom(buf.readBlockPos());
    }

    private float getViewRange() {
        return this.dataTracker.get(VIEW_RANGE);
    }

    private void setViewRange(float viewRange) {
        this.dataTracker.set(VIEW_RANGE, viewRange);
    }

    @Override
    public boolean shouldRender(double distance) {
        return distance < MathHelper.square((double) this.getViewRange() * 64.0 * DisplayEntity.getRenderDistanceMultiplier());
    }
}
