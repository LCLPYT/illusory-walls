package work.lclpnet.illwalls.entity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
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

    private boolean fading = false;
    private transient int fadeStart = 0;
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
            fadeStart = age;
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
}
