package work.lclpnet.illwalls.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import work.lclpnet.illwalls.IllusoryWallsMod;
import work.lclpnet.illwalls.entity.ExtraSpawnData;

import java.util.Objects;

public record EntityExtraSpawnS2CPacket(EntitySpawnS2CPacket packet, PacketByteBuf data) implements CustomPayload {

    public static final Id<EntityExtraSpawnS2CPacket> ID = new Id<>(IllusoryWallsMod.identifier("spawn"));

    public static final PacketCodec<RegistryByteBuf, EntityExtraSpawnS2CPacket> CODEC = PacketCodec.tuple(
            EntitySpawnS2CPacket.CODEC, EntityExtraSpawnS2CPacket::packet,
            IllusoryWallsPacketCodecs.BYTE_BUF_CODEC, EntityExtraSpawnS2CPacket::data,
            EntityExtraSpawnS2CPacket::new);

    public EntityExtraSpawnS2CPacket(EntitySpawnS2CPacket packet, PacketByteBuf data) {
        this.packet = Objects.requireNonNull(packet);
        this.data = Objects.requireNonNull(data);
    }

    public EntityExtraSpawnS2CPacket(Entity entity) {
        this(new EntitySpawnS2CPacket(entity), createDataBuffer(entity));
    }

    public static PacketByteBuf createDataBuffer(Object any) {
        if (any instanceof ExtraSpawnData extra) {
            var buf = PacketByteBufs.create();
            extra.writeExtraSpawnData(buf);
            return buf;
        }

        return PacketByteBufs.empty();
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
