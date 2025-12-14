package work.lclpnet.illwalls.util;

import net.minecraft.network.chat.Component;

import java.util.Locale;

public enum McTimeUnit {

    TICKS(1),
    SECONDS(20),
    MINUTES(1200);

    private final int ticks;

    McTimeUnit(int ticks) {
        this.ticks = ticks;
    }

    public int toTicks(int i) {
        return i * ticks;
    }

    public int fromTicks(int ticks) {
        return ticks / this.ticks;
    }

    public boolean canRepresentTicks(int ticks) {
        return ticks % this.ticks == 0;
    }

    public Component asText() {
        return Component.translatable("illusory_wall.unit." + name().toLowerCase(Locale.ROOT));
    }

    public static McTimeUnit getBiggestFittingTimeUnit(int ticks) {
        int highestTicks = Integer.MIN_VALUE;
        McTimeUnit biggest = null;

        for (McTimeUnit value : values()) {
            if (value.canRepresentTicks(ticks) && value.ticks > highestTicks) {
                highestTicks = value.ticks;
                biggest = value;
            }
        }

        return biggest != null ? biggest : McTimeUnit.TICKS;
    }
}
