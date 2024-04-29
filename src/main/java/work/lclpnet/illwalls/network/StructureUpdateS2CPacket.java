package work.lclpnet.illwalls.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import work.lclpnet.illwalls.IllusoryWallsMod;
import work.lclpnet.kibu.structure.BlockStructure;

import java.util.Objects;

public record StructureUpdateS2CPacket(int entityId, BlockStructure deltaStructure) implements CustomPayload {

    public static final Id<StructureUpdateS2CPacket> ID = new Id<>(IllusoryWallsMod.identifier("structure_update"));
    public static final PacketCodec<ByteBuf, StructureUpdateS2CPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, StructureUpdateS2CPacket::entityId,
            IllusoryWallsPacketCodecs.STRUCTURE_PACKET_CODEC, StructureUpdateS2CPacket::deltaStructure,
            StructureUpdateS2CPacket::new);

    public StructureUpdateS2CPacket(int entityId, BlockStructure deltaStructure) {
        this.entityId = entityId;
        this.deltaStructure = Objects.requireNonNull(deltaStructure);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
