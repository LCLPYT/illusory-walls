package work.lclpnet.illwalls.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import work.lclpnet.illwalls.IllusoryWallsMod;
import work.lclpnet.illwalls.impl.FabricBlockStateAdapter;
import work.lclpnet.kibu.structure.BlockStructure;

import java.io.IOException;
import java.util.Objects;

public class StructureUpdatePacket implements PacketSerializer {

    public static final Identifier ID = IllusoryWallsMod.identifier("structure_update");

    private final int entityId;
    private final BlockStructure deltaStructure;

    public StructureUpdatePacket(int entityId, BlockStructure deltaStructure) {
        this.entityId = entityId;
        this.deltaStructure = Objects.requireNonNull(deltaStructure);
    }

    public StructureUpdatePacket(PacketByteBuf buf) {
        this.entityId = buf.readVarInt();

        final int length = buf.readVarInt();
        final byte[] bytes = buf.readByteArray(length);

        final var adapter = FabricBlockStateAdapter.getInstance();

        try {
            this.deltaStructure = IllusoryWallsMod.SCHEMATIC_FORMAT.reader().fromArray(bytes, adapter);
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize structure", e);
        }
    }

    @Override
    public void writeTo(PacketByteBuf buf) {
        buf.writeVarInt(entityId);

        final byte[] bytes;
        try {
            bytes = IllusoryWallsMod.SCHEMATIC_FORMAT.writer().toArray(deltaStructure);
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize structure", e);
        }

        buf.writeVarInt(bytes.length);
        buf.writeByteArray(bytes);
    }

    @Override
    public Identifier getIdentifier() {
        return ID;
    }

    public int getEntityId() {
        return entityId;
    }

    public BlockStructure getDeltaStructure() {
        return deltaStructure;
    }
}
