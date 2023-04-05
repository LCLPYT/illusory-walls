package work.lclpnet.illwalls.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.Identifier;
import work.lclpnet.illwalls.IllusoryWallsMod;
import work.lclpnet.illwalls.entity.ExtraSpawnData;

import java.util.Objects;

public class EntityExtraSpawnPacket implements PacketSerializer {

    public static final Identifier ID = IllusoryWallsMod.identifier("spawn");

    private final EntitySpawnS2CPacket packet;
    private final PacketByteBuf data;

    public EntityExtraSpawnPacket(EntitySpawnS2CPacket packet, PacketByteBuf data) {
        this.packet = Objects.requireNonNull(packet);
        this.data = Objects.requireNonNull(data);
    }

    public EntityExtraSpawnPacket(Entity entity) {
        this(new EntitySpawnS2CPacket(entity), createDataBuffer(entity));
    }

    public EntityExtraSpawnPacket(PacketByteBuf buf) {
        // read packet bytes
        int size = buf.readVarInt();
        var raw = buf.readBytes(size);

        // re-create packet
        final var packetBuf = new PacketByteBuf(raw);
        this.packet = new EntitySpawnS2CPacket(packetBuf);

        // read data
        size = buf.readVarInt();
        raw = buf.readBytes(size);
        data = new PacketByteBuf(raw);
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
    public void writeTo(PacketByteBuf buf) {
        var packetBuf = PacketByteBufs.create();
        packet.write(packetBuf);

        int packetBufSize = packetBuf.readableBytes();
        buf.writeVarInt(packetBufSize);
        buf.writeBytes(packetBuf);

        int dataSize = data.readableBytes();
        buf.writeVarInt(dataSize);
        buf.writeBytes(data);
    }

    @Override
    public Identifier getIdentifier() {
        return ID;
    }

    public EntitySpawnS2CPacket getPacket() {
        return packet;
    }

    public PacketByteBuf getData() {
        return data;
    }
}
