package work.lclpnet.illwalls.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.block.BlockState;
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
import work.lclpnet.illwalls.impl.FabricBlockStateAdapter;
import work.lclpnet.illwalls.impl.FabricNbtConversion;
import work.lclpnet.illwalls.impl.FabricStructureWrapper;
import work.lclpnet.illwalls.impl.ListenerStructureWrapper;
import work.lclpnet.illwalls.network.EntityExtraSpawnPacket;
import work.lclpnet.illwalls.network.ServerNetworkHandler;
import work.lclpnet.illwalls.network.StructureUpdatePacket;
import work.lclpnet.kibu.jnbt.CompoundTag;
import work.lclpnet.kibu.structure.BlockStructure;

import java.io.IOException;

import static work.lclpnet.illwalls.impl.FabricStructureWrapper.createSimpleStructure;

public class StructureEntity extends Entity implements ExtraSpawnData {

    public static final String
            FADING_NBT_KEY = "fading",
            VIEW_RANGE_NBT_KEY = "view_range",
            STRUCTURE_NBT_KEY = "structure";
    private static final TrackedData<Boolean> FADING = DataTracker.registerData(StructureEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Float> VIEW_RANGE = DataTracker.registerData(StructureEntity.class, TrackedDataHandlerRegistry.FLOAT);

    private transient int fadeEnd = 0;
    @Environment(EnvType.CLIENT)
    private transient long fadeStartMs = 0L;
    @Environment(EnvType.CLIENT)
    public BlockRenderOverrideView renderOverrideView = null;
    private FabricStructureWrapper structure = makeStructure(createSimpleStructure());

    public StructureEntity(EntityType<?> entityType, World world) {
        super(entityType, world);
        this.ignoreCameraFrustum = true;
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
        boolean wasFading = isFading();
        this.dataTracker.set(FADING, fading);

        if (!wasFading && fading) {
            startFading();
        }
    }

    private synchronized void startFading() {
        fadeEnd = age + IllusoryWallEntity.FADE_DURATION_TICKS;

        if (world.isClient()) {
            startFadingClient();
        }
    }

    @Environment(EnvType.CLIENT)
    private void startFadingClient() {
        System.out.println("FADING CLIENT");
        fadeStartMs = System.currentTimeMillis();

        for (BlockPos blockPos : structure.getBlockPositions()) {
            addBlockRenderOverwrite(blockPos);
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

        var adapter = FabricBlockStateAdapter.getInstance();
        BlockStructure structure = IllusoryWallsMod.SCHEMATIC_FORMAT.deserializer().deserialize(structureTag, adapter);

        this.structure = makeStructure(structure);
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putBoolean(FADING_NBT_KEY, isFading());
        nbt.putFloat(VIEW_RANGE_NBT_KEY, getViewRange());

        BlockStructure structure = this.structure.getStructure();
        CompoundTag structureTag = IllusoryWallsMod.SCHEMATIC_FORMAT.serializer().serialize(structure);
        NbtCompound structureNbt = FabricNbtConversion.convert(structureTag, NbtCompound.class);
        nbt.put(STRUCTURE_NBT_KEY, structureNbt);
    }

    @Override
    public void tick() {
        super.tick();

        if (world.isClient || !isFading() || age < fadeEnd) return;

        this.discard();
        System.out.println("DISCARD Structure");
    }

    public FabricStructureWrapper getStructure() {
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
            bytes = IllusoryWallsMod.SCHEMATIC_FORMAT.writer().toArray(structure);
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize structure", e);
        }

        buf.writeVarInt(bytes.length);
        buf.writeByteArray(bytes);
        buf.writeBoolean(isFading());
    }

    @Override
    public void readExtraSpawnData(PacketByteBuf buf) {
        final int length = buf.readVarInt();
        final byte[] bytes = buf.readByteArray(length);

        final var adapter = FabricBlockStateAdapter.getInstance();

        final BlockStructure structure;
        try {
            structure = IllusoryWallsMod.SCHEMATIC_FORMAT.reader().fromArray(bytes, adapter);
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize structure", e);
        }

        this.structure = makeStructure(structure);
        setFading(buf.readBoolean());
    }

    private boolean existsInWorld() {
        return this.world.getEntityById(this.getId()) != null;
    }

    public void updateStructure(BlockStructure delta) {
        if (!world.isClient) {
            if (!this.existsInWorld()) return;  // too early

            // if we are in the server world, send an update packet
            var updatePacket = new StructureUpdatePacket(getId(), delta);
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

    private void onUpdate(BlockPos pos, BlockState state) {
        if (world.isClient) {
            onClientUpdate(pos, state);
            return;
        }

        // move the wall entity to the block in the center of the structure
        center(this, this.structure);

        // on the server world, update send a delta update structure to the players
        var deltaStructure = createSimpleStructure();
        var adapter = FabricBlockStateAdapter.getInstance();
        deltaStructure.setBlockState(adapter.adapt(pos), adapter.adapt(state));

        updateStructure(deltaStructure);
    }

    @Environment(EnvType.CLIENT)
    private void onClientUpdate(BlockPos pos, BlockState state) {
        if (!isFading()) return;

        addBlockRenderOverwrite(pos);
    }

    @Environment(EnvType.CLIENT)
    private void addBlockRenderOverwrite(BlockPos pos) {
        renderOverrideView.illwalls$addOverride(pos);
    }

    @Environment(EnvType.CLIENT)
    private void removeBlockRenderOverwrite(BlockPos pos) {
        renderOverrideView.illwalls$removeOverride(pos);
    }

    @Override
    public void onRemoved() {
        // removed on the CLIENT
        super.onRemoved();

        if (!isFading()) return;

        for (BlockPos pos : structure.getBlockPositions()) {
            removeBlockRenderOverwrite(pos);
        }
    }

    private FabricStructureWrapper makeStructure(BlockStructure structure) {
        return new ListenerStructureWrapper(structure, this::onUpdate);
    }

    /**
     * Moves this entity to the nearest block of the center of the structure.
     * This is done, so that the wall loads evenly from all directions.
     */
    public static void center(Entity entity, FabricStructureWrapper structure) {
        BlockPos center = structure.getCenter();
        int centerX = center.getX();
        int centerY = center.getY();
        int centerZ = center.getZ();

        // check whether the wall is in the center already
        var currentPos = entity.getBlockPos();
        if (currentPos.getX() == centerX && currentPos.getY() == centerY && currentPos.getZ() == centerZ) return;

        entity.setPosition(centerX, centerY, centerZ);
    }
}
