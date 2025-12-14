package work.lclpnet.illwalls.entity;

import net.minecraft.network.FriendlyByteBuf;

public interface ExtraSpawnData {

    void writeExtraSpawnData(FriendlyByteBuf buf);

    void readExtraSpawnData(FriendlyByteBuf buf);
}
