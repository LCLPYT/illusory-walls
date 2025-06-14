package work.lclpnet.illwalls.wall;

import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;

public class IllusoryWallProperties {

    public static final String
            RESPAWN_DURATION_NBT_KEY = "respawn_duration",
            RESPAWN_TIMER_NBT_KEY = "respawn_timer";
    public static final int NO_RESPAWN = -1;

    private int respawnDuration = NO_RESPAWN;
    private int respawnTimer = -1;

    /**
     * Sets the respawn duration of the wall. If -1, respawning will be disabled.
     * @param respawnDuration Respawn duration in ticks.
     */
    public void setRespawnDuration(int respawnDuration) {
        this.respawnDuration = respawnDuration;
    }

    public int getRespawnDuration() {
        return respawnDuration;
    }

    public boolean shouldRespawn() {
        return respawnDuration != -1;
    }

    public void startRespawnTimer() {
        synchronized (this) {
            respawnTimer = respawnDuration;
        }
    }

    public boolean isRespawnTimerActive() {
        synchronized (this) {
            return respawnTimer != -1;
        }
    }

    public void stopRespawnTimer() {
        synchronized (this) {
            respawnTimer = -1;
        }
    }

    public boolean tickTimer() {
        synchronized (this) {
            if (respawnTimer == -1) return false;

            return respawnTimer-- == 0;
        }
    }

    public void readFrom(ReadView view) {
        respawnDuration = view.getInt(RESPAWN_DURATION_NBT_KEY, 0);
        respawnTimer = view.getInt(RESPAWN_TIMER_NBT_KEY, 0);
    }

    public void writeTo(WriteView view) {
        view.putInt(RESPAWN_DURATION_NBT_KEY, respawnDuration);
        view.putInt(RESPAWN_TIMER_NBT_KEY, respawnTimer);
    }
}
