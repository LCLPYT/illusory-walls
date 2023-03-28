package work.lclpnet.illwalls.schem.v2;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import work.lclpnet.illwalls.schem.SchematicReader;
import work.lclpnet.illwalls.structure.BlockStateGetter;
import work.lclpnet.illwalls.structure.BlockStructure;
import work.lclpnet.illwalls.structure.MapStructure;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static work.lclpnet.illwalls.schem.v2.SpongeSchematicV2.*;

class Reader implements SchematicReader {

    private final BlockStateGetter blockStateGetter;

    Reader(BlockStateGetter blockStateGetter) {
        this.blockStateGetter = Objects.requireNonNull(blockStateGetter);
    }

    @Override
    public BlockStructure read(NbtCompound nbt) {
        var schematic = nbt.getCompound(SCHEMATIC);
        return readSchematic(schematic);
    }

    private BlockStructure readSchematic(NbtCompound nbt) {
        final int version = nbt.getInt(VERSION);
        if (version != FORMAT_VERSION) throw new IllegalArgumentException("Invalid nbt");

        final int width = nbt.getInt(WIDTH);
        final int length = nbt.getInt(LENGTH);
        // height is implicitly defined by the block data buffer length, if needed it can also be read in advance here

        final int[] offset = nbt.getIntArray(OFFSET);
        if (offset.length != 3) throw new IllegalArgumentException("Invalid nbt");

        // parse palette
        final NbtCompound paletteTag = nbt.getCompound(PALETTE);
        final Map<Integer, BlockState> palette = new HashMap<>();

        for (var blockString : paletteTag.getKeys()) {
            var blockState = blockStateGetter.getBlockState(blockString);
            if (blockState == null || blockState.isAir()) continue;

            int id = paletteTag.getInt(blockString);
            palette.put(id, blockState);
        }

        // parse blocks
        var container = new MapStructure();

        byte[] blocks = nbt.getByteArray(BLOCK_DATA);   // the block byte array
        int i = 0;                                      // the byte index
        int id, varIntByte;                             // varint id
        int posIdx = 0;                                 // index of the currently read position ((y => z) => x)
        final var worldPos = new BlockPos.Mutable();    // position in world space

        while (i < blocks.length) {
            // read var int id
            id = 0;
            varIntByte = 0;

            while (true) {
                id |= (blocks[i] & 127) << (varIntByte++ * 7);

                if (varIntByte > 5) throw new IllegalStateException("var int out of bounds");

                if ((blocks[i] & 128) != 128) {
                    i++;
                    break;
                }

                i++;
            }

            // posIdx = (y * length * width) + (z * width) + x
            int y = posIdx / (width * length);
            int z = (posIdx % (width * length)) / width;
            int x = (posIdx % (width * length)) % width;

            var state = palette.get(id);
            if (state == null) continue;  // invalid buffer, ignore the error

            worldPos.set(x + offset[0], y + offset[1], z + offset[2]);

            // TODO read block entity, if there is one
            container.setBlockState(worldPos, state);

            posIdx++;
        }

        return container;
    }
}
