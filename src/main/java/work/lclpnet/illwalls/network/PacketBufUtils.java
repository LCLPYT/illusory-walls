package work.lclpnet.illwalls.network;

import net.minecraft.network.PacketByteBuf;
import work.lclpnet.illwalls.struct.ExtendedBlockStateAdapter;
import work.lclpnet.kibu.schematic.api.SchematicFormat;
import work.lclpnet.kibu.structure.BlockStructure;

import java.io.IOException;

public class PacketBufUtils {

    public static void writeBlockStructure(PacketByteBuf buf, BlockStructure structure, SchematicFormat format) {
        final byte[] bytes;
        try {
            bytes = format.writer().toArray(structure);
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize structure", e);
        }

        buf.writeVarInt(bytes.length);
        buf.writeByteArray(bytes);
    }

    public static BlockStructure readBlockStructure(PacketByteBuf buf, SchematicFormat format) {
        final int length = buf.readVarInt();
        final byte[] bytes = buf.readByteArray(length);

        final var adapter = ExtendedBlockStateAdapter.getInstance();

        try {
            return format.reader().fromArray(bytes, adapter);
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize structure", e);
        }
    }
}
