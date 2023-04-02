package work.lclpnet.illwalls.entity;

import net.minecraft.network.PacketByteBuf;

public interface ExtraSpawnData {

    void writeExtraSpawnData(PacketByteBuf buf);

    void readExtraSpawnData(PacketByteBuf buf);
}
