package work.lclpnet.illwalls.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerEntity;
import work.lclpnet.illwalls.IllusoryWallsMod;
import work.lclpnet.illwalls.entity.ExtraSpawnData;

import java.util.Objects;

public record EntityExtraSpawnS2CPacket(ClientboundAddEntityPacket packet, FriendlyByteBuf data) implements CustomPacketPayload {

    public static final Type<EntityExtraSpawnS2CPacket> ID = new Type<>(IllusoryWallsMod.identifier("spawn"));

    public static final StreamCodec<RegistryFriendlyByteBuf, EntityExtraSpawnS2CPacket> CODEC = StreamCodec.composite(
            ClientboundAddEntityPacket.STREAM_CODEC, EntityExtraSpawnS2CPacket::packet,
            IllusoryWallsPacketCodecs.BYTE_BUF_CODEC, EntityExtraSpawnS2CPacket::data,
            EntityExtraSpawnS2CPacket::new);

    public EntityExtraSpawnS2CPacket(ClientboundAddEntityPacket packet, FriendlyByteBuf data) {
        this.packet = Objects.requireNonNull(packet);
        this.data = Objects.requireNonNull(data);
    }

    public EntityExtraSpawnS2CPacket(Entity entity, ServerEntity entityTrackerEntry) {
        this(new ClientboundAddEntityPacket(entity, entityTrackerEntry), createDataBuffer(entity));
    }

    public static FriendlyByteBuf createDataBuffer(Object any) {
        if (any instanceof ExtraSpawnData extra) {
            var buf = PacketByteBufs.create();
            extra.writeExtraSpawnData(buf);
            return buf;
        }

        return PacketByteBufs.empty();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
