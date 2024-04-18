package work.lclpnet.illwalls.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientCommonPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.Identifier;

public interface PacketSerializer {

    void writeTo(PacketByteBuf buf);

    Identifier getIdentifier();

    @SuppressWarnings("unchecked")
    default <T extends ClientCommonPacketListener> Packet<T> toVanillaS2CPacket() {
        var buf = PacketByteBufs.create();
        this.writeTo(buf);
        return (Packet<T>) ServerPlayNetworking.createS2CPacket(getIdentifier(), buf);
    }
}
