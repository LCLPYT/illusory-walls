package work.lclpnet.illwalls.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Display;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.Level;
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

    public IllusoryWallEntity(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);

        if (level().isClientSide()) {
            initClient();
        }
    }

    @Environment(EnvType.CLIENT)
    private void initClient() {
        // the outline color of an illusory wall should always be the same.
        // Therefore, use a persistent seed for a random.
        RandomSource colorRandom = new XoroshiroRandomSource(this.getId());
        int hsvColor = ColorUtil.getRandomHsvColor(colorRandom);
        this.outlineColor = ColorUtil.setArgbPackedAlpha(hsvColor, 255);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {}

    public boolean isFading() {
        return fading;
    }

    public void setFading(boolean fading) {
        this.fading = fading;

        if (fading) {
            fadeEnd = tickCount + FADE_DURATION_TICKS;
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput view) {
        setFading(view.getBooleanOr(FADING_NBT_KEY, false));

        var structureNbt = view.read(STRUCTURE_NBT_KEY, CompoundTag.CODEC).orElseGet(CompoundTag::new);
        var structureTag = FabricNbtConversion.convert(structureNbt, work.lclpnet.kibu.jnbt.CompoundTag.class);

        var adapter = ExtendedBlockStateAdapter.getInstance();
        BlockStructure structure = IllusoryWallsMod.SCHEMATIC_FORMAT.deserializer().deserialize(structureTag, adapter, StructureContainer::createMutableStructure);

        this.structureContainer.setStructure(structure);

        properties.readFrom(view.childOrEmpty(PROPERTIES_NBT_KEY));

        setFadeMode(view.getIntOr(FADE_MODE_NBT_KEY, 0));

        fadeFrom = BlockPos.of(view.getLongOr(FADE_FROM_NBT_KEY, 0));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput nbt) {
        nbt.putBoolean(FADING_NBT_KEY, isFading());

        BlockStructure structure = this.structureContainer.getWrapper().getStructure();
        var structureTag = IllusoryWallsMod.SCHEMATIC_FORMAT.serializer().serialize(structure);
        CompoundTag structureNbt = FabricNbtConversion.convert(structureTag, CompoundTag.class);
        nbt.store(STRUCTURE_NBT_KEY, CompoundTag.CODEC, structureNbt);

        getIllusoryWallProperties().writeTo(nbt.child(PROPERTIES_NBT_KEY));

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
    public boolean shouldBeTrackedBy(ServerPlayer player) {
        return PlayerInfo.get(player).canSeeIllusoryWalls();
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entityTrackerEntry) {
        var packet = new EntityExtraSpawnS2CPacket(this, entityTrackerEntry);
        return ServerNetworkHandler.createS2CPacket(packet);
    }

    @Override
    public void writeExtraSpawnData(FriendlyByteBuf buf) {
        IllusoryWallsPacketCodecs.STRUCTURE_PACKET_CODEC.encode(buf, structureContainer.getWrapper().getStructure());
    }

    @Override
    public void readExtraSpawnData(FriendlyByteBuf buf) {
        BlockStructure structure = IllusoryWallsPacketCodecs.STRUCTURE_PACKET_CODEC.decode(buf);

        this.structureContainer.setStructure(structure);
    }

    public synchronized void fade(@Nullable BlockPos from) {
        Level world = level();
        if (world.isClientSide() || isFading()) return;

        fadeFrom = from;

        // remove blocks
        for (BlockPos pos : structureContainer.getWrapper().getBlockPositions()) {
            world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        }

        BlockPos pos = blockPosition();
        Vec3 soundPos = pos.getCenter();

        world.playSound(null, soundPos.x(), soundPos.y(), soundPos.z(),
                IllusoryWallsMod.ILLUSORY_WALL_FADE_SOUND, SoundSource.BLOCKS, 0.85f, 1f);

        setFading(true);
        setFadeMode(FADE_OUT);

        var serverWorld = (ServerLevel) world;

        // spawn a StructureEntity for display
        spawnStructureEntity(pos, serverWorld, FADE_OUT);
    }

    public synchronized void resetWall() {
        if (isRemoved()) return;

        Level world = level();
        if (world.isClientSide() || (isFading() && getFadeMode() == FADE_IN)) return;

        properties.stopRespawnTimer();

        BlockPos pos = blockPosition();
        Vec3 soundPos = pos.getCenter();

        world.playSound(null, soundPos.x(), soundPos.y(), soundPos.z(),
                IllusoryWallsMod.ILLUSORY_WALL_FADE_SOUND, SoundSource.BLOCKS, 0.85f, 1f);

        setFading(true);
        setFadeMode(FADE_IN);

        ServerLevel serverWorld = (ServerLevel) world;

        // spawn a StructureEntity for display
        spawnStructureEntity(pos, serverWorld, FADE_IN);
    }

    private void replaceBlocks() {
        Level world = level();
        if (world.isClientSide()) return;

        // reset blocks
        ExtendedStructureWrapper wrapper = structureContainer.getWrapper();

        for (BlockPos pos : wrapper.getBlockPositions()) {
            world.setBlockAndUpdate(pos, wrapper.getBlockState(pos));
        }
    }

    private void spawnStructureEntity(BlockPos pos, ServerLevel serverWorld, int fadeIn) {
        IllusoryWallsMod.STRUCTURE_ENTITY.spawn(serverWorld, entity -> {
            structureContainer.getWrapper().copyTo(entity.getStructureContainer().getWrapper());
            entity.setFading(true);
            entity.setFadeMode(fadeIn);
            entity.setFadingFrom(fadeFrom != null ? fadeFrom : pos);
        }, pos, EntitySpawnReason.CONVERSION, false, false);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide() || !this.isFading() || tickCount < fadeEnd) return;

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
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < Mth.square(64.0 * Display.getViewScale());
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
    public final boolean hurtServer(ServerLevel world, DamageSource source, float amount) {
        return false;
    }
}
