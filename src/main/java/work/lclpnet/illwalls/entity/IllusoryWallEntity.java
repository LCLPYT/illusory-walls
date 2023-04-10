package work.lclpnet.illwalls.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import work.lclpnet.illwalls.IllusoryWallsMod;
import work.lclpnet.illwalls.impl.FabricBlockStateAdapter;
import work.lclpnet.illwalls.impl.FabricNbtConversion;
import work.lclpnet.illwalls.network.EntityExtraSpawnPacket;
import work.lclpnet.illwalls.network.PacketBufUtils;
import work.lclpnet.illwalls.util.ColorUtil;
import work.lclpnet.kibu.jnbt.CompoundTag;
import work.lclpnet.kibu.structure.BlockStructure;

import java.util.concurrent.atomic.AtomicInteger;

public class IllusoryWallEntity extends Entity implements EntityConditionalTracking, ExtraSpawnData, StructureHolder {

    public static final String
            FADING_NBT_KEY = "fading",
            STRUCTURE_NBT_KEY = "structure";
    @Environment(EnvType.CLIENT)
    private static final AtomicInteger nextId = new AtomicInteger(0);
    public static final int FADE_DURATION_TICKS = 20;
    public static final int FADE_DURATION_MS = FADE_DURATION_TICKS * 50;

    private boolean fading = false;
    private transient int fadeEnd = 0;
    private final StructureContainer structureContainer = new StructureContainer(this);
    @Environment(EnvType.CLIENT)
    private int wallId;
    @Environment(EnvType.CLIENT)
    private int color;

    public IllusoryWallEntity(EntityType<?> type, World world) {
        super(type, world);
        this.ignoreCameraFrustum = true;

        if (world.isClient) {
            initClient();
        }
    }

    @Environment(EnvType.CLIENT)
    private void initClient() {
        this.wallId = nextId.getAndIncrement();
        this.color = ColorUtil.getRandomColor(this.random);
    }

    @Override
    protected void initDataTracker() {}

    public boolean isFading() {
        return fading;
    }

    public void setFading(boolean fading) {
        this.fading = fading;

        if (fading) {
            fadeEnd = age + FADE_DURATION_TICKS;
        }
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        setFading(nbt.getBoolean(FADING_NBT_KEY));

        NbtCompound structureNbt = nbt.getCompound(STRUCTURE_NBT_KEY);
        CompoundTag structureTag = FabricNbtConversion.convert(structureNbt, CompoundTag.class);

        var adapter = FabricBlockStateAdapter.getInstance();
        BlockStructure structure = IllusoryWallsMod.SCHEMATIC_FORMAT.deserializer().deserialize(structureTag, adapter);

        this.structureContainer.setStructure(structure);
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putBoolean(FADING_NBT_KEY, isFading());

        BlockStructure structure = this.structureContainer.getWrapper().getStructure();
        CompoundTag structureTag = IllusoryWallsMod.SCHEMATIC_FORMAT.serializer().serialize(structure);
        NbtCompound structureNbt = FabricNbtConversion.convert(structureTag, NbtCompound.class);
        nbt.put(STRUCTURE_NBT_KEY, structureNbt);
    }

    @Override
    public StructureContainer getStructureContainer() {
        return structureContainer;
    }

    @Override
    public boolean shouldBeTrackedBy(ServerPlayerEntity player) {
        return PlayerInfo.get(player).canSeeIllusoryWalls();
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket() {
        var packet = new EntityExtraSpawnPacket(this);
        return packet.toVanillaS2CPacket();
    }

    @Override
    public void writeExtraSpawnData(PacketByteBuf buf) {
        PacketBufUtils.writeBlockStructure(buf, structureContainer.getWrapper().getStructure(), IllusoryWallsMod.SCHEMATIC_FORMAT);
    }

    @Override
    public void readExtraSpawnData(PacketByteBuf buf) {
        BlockStructure structure = PacketBufUtils.readBlockStructure(buf, IllusoryWallsMod.SCHEMATIC_FORMAT);

        this.structureContainer.setStructure(structure);
    }

    public synchronized void fade() {
        if (this.world.isClient || isFading()) return;

        // remove blocks
        for (BlockPos pos : structureContainer.getWrapper().getBlockPositions()) {
            world.setBlockState(pos, Blocks.AIR.getDefaultState());
        }

        setFading(true);

        var serverWorld = (ServerWorld) this.world;

        // spawn a StructureEntity for display
        IllusoryWallsMod.STRUCTURE_ENTITY.spawn(serverWorld, null, entity -> {
            structureContainer.getWrapper().copyTo(entity.getStructureContainer().getWrapper());
            entity.setFading(true);
        }, getBlockPos(), SpawnReason.CONVERSION, false, false);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.world.isClient || !this.isFading() || age < fadeEnd) return;

        // fade done
        this.discard();
    }

    @Override
    public boolean shouldRender(double distance) {
        return distance < MathHelper.square(64.0 * DisplayEntity.getRenderDistanceMultiplier());
    }

    @Environment(EnvType.CLIENT)
    public int getColor() {
        return color;
    }

    @Environment(EnvType.CLIENT)
    public int getWallId() {
        return wallId;
    }
}
