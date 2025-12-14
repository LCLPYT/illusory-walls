package work.lclpnet.illwalls.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import work.lclpnet.illwalls.IllusoryWallsMod;

public record AttackBlockAdventureC2SPacket(BlockPos pos, Direction direction) implements CustomPacketPayload {

    public static final Type<AttackBlockAdventureC2SPacket> ID = new Type<>(IllusoryWallsMod.identifier("attack_block"));
    public static final StreamCodec<FriendlyByteBuf, AttackBlockAdventureC2SPacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, AttackBlockAdventureC2SPacket::pos,
            Direction.STREAM_CODEC, AttackBlockAdventureC2SPacket::direction,
            AttackBlockAdventureC2SPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
