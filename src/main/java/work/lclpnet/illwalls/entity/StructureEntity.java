package work.lclpnet.illwalls.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.entity.Display;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import work.lclpnet.illwalls.IllusoryWallsMod;
import work.lclpnet.illwalls.network.EntityExtraSpawnS2CPacket;
import work.lclpnet.illwalls.network.IllusoryWallsPacketCodecs;
import work.lclpnet.illwalls.network.ServerNetworkHandler;
import work.lclpnet.illwalls.struct.ExtendedBlockStateAdapter;
import work.lclpnet.illwalls.struct.StructureContainer;
import work.lclpnet.illwalls.struct.StructureHolder;
import work.lclpnet.kibu.nbt.FabricNbtConversion;
import work.lclpnet.kibu.structure.BlockStructure;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * An entity consisting of multiple blocks (a structure) that is rendered similar to {@link net.minecraft.world.entity.Display.BlockDisplay}.
 * However, this entity can be rendered with opacity, determined by the fading parameters.
 */
public class StructureEntity extends Entity implements ExtraSpawnData, StructureHolder {

    public static final String
            FADING_NBT_KEY = "fading",
            VIEW_RANGE_NBT_KEY = "view_range",
            STRUCTURE_NBT_KEY = "structure",
            FADE_MODE_NBT_KEY = "fade_mode";
    public static final int FADE_OUT = 0, FADE_IN = 1;
    private static final EntityDataAccessor<Boolean> FADING = SynchedEntityData.defineId(StructureEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> VIEW_RANGE = SynchedEntityData.defineId(StructureEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Optional<BlockPos>> FADING_FROM = SynchedEntityData.defineId(StructureEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);

    private transient int fadeEnd = 0;
    @Environment(EnvType.CLIENT)
    private transient long fadeStartMs;
    private final StructureContainer structureContainer = new StructureContainer(this);
    private int fadeMode = FADE_OUT;

    public StructureEntity(EntityType<?> entityType, Level world) {
        super(entityType, world);

        if (world.isClientSide()) {
            initClient();
        }
    }

    @Environment(EnvType.CLIENT)
    private void initClient() {
        fadeStartMs = 0;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(FADING, false)
                .define(VIEW_RANGE, 1f)
                .define(FADING_FROM, Optional.empty());
    }

    public boolean isFading() {
        return this.entityData.get(FADING);
    }

    public void setFading(boolean fading) {
        boolean wasFading = isFading();
        this.entityData.set(FADING, fading);

        if (!wasFading && fading) {
            startFading();
        }
    }

    public int getFadeMode() {
        return fadeMode;
    }

    public void setFadeMode(int fadeMode) {
        this.fadeMode = fadeMode;
    }

    @Nullable
    public BlockPos getFadingFrom() {
        return this.entityData.get(FADING_FROM).orElse(null);
    }

    public void setFadingFrom(@Nullable BlockPos pos) {
        this.entityData.set(FADING_FROM, Optional.ofNullable(pos));
    }

    private void startFading() {
        fadeEnd = tickCount + IllusoryWallEntity.FADE_DURATION_TICKS;

        if (level().isClientSide()) {
            fadeStartMs = System.currentTimeMillis();
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> data) {
        super.onSyncedDataUpdated(data);

        if (data.equals(FADING) && isFading()) {
            startFading();
        }
    }

    @Environment(EnvType.CLIENT)
    public long getFadeStartMs() {
        return fadeStartMs;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput view) {
        this.setFading(view.getBooleanOr(FADING_NBT_KEY, false));
        this.setViewRange(view.getFloatOr(VIEW_RANGE_NBT_KEY, 0f));

        CompoundTag structureNbt = view.read(STRUCTURE_NBT_KEY, CompoundTag.CODEC).orElseGet(CompoundTag::new);
        var structureTag = FabricNbtConversion.convert(structureNbt, work.lclpnet.kibu.jnbt.CompoundTag.class);

        var adapter = ExtendedBlockStateAdapter.getInstance();
        BlockStructure structure = IllusoryWallsMod.SCHEMATIC_FORMAT.deserializer().deserialize(structureTag, adapter, StructureContainer::createMutableStructure);

        this.structureContainer.setStructure(structure);

        fadeMode = view.getIntOr(FADE_MODE_NBT_KEY, 0);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput view) {
        view.putBoolean(FADING_NBT_KEY, isFading());
        view.putFloat(VIEW_RANGE_NBT_KEY, getViewRange());

        BlockStructure structure = this.structureContainer.getWrapper().getStructure();
        var structureTag = IllusoryWallsMod.SCHEMATIC_FORMAT.serializer().serialize(structure);
        CompoundTag structureNbt = FabricNbtConversion.convert(structureTag, CompoundTag.class);
        view.store(STRUCTURE_NBT_KEY, CompoundTag.CODEC, structureNbt);

        view.putInt(FADE_MODE_NBT_KEY, fadeMode);
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide() || !isFading() || tickCount < fadeEnd + 2) return;

        this.discard();
    }

    @Override
    public StructureContainer getStructureContainer() {
        return structureContainer;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entityTrackerEntry) {
        var packet = new EntityExtraSpawnS2CPacket(this, entityTrackerEntry);
        return ServerNetworkHandler.createS2CPacket(packet);
    }

    @Override
    public void writeExtraSpawnData(FriendlyByteBuf buf) {
        IllusoryWallsPacketCodecs.STRUCTURE_PACKET_CODEC.encode(buf, structureContainer.getWrapper().getStructure());
        buf.writeBoolean(isFading());
        buf.writeBlockPos(getFadingFrom());
        buf.writeVarInt(getFadeMode());
    }

    @Override
    public void readExtraSpawnData(FriendlyByteBuf buf) {
        BlockStructure structure = IllusoryWallsPacketCodecs.STRUCTURE_PACKET_CODEC.decode(buf);

        this.structureContainer.setStructure(structure);
        setFading(buf.readBoolean());
        setFadingFrom(buf.readBlockPos());
        setFadeMode(buf.readVarInt());
    }

    private float getViewRange() {
        return this.entityData.get(VIEW_RANGE);
    }

    private void setViewRange(float viewRange) {
        this.entityData.set(VIEW_RANGE, viewRange);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < Mth.square((double) this.getViewRange() * 64.0 * Display.getViewScale());
    }

    @Override
    public final boolean hurtServer(ServerLevel world, DamageSource source, float amount) {
        return false;
    }
}
