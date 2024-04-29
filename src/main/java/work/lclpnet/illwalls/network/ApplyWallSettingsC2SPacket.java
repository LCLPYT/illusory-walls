package work.lclpnet.illwalls.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import work.lclpnet.illwalls.IllusoryWallsMod;
import work.lclpnet.illwalls.entity.IllusoryWallEntity;
import work.lclpnet.illwalls.wall.IllusoryWallPlayerSettings;

import javax.annotation.Nullable;

public record ApplyWallSettingsC2SPacket(IllusoryWallPlayerSettings settings, int entityId) implements CustomPayload {

    public static final Id<ApplyWallSettingsC2SPacket> ID = new Id<>(IllusoryWallsMod.identifier("apply_wall_settings"));
    public static final PacketCodec<PacketByteBuf, ApplyWallSettingsC2SPacket> CODEC = PacketCodec.tuple(
            IllusoryWallPlayerSettings.PACKET_CODEC, ApplyWallSettingsC2SPacket::settings,
            PacketCodecs.VAR_INT, ApplyWallSettingsC2SPacket::entityId,
            ApplyWallSettingsC2SPacket::new);

    public ApplyWallSettingsC2SPacket(IllusoryWallPlayerSettings settings, @Nullable IllusoryWallEntity wall) {
        this(settings, wall != null ? wall.getId() : -1);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
