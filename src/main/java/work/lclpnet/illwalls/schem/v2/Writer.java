package work.lclpnet.illwalls.schem.v2;

import net.minecraft.SharedConstants;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;
import work.lclpnet.illwalls.schem.SchematicAdapter;
import work.lclpnet.illwalls.schem.SchematicRegion;
import work.lclpnet.illwalls.schem.SchematicWriter;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import static work.lclpnet.illwalls.schem.v2.SpongeSchematicV2.*;

class Writer implements SchematicWriter {

    @Override
    public NbtCompound create(SchematicRegion region, SchematicAdapter adapter) {
        var nbt = new NbtCompound();

        var schematic = createSchematic(region, adapter);
        nbt.put(SCHEMATIC, schematic);

        return nbt;
    }

    private NbtCompound createSchematic(SchematicRegion region, SchematicAdapter adapter) {
        final var offset = region.getOffset();

        final int width = region.getWidth();
        final int height = region.getHeight();
        final int length = region.getLength();

        final var dataBuffer = new ByteArrayOutputStream(width * height * length);
        final var palette = new HashMap<String, Integer>();
        final var blockEntities = new ArrayList<NbtCompound>();
        final var pos = new BlockPos.Mutable();

        int nextId = 0;

        for (int y = 0; y < height; y++) {
            final int worldY = offset.getY() + y;

            for (int z = 0; z < length; z++) {
                final int worldZ = offset.getZ() + z;

                for (int x = 0; x < width; x++) {
                    final int worldX = offset.getX() + x;

                    pos.set(worldX, worldY, worldZ);

                    final var blockString = adapter.getBlockState(pos).toString();

                    int id;
                    if (palette.containsKey(blockString)) {
                        id = palette.get(blockString);
                    } else {
                        palette.put(blockString, id = nextId++);
                    }

                    final var blockEntity = adapter.getBlockEntity(pos);
                    if (blockEntity != null) {
                        final var blockEntityNbt = blockEntity.createNbtWithIdentifyingData();

                        final var blockEntityId = blockEntityNbt.getString("id");
                        blockEntityNbt.putString(BLOCK_ENTITY_ID, blockEntityId);

                        blockEntityNbt.putIntArray(BLOCK_ENTITY_POS, new int[]{x, y, z});

                        blockEntityNbt.remove("id");
                        blockEntityNbt.remove("x");
                        blockEntityNbt.remove("y");
                        blockEntityNbt.remove("z");

                        blockEntities.add(blockEntityNbt);
                    }

                    // write as byte
                    while ((id & -128) != 0) {
                        dataBuffer.write(id & 127 | 128);
                        id >>>= 7;
                    }

                    dataBuffer.write(id);
                }
            }
        }

        final var nbt = new NbtCompound();

        nbt.putInt(VERSION, FORMAT_VERSION);
        nbt.putInt(DATA_VERSION, SharedConstants.getGameVersion().getSaveVersion().getId());

        nbt.putShort(WIDTH, (short) width);
        nbt.putShort(HEIGHT, (short) height);
        nbt.putShort(LENGTH, (short) length);

        nbt.putIntArray(OFFSET, new int[]{offset.getX(), offset.getY(), offset.getZ()});

        nbt.putInt(PALETTE_MAX, nextId);

        final var paletteNbt = new NbtCompound();
        palette.forEach(paletteNbt::putInt);

        nbt.put(PALETTE, paletteNbt);
        nbt.putByteArray(BLOCK_DATA, dataBuffer.toByteArray());

        final var blockEntityNbt = new NbtList();
        blockEntityNbt.addAll(blockEntities);
        nbt.put(BLOCK_ENTITIES, blockEntityNbt);

        return nbt;
    }
}
