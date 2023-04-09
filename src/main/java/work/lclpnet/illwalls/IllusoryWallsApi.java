package work.lclpnet.illwalls;

import work.lclpnet.illwalls.wall.IllusoryWallLookup;
import work.lclpnet.illwalls.wall.IllusoryWallManager;

import javax.annotation.Nonnull;

public interface IllusoryWallsApi {

    @Nonnull
    static IllusoryWallsApi getInstance() {
        return IllusoryWallsMod.getInstance();
    }

    IllusoryWallLookup lookup();

    IllusoryWallManager manager();
}
