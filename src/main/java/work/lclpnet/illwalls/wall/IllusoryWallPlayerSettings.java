package work.lclpnet.illwalls.wall;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import work.lclpnet.illwalls.entity.IllusoryWallEntity;

/**
 * Individual player settings for illusory wall creation.
 */
public class IllusoryWallPlayerSettings {

    public static final StreamCodec<FriendlyByteBuf, IllusoryWallPlayerSettings> PACKET_CODEC = StreamCodec.ofMember(IllusoryWallPlayerSettings::writeTo, IllusoryWallPlayerSettings::new);
    private int respawnDuration = IllusoryWallProperties.NO_RESPAWN;

    public IllusoryWallPlayerSettings() {}

    public IllusoryWallPlayerSettings(FriendlyByteBuf buf) {
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

    public void writeTo(FriendlyByteBuf buf) {
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
