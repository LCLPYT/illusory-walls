package work.lclpnet.illwalls.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import work.lclpnet.illwalls.IllusoryWallsMod;
import work.lclpnet.illwalls.entity.IllusoryWallEntity;
import work.lclpnet.illwalls.util.PlayerInfo;
import work.lclpnet.illwalls.wall.IllusoryWallPlayerSettings;

public record EditWallScreenS2CPacket(IllusoryWallPlayerSettings settings, int entityId) implements CustomPacketPayload {

    public static final Type<EditWallScreenS2CPacket> ID = new Type<>(IllusoryWallsMod.identifier("edit_wall_screen"));
    public static final StreamCodec<FriendlyByteBuf, EditWallScreenS2CPacket> CODEC = StreamCodec.composite(
            IllusoryWallPlayerSettings.PACKET_CODEC, EditWallScreenS2CPacket::settings,
            ByteBufCodecs.VAR_INT, EditWallScreenS2CPacket::entityId,
            EditWallScreenS2CPacket::new);

    public EditWallScreenS2CPacket(ServerPlayer player) {
        this(PlayerInfo.get(player).getWallSettings(), -1);
    }

    public EditWallScreenS2CPacket(IllusoryWallEntity entity) {
        this(new IllusoryWallPlayerSettings(entity), entity.getId());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
