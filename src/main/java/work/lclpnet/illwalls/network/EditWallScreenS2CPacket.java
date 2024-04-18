package work.lclpnet.illwalls.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import work.lclpnet.illwalls.IllusoryWallsMod;
import work.lclpnet.illwalls.entity.IllusoryWallEntity;
import work.lclpnet.illwalls.util.PlayerInfo;
import work.lclpnet.illwalls.wall.IllusoryWallPlayerSettings;

public class EditWallScreenS2CPacket implements PacketSerializer {

    public static final Identifier ID = IllusoryWallsMod.identifier("edit_wall_screen");

    private final IllusoryWallPlayerSettings settings;
    private final int entityId;

    public EditWallScreenS2CPacket(PacketByteBuf buf) {
        this.settings = new IllusoryWallPlayerSettings(buf);
        this.entityId = buf.readVarInt();
    }

    public EditWallScreenS2CPacket(ServerPlayerEntity player) {
        this(PlayerInfo.get(player).getWallSettings(), -1);
    }

    public EditWallScreenS2CPacket(IllusoryWallEntity entity) {
        this(new IllusoryWallPlayerSettings(entity), entity.getId());
    }

    public EditWallScreenS2CPacket(IllusoryWallPlayerSettings settings, int entityId) {
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
