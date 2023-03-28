package work.lclpnet.illwalls.schem;

import net.minecraft.util.math.BlockPos;

public interface SchematicRegion {

    BlockPos getOffset();

    int getWidth();

    int getHeight();

    int getLength();
}
