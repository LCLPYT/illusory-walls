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
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.random.Xoroshiro128PlusPlusRandom;
import net.minecraft.world.World;
import work.lclpnet.illwalls.IllusoryWallsMod;
import work.lclpnet.illwalls.network.EntityExtraSpawnPacket;
import work.lclpnet.illwalls.network.PacketBufUtils;
import work.lclpnet.illwalls.struct.ExtendedBlockStateAdapter;
import work.lclpnet.illwalls.struct.StructureContainer;
import work.lclpnet.illwalls.struct.StructureHolder;
import work.lclpnet.illwalls.util.ColorUtil;
import work.lclpnet.illwalls.util.PlayerInfo;
import work.lclpnet.kibu.jnbt.CompoundTag;
import work.lclpnet.kibu.nbt.FabricNbtConversion;
import work.lclpnet.kibu.structure.BlockStructure;

import javax.annotation.Nullable;

public class IllusoryWallEntity extends Entity implements EntityConditionalTracking, ExtraSpawnData, StructureHolder {

    public static final String
            FADING_NBT_KEY = "fading",
            STRUCTURE_NBT_KEY = "structure";
    public static final int FADE_DURATION_TICKS = 20;
    public static final int FADE_DURATION_MS = FADE_DURATION_TICKS * 50;

    private boolean fading = false;
    private transient int fadeEnd = 0;
    private final StructureContainer structureContainer = new StructureContainer(this);
    @Environment(EnvType.CLIENT)
    private int outlineColor;

    public IllusoryWallEntity(EntityType<?> type, World world) {
        super(type, world);
        this.ignoreCameraFrustum = true;
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);

        if (getWorld().isClient) {
            initClient();
        }
    }

    @Environment(EnvType.CLIENT)
    private void initClient() {
        // the outline color of an illusory wall should always be the same.
        // Therefore, use a persistent seed for a random.
        Random colorRandom = new Xoroshiro128PlusPlusRandom(this.getId());
        int hsvColor = ColorUtil.getRandomHsvColor(colorRandom);
        this.outlineColor = ColorUtil.setArgbPackedAlpha(hsvColor, 255);
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

        var adapter = ExtendedBlockStateAdapter.getInstance();
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

    public synchronized void fade(@Nullable BlockPos from) {
        World world = getWorld();
        if (world.isClient || isFading()) return;

        // remove blocks
        for (BlockPos pos : structureContainer.getWrapper().getBlockPositions()) {
            world.setBlockState(pos, Blocks.AIR.getDefaultState());
        }

        BlockPos pos = getBlockPos();
        Vec3d soundPos = pos.toCenterPos();

        world.playSound(null, soundPos.getX(), soundPos.getY(), soundPos.getZ(),
                IllusoryWallsMod.ILLUSORY_WALL_FADE_SOUND, SoundCategory.BLOCKS, 0.85f, 1f);

        setFading(true);

        var serverWorld = (ServerWorld) world;

        // spawn a StructureEntity for display
        IllusoryWallsMod.STRUCTURE_ENTITY.spawn(serverWorld, null, entity -> {
            structureContainer.getWrapper().copyTo(entity.getStructureContainer().getWrapper());
            entity.setFading(true);
            entity.setFadingFrom(from != null ? from : pos);
        }, pos, SpawnReason.CONVERSION, false, false);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getWorld().isClient || !this.isFading() || age < fadeEnd) return;

        // fade done
        this.discard();
    }

    @Override
    public boolean shouldRender(double distance) {
        return distance < MathHelper.square(64.0 * DisplayEntity.getRenderDistanceMultiplier());
    }

    @Environment(EnvType.CLIENT)
    public int getOutlineColor() {
        return outlineColor;
    }
}
