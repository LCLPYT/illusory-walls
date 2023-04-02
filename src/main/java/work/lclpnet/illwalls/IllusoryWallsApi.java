package work.lclpnet.illwalls;

import javax.annotation.Nonnull;

public interface IllusoryWallsApi {

    @Nonnull
    static IllusoryWallsApi getInstance() {
        var api = IllusoryWallsMod.INSTANCE;
        if (api == null) throw new IllegalStateException("Called too early");
        return api;
    }
}
