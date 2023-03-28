package work.lclpnet.illwalls.structure;

import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.PacketByteBuf;
import work.lclpnet.illwalls.schem.v2.SpongeSchematicV2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class BlockStructureHandler {

    public static final TrackedDataHandler<BlockStructure> BLOCK_STRUCTURE = TrackedDataHandler.of(
            new Writer(), new Reader()
    );

    private static class Writer implements PacketByteBuf.PacketWriter<BlockStructure> {

        @Override
        public void accept(PacketByteBuf buf, BlockStructure storage) {
            var nbt = SpongeSchematicV2.getInstance().writer().create(storage, storage);
            var compressedData = new ByteArrayOutputStream();

            try {
                NbtIo.writeCompressed(nbt, compressedData);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            buf.writeBytes(compressedData.toByteArray());
        }
    }

    private static class Reader implements PacketByteBuf.PacketReader<BlockStructure> {

        @Override
        public BlockStructure apply(PacketByteBuf buf) {
            byte[] bytes = buf.readByteArray();
            var compressedData = new ByteArrayInputStream(bytes);

            NbtCompound nbt;
            try {
                nbt = NbtIo.readCompressed(compressedData);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return SpongeSchematicV2.getInstance().reader().read(nbt);
        }
    }
}
