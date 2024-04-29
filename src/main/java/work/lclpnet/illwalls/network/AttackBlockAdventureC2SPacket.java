package work.lclpnet.illwalls.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import work.lclpnet.illwalls.IllusoryWallsMod;

public record AttackBlockAdventureC2SPacket(BlockPos pos, Direction direction) implements CustomPayload {

    public static final Id<AttackBlockAdventureC2SPacket> ID = new Id<>(IllusoryWallsMod.identifier("attack_block"));
    public static final PacketCodec<PacketByteBuf, AttackBlockAdventureC2SPacket> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, AttackBlockAdventureC2SPacket::pos,
            Direction.PACKET_CODEC, AttackBlockAdventureC2SPacket::direction,
            AttackBlockAdventureC2SPacket::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
