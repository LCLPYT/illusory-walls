package work.lclpnet.illwalls.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.Identifier;

public interface PacketSerializer {

    void writeTo(PacketByteBuf buf);

    Identifier getIdentifier();

    default Packet<ClientPlayPacketListener> toVanillaS2CPacket() {
        var buf = PacketByteBufs.create();
        this.writeTo(buf);
        return ServerPlayNetworking.createS2CPacket(getIdentifier(), buf);
    }
}
