package work.lclpnet.illwalls.schem;

import net.minecraft.nbt.NbtCompound;
import work.lclpnet.illwalls.structure.BlockStructure;

public interface SchematicReader {

    BlockStructure read(NbtCompound nbt);
}
