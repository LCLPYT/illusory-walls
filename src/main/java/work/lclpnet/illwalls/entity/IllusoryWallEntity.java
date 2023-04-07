package work.lclpnet.illwalls.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import work.lclpnet.illwalls.IllusoryWallsMod;
import work.lclpnet.illwalls.impl.FabricBlockStateAdapter;
import work.lclpnet.illwalls.impl.FabricNbtConversion;
import work.lclpnet.illwalls.impl.FabricStructureWrapper;
import work.lclpnet.illwalls.impl.ListenerStructureWrapper;
import work.lclpnet.kibu.jnbt.CompoundTag;
import work.lclpnet.kibu.structure.BlockStructure;

public class IllusoryWallEntity extends Entity implements ServerOnlyEntity {

    public static final String
            FADING_NBT_KEY = "fading",
            STRUCTURE_NBT_KEY = "structure";
    public static final int FADE_DURATION_TICKS = 20;
    public static final int FADE_DURATION_MS = FADE_DURATION_TICKS * 50;

    private boolean fading = false;
    private transient int fadeEnd = 0;
    private FabricStructureWrapper structure = makeStructure(FabricStructureWrapper.createSimpleStructure());

    public IllusoryWallEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Override
    protected void initDataTracker() {
        // server only have no data trackers
    }

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

        this.structure = makeStructure(structure);
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putBoolean(FADING_NBT_KEY, isFading());

        BlockStructure structure = this.structure.getStructure();
        CompoundTag structureTag = IllusoryWallsMod.SCHEMATIC_FORMAT.serializer().serialize(structure);
        NbtCompound structureNbt = FabricNbtConversion.convert(structureTag, NbtCompound.class);
        nbt.put(STRUCTURE_NBT_KEY, structureNbt);
    }

    public FabricStructureWrapper getStructure() {
        return structure;
    }

    private void onUpdate(BlockPos pos, BlockState state) {
        if (world.isClient) return;

        StructureEntity.center(this, this.structure);
    }

    private FabricStructureWrapper makeStructure(BlockStructure structure) {
        return new ListenerStructureWrapper(structure, this::onUpdate);
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket() {
        return null;  // technically not needed, but just for safety
    }

    public synchronized void fade() {
        if (this.world.isClient || isFading()) return;

        var serverWorld = (ServerWorld) this.world;

        // spawn a StructureEntity for display
        var structureEntity = IllusoryWallsMod.STRUCTURE_ENTITY.spawn(serverWorld, null, entity -> {
            structure.copyTo(entity.getStructure());
            entity.setFading(true);
        }, getBlockPos(), SpawnReason.CONVERSION, false, false);

        if (structureEntity == null) return;

        setFading(true);

        for (BlockPos pos : structure.getBlockPositions()) {
            world.setBlockState(pos, Blocks.AIR.getDefaultState());
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.world.isClient || !this.isFading() || age < fadeEnd) return;

        // fade done
        this.discard();
        System.out.println("DISCARD");
    }
}
