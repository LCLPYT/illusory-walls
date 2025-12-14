package work.lclpnet.illwalls.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import work.lclpnet.illwalls.IllusoryWallsMod;
import work.lclpnet.illwalls.struct.ExtendedBlockStateAdapter;
import work.lclpnet.illwalls.struct.StructureContainer;
import work.lclpnet.kibu.structure.BlockStructure;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class IllusoryWallsPacketCodecs {

    private IllusoryWallsPacketCodecs() {}

    public static final StreamCodec<FriendlyByteBuf, FriendlyByteBuf> BYTE_BUF_CODEC = StreamCodec.ofMember((data, buf) -> {
        int dataSize = data.readableBytes();
        buf.writeVarInt(dataSize);
        buf.writeBytes(data);
    }, buf -> {
        int size = buf.readVarInt();
        ByteBuf raw = buf.readBytes(size);
        return new FriendlyByteBuf(raw);
    });

    public static final StreamCodec<ByteBuf, BlockStructure> STRUCTURE_PACKET_CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE_ARRAY, structure -> {
                try {
                    return IllusoryWallsMod.SCHEMATIC_FORMAT.writer().toArray(structure);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to serialize structure", e);
                }
            }, bytes -> {
                var adapter = ExtendedBlockStateAdapter.getInstance();

                try {
                    var in = new ByteArrayInputStream(bytes);
                    return IllusoryWallsMod.SCHEMATIC_FORMAT.reader().read(in, adapter, StructureContainer::createMutableStructure);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to deserialize structure", e);
                }
            });
}
