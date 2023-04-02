package work.lclpnet.illwalls.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import work.lclpnet.illwalls.impl.FabricBlockStateAdapter;
import work.lclpnet.illwalls.impl.FabricNbtConversion;
import work.lclpnet.illwalls.impl.FabricStructureWrapper;
import work.lclpnet.illwalls.impl.IllusoryWallStructure;
import work.lclpnet.illwalls.network.EntityExtraSpawnPacket;
import work.lclpnet.illwalls.network.IllusoryWallUpdatePacket;
import work.lclpnet.illwalls.network.ServerNetworkHandler;
import work.lclpnet.kibu.jnbt.CompoundTag;
import work.lclpnet.kibu.schematic.SchematicFormats;
import work.lclpnet.kibu.schematic.api.SchematicFormat;
import work.lclpnet.kibu.structure.BlockStructure;

import java.io.IOException;

public class IllusoryWallEntity extends Entity implements ExtraSpawnData {

    public static final String
            FADING_NBT_KEY = "fading",
            VIEW_RANGE_NBT_KEY = "view_range",
            STRUCTURE_NBT_KEY = "structure";
    private static final TrackedData<Boolean> FADING = DataTracker.registerData(IllusoryWallEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    public static final SchematicFormat SCHEMATIC_FORMAT = SchematicFormats.SPONGE_V2;
    public static final int FADE_DURATION_TICKS = 20;
    public static final int FADE_DURATION_MS = FADE_DURATION_TICKS * 50;
    private static final TrackedData<Float> VIEW_RANGE = DataTracker.registerData(IllusoryWallEntity.class, TrackedDataHandlerRegistry.FLOAT);

    @Environment(EnvType.CLIENT)
    private long fadeStartMs = 0L;
    private IllusoryWallStructure structure = new IllusoryWallStructure(this);

    public IllusoryWallEntity(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(FADING, false);
        this.dataTracker.startTracking(VIEW_RANGE, 1f);
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
        this.setFading(nbt.getBoolean(FADING_NBT_KEY));
        this.setViewRange(nbt.getFloat(VIEW_RANGE_NBT_KEY));

        NbtCompound structureNbt = nbt.getCompound(STRUCTURE_NBT_KEY);
        CompoundTag structureTag = FabricNbtConversion.convert(structureNbt, CompoundTag.class);

        var adapter = FabricBlockStateAdapter.getInstance();
        BlockStructure structure = SCHEMATIC_FORMAT.deserializer().deserialize(structureTag, adapter);

        this.structure = new IllusoryWallStructure(this, structure);
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putBoolean(FADING_NBT_KEY, isFading());
        nbt.putFloat(VIEW_RANGE_NBT_KEY, getViewRange());

        BlockStructure structure = this.structure.getStructure();
        CompoundTag structureTag = SCHEMATIC_FORMAT.serializer().serialize(structure);
        NbtCompound structureNbt = FabricNbtConversion.convert(structureTag, NbtCompound.class);
        nbt.put(STRUCTURE_NBT_KEY, structureNbt);
    }

    public IllusoryWallStructure getStructure() {
        return structure;
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket() {
        var packet = new EntityExtraSpawnPacket(this);
        return packet.toVanillaS2CPacket();
    }

    @Override
    public void writeExtraSpawnData(PacketByteBuf buf) {
        final var structure = this.structure.getStructure();

        final byte[] bytes;
        try {
            bytes = SCHEMATIC_FORMAT.writer().toArray(structure);
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize structure", e);
        }

        buf.writeVarInt(bytes.length);
        buf.writeByteArray(bytes);
    }

    @Override
    public void readExtraSpawnData(PacketByteBuf buf) {
        final int length = buf.readVarInt();
        final byte[] bytes = buf.readByteArray(length);

        final var adapter = FabricBlockStateAdapter.getInstance();

        final BlockStructure structure;
        try {
            structure = SCHEMATIC_FORMAT.reader().fromArray(bytes, adapter);
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize structure", e);
        }

        this.structure = new IllusoryWallStructure(this, structure);
    }

    private boolean existsInWorld() {
        return this.world.getEntityById(this.getId()) != null;
    }

    public void updateStructure(BlockStructure delta) {
        if (!world.isClient) {
            if (!this.existsInWorld()) return;  // too early

            // if we are in the server world, send an update packet
            var updatePacket = new IllusoryWallUpdatePacket(getId(), delta);
            ServerNetworkHandler.send(updatePacket, PlayerLookup.tracking(this));
            return;
        }

        // sync the received delta structure
        var deltaWrapper = new FabricStructureWrapper(delta);
        var positions = deltaWrapper.getBlockPositions();

        positions.forEach(pos -> structure.setBlockState(pos, deltaWrapper.getBlockState(pos)));
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
