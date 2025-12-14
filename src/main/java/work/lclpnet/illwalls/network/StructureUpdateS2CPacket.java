package work.lclpnet.illwalls.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import work.lclpnet.illwalls.IllusoryWallsMod;
import work.lclpnet.kibu.structure.BlockStructure;

import java.util.Objects;

public record StructureUpdateS2CPacket(int entityId, BlockStructure deltaStructure) implements CustomPacketPayload {

    public static final Type<StructureUpdateS2CPacket> ID = new Type<>(IllusoryWallsMod.identifier("structure_update"));
    public static final StreamCodec<ByteBuf, StructureUpdateS2CPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, StructureUpdateS2CPacket::entityId,
            IllusoryWallsPacketCodecs.STRUCTURE_PACKET_CODEC, StructureUpdateS2CPacket::deltaStructure,
            StructureUpdateS2CPacket::new);

    public StructureUpdateS2CPacket(int entityId, BlockStructure deltaStructure) {
        this.entityId = entityId;
        this.deltaStructure = Objects.requireNonNull(deltaStructure);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
