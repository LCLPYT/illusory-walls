package work.lclpnet.illwalls.schem;

import net.minecraft.nbt.NbtCompound;

public interface SchematicWriter {

    NbtCompound create(SchematicRegion region, SchematicAdapter adapter);
}
