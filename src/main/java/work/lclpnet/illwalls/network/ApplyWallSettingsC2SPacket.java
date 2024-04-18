package work.lclpnet.illwalls.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import work.lclpnet.illwalls.IllusoryWallsMod;
import work.lclpnet.illwalls.entity.IllusoryWallEntity;
import work.lclpnet.illwalls.wall.IllusoryWallPlayerSettings;

import javax.annotation.Nullable;

public class ApplyWallSettingsC2SPacket implements PacketSerializer {

    public static final Identifier ID = IllusoryWallsMod.identifier("apply_wall_settings");

    private final IllusoryWallPlayerSettings settings;
    private final int entityId;

    public ApplyWallSettingsC2SPacket(PacketByteBuf buf) {
        this.settings = new IllusoryWallPlayerSettings(buf);
        this.entityId = buf.readVarInt();
    }

    public ApplyWallSettingsC2SPacket(IllusoryWallPlayerSettings settings, @Nullable IllusoryWallEntity wall) {
        this(settings, wall != null ? wall.getId() : -1);
    }

    public ApplyWallSettingsC2SPacket(IllusoryWallPlayerSettings settings, int entityId) {
        this.settings = settings;
        this.entityId = entityId;
    }

    @Override
    public void writeTo(PacketByteBuf buf) {
        settings.writeTo(buf);
        buf.writeVarInt(entityId);
    }

    @Override
    public Identifier getIdentifier() {
        return ID;
    }

    public IllusoryWallPlayerSettings getSettings() {
        return settings;
    }

    public int getEntityId() {
        return entityId;
    }
}
