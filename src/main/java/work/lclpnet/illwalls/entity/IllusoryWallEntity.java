package work.lclpnet.illwalls.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.network.EntityTrackerEntry;
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
import work.lclpnet.illwalls.network.EntityExtraSpawnS2CPacket;
import work.lclpnet.illwalls.network.IllusoryWallsPacketCodecs;
import work.lclpnet.illwalls.network.ServerNetworkHandler;
import work.lclpnet.illwalls.struct.ExtendedBlockStateAdapter;
import work.lclpnet.illwalls.struct.ExtendedStructureWrapper;
import work.lclpnet.illwalls.struct.StructureContainer;
import work.lclpnet.illwalls.struct.StructureHolder;
import work.lclpnet.illwalls.util.ColorUtil;
import work.lclpnet.illwalls.util.PlayerInfo;
import work.lclpnet.illwalls.wall.IllusoryWallProperties;
import work.lclpnet.kibu.jnbt.CompoundTag;
import work.lclpnet.kibu.nbt.FabricNbtConversion;
import work.lclpnet.kibu.structure.BlockStructure;

import javax.annotation.Nullable;

/**
 * Represents an illusory wall in the world.
 * This entity is only visible to players who can see illusory walls (edit mode).
 * The "actual" entity shown to everyone, when the fall is fading, is a {@link StructureEntity}.
 * @see PlayerInfo#canSeeIllusoryWalls()
 */
public class IllusoryWallEntity extends Entity implements EntityConditionalTracking, ExtraSpawnData, StructureHolder {

    public static final String
            FADING_NBT_KEY = "fading",
            STRUCTURE_NBT_KEY = "structure",
            PROPERTIES_NBT_KEY = "properties",
            FADE_MODE_NBT_KEY = "fade_mode",
            FADE_FROM_NBT_KEY = "fade_from";
    public static final int FADE_DURATION_TICKS = 20;
    public static final int FADE_DURATION_MS = FADE_DURATION_TICKS * 50;
    public static final int FADE_OUT = 0, FADE_IN = 1;

    private boolean fading = false;
    private int fadeMode = FADE_OUT;
    private transient int fadeEnd = 0;
    private BlockPos fadeFrom = null;
    private final StructureContainer structureContainer = new StructureContainer(this);
    private final IllusoryWallProperties properties = new IllusoryWallProperties();
    @Environment(EnvType.CLIENT)
    private int outlineColor;

    public IllusoryWallEntity(EntityType<?> type, World world) {
        super(type, world);
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
    protected void initDataTracker(DataTracker.Builder builder) {}

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
        setFading(nbt.getBoolean(FADING_NBT_KEY).orElse(false));

        NbtCompound structureNbt = nbt.getCompound(STRUCTURE_NBT_KEY).orElseGet(NbtCompound::new);
        CompoundTag structureTag = FabricNbtConversion.convert(structureNbt, CompoundTag.class);

        var adapter = ExtendedBlockStateAdapter.getInstance();
        BlockStructure structure = IllusoryWallsMod.SCHEMATIC_FORMAT.deserializer().deserialize(structureTag, adapter, StructureContainer::createMutableStructure);

        this.structureContainer.setStructure(structure);

        if (nbt.contains(PROPERTIES_NBT_KEY)) {
            NbtCompound propertiesNbt = nbt.getCompound(PROPERTIES_NBT_KEY).orElseGet(NbtCompound::new);
            properties.readFrom(propertiesNbt);
        }

        if (nbt.contains(FADE_MODE_NBT_KEY)) {
            setFadeMode(nbt.getInt(FADE_MODE_NBT_KEY).orElse(0));
        }

        if (nbt.contains(FADE_FROM_NBT_KEY)) {
            fadeFrom = BlockPos.fromLong(nbt.getLong(FADE_FROM_NBT_KEY).orElse(0L));
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putBoolean(FADING_NBT_KEY, isFading());

        BlockStructure structure = this.structureContainer.getWrapper().getStructure();
        CompoundTag structureTag = IllusoryWallsMod.SCHEMATIC_FORMAT.serializer().serialize(structure);
        NbtCompound structureNbt = FabricNbtConversion.convert(structureTag, NbtCompound.class);
        nbt.put(STRUCTURE_NBT_KEY, structureNbt);

        NbtCompound propertiesNbt = new NbtCompound();
        getIllusoryWallProperties().writeTo(propertiesNbt);
        nbt.put(PROPERTIES_NBT_KEY, propertiesNbt);

        nbt.putInt(FADE_MODE_NBT_KEY, getFadeMode());

        if (fadeFrom != null) {
            nbt.putLong(FADE_FROM_NBT_KEY, fadeFrom.asLong());
        }
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
    public Packet<ClientPlayPacketListener> createSpawnPacket(EntityTrackerEntry entityTrackerEntry) {
        var packet = new EntityExtraSpawnS2CPacket(this, entityTrackerEntry);
        return ServerNetworkHandler.createS2CPacket(packet);
    }

    @Override
    public void writeExtraSpawnData(PacketByteBuf buf) {
        IllusoryWallsPacketCodecs.STRUCTURE_PACKET_CODEC.encode(buf, structureContainer.getWrapper().getStructure());
    }

    @Override
    public void readExtraSpawnData(PacketByteBuf buf) {
        BlockStructure structure = IllusoryWallsPacketCodecs.STRUCTURE_PACKET_CODEC.decode(buf);

        this.structureContainer.setStructure(structure);
    }

    public synchronized void fade(@Nullable BlockPos from) {
        World world = getWorld();
        if (world.isClient || isFading()) return;

        fadeFrom = from;

        // remove blocks
        for (BlockPos pos : structureContainer.getWrapper().getBlockPositions()) {
            world.setBlockState(pos, Blocks.AIR.getDefaultState());
        }

        BlockPos pos = getBlockPos();
        Vec3d soundPos = pos.toCenterPos();

        world.playSound(null, soundPos.getX(), soundPos.getY(), soundPos.getZ(),
                IllusoryWallsMod.ILLUSORY_WALL_FADE_SOUND, SoundCategory.BLOCKS, 0.85f, 1f);

        setFading(true);
        setFadeMode(FADE_OUT);

        var serverWorld = (ServerWorld) world;

        // spawn a StructureEntity for display
        spawnStructureEntity(pos, serverWorld, FADE_OUT);
    }

    public synchronized void resetWall() {
        if (isRemoved()) return;

        World world = getWorld();
        if (world.isClient || (isFading() && getFadeMode() == FADE_IN)) return;

        properties.stopRespawnTimer();

        BlockPos pos = getBlockPos();
        Vec3d soundPos = pos.toCenterPos();

        world.playSound(null, soundPos.getX(), soundPos.getY(), soundPos.getZ(),
                IllusoryWallsMod.ILLUSORY_WALL_FADE_SOUND, SoundCategory.BLOCKS, 0.85f, 1f);

        setFading(true);
        setFadeMode(FADE_IN);

        ServerWorld serverWorld = (ServerWorld) world;

        // spawn a StructureEntity for display
        spawnStructureEntity(pos, serverWorld, FADE_IN);
    }

    private void replaceBlocks() {
        World world = getWorld();
        if (world.isClient) return;

        // reset blocks
        ExtendedStructureWrapper wrapper = structureContainer.getWrapper();

        for (BlockPos pos : wrapper.getBlockPositions()) {
            world.setBlockState(pos, wrapper.getBlockState(pos));
        }
    }

    private void spawnStructureEntity(BlockPos pos, ServerWorld serverWorld, int fadeIn) {
        IllusoryWallsMod.STRUCTURE_ENTITY.spawn(serverWorld, entity -> {
            structureContainer.getWrapper().copyTo(entity.getStructureContainer().getWrapper());
            entity.setFading(true);
            entity.setFadeMode(fadeIn);
            entity.setFadingFrom(fadeFrom != null ? fadeFrom : pos);
        }, pos, SpawnReason.CONVERSION, false, false);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getWorld().isClient || !this.isFading() || age < fadeEnd) return;

        if (getFadeMode() != FADE_OUT) {
            this.setFading(false);

            if (fadeMode == FADE_IN) {
                replaceBlocks();
            }

            return;
        }

        // fade done
        if (!properties.shouldRespawn()) {
            this.discard();
            return;
        }

        if (!properties.isRespawnTimerActive()) {
            properties.startRespawnTimer();
        }

        if (properties.tickTimer()) {
            resetWall();
        }
    }

    @Override
    public boolean shouldRender(double distance) {
        return distance < MathHelper.square(64.0 * DisplayEntity.getRenderDistanceMultiplier());
    }

    @Environment(EnvType.CLIENT)
    public int getOutlineColor() {
        return outlineColor;
    }

    public int getFadeMode() {
        return fadeMode;
    }

    public void setFadeMode(int fadeMode) {
        this.fadeMode = fadeMode;
    }

    public IllusoryWallProperties getIllusoryWallProperties() {
        return properties;
    }

    @Override
    public final boolean damage(ServerWorld world, DamageSource source, float amount) {
        return false;
    }
}
