package work.lclpnet.illwalls.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import work.lclpnet.illwalls.IllusoryWallsMod;
import work.lclpnet.illwalls.entity.IllusoryWallEntity;
import work.lclpnet.illwalls.wall.IllusoryWallPlayerSettings;

import javax.annotation.Nullable;

public record ApplyWallSettingsC2SPacket(IllusoryWallPlayerSettings settings, int entityId) implements CustomPacketPayload {

    public static final Type<ApplyWallSettingsC2SPacket> ID = new Type<>(IllusoryWallsMod.identifier("apply_wall_settings"));
    public static final StreamCodec<FriendlyByteBuf, ApplyWallSettingsC2SPacket> CODEC = StreamCodec.composite(
            IllusoryWallPlayerSettings.PACKET_CODEC, ApplyWallSettingsC2SPacket::settings,
            ByteBufCodecs.VAR_INT, ApplyWallSettingsC2SPacket::entityId,
            ApplyWallSettingsC2SPacket::new);

    public ApplyWallSettingsC2SPacket(IllusoryWallPlayerSettings settings, @Nullable IllusoryWallEntity wall) {
        this(settings, wall != null ? wall.getId() : -1);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
