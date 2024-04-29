package work.lclpnet.illwalls.wall;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import work.lclpnet.illwalls.entity.IllusoryWallEntity;

/**
 * Individual player settings for illusory wall creation.
 */
public class IllusoryWallPlayerSettings {

    public static final PacketCodec<PacketByteBuf, IllusoryWallPlayerSettings> PACKET_CODEC = PacketCodec.of(IllusoryWallPlayerSettings::writeTo, IllusoryWallPlayerSettings::new);
    private int respawnDuration = IllusoryWallProperties.NO_RESPAWN;

    public IllusoryWallPlayerSettings() {}

    public IllusoryWallPlayerSettings(PacketByteBuf buf) {
        respawnDuration = buf.readVarInt();
    }

    public IllusoryWallPlayerSettings(IllusoryWallEntity entity) {
        IllusoryWallProperties properties = entity.getIllusoryWallProperties();
        respawnDuration = properties.getRespawnDuration();
    }

    public void setRespawnDuration(int respawnDuration) {
        this.respawnDuration = respawnDuration;
    }

    public int getRespawnDuration() {
        return respawnDuration;
    }

    public boolean shouldRespawn() {
        return respawnDuration != IllusoryWallProperties.NO_RESPAWN;
    }

    public void writeTo(PacketByteBuf buf) {
        buf.writeVarInt(respawnDuration);
    }

    public void copyFrom(IllusoryWallPlayerSettings other) {
        this.setRespawnDuration(other.getRespawnDuration());
    }

    public void applyTo(IllusoryWallEntity entity) {
        IllusoryWallProperties props = entity.getIllusoryWallProperties();
        props.setRespawnDuration(respawnDuration);
    }
}
