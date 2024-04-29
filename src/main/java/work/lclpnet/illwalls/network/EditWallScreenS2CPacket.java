package work.lclpnet.illwalls.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import work.lclpnet.illwalls.IllusoryWallsMod;
import work.lclpnet.illwalls.entity.IllusoryWallEntity;
import work.lclpnet.illwalls.util.PlayerInfo;
import work.lclpnet.illwalls.wall.IllusoryWallPlayerSettings;

public record EditWallScreenS2CPacket(IllusoryWallPlayerSettings settings, int entityId) implements CustomPayload {

    public static final Id<EditWallScreenS2CPacket> ID = new Id<>(IllusoryWallsMod.identifier("edit_wall_screen"));
    public static final PacketCodec<PacketByteBuf, EditWallScreenS2CPacket> CODEC = PacketCodec.tuple(
            IllusoryWallPlayerSettings.PACKET_CODEC, EditWallScreenS2CPacket::settings,
            PacketCodecs.VAR_INT, EditWallScreenS2CPacket::entityId,
            EditWallScreenS2CPacket::new);

    public EditWallScreenS2CPacket(ServerPlayerEntity player) {
        this(PlayerInfo.get(player).getWallSettings(), -1);
    }

    public EditWallScreenS2CPacket(IllusoryWallEntity entity) {
        this(new IllusoryWallPlayerSettings(entity), entity.getId());
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
