package work.lclpnet.illwalls;

import work.lclpnet.illwalls.wall.IllusoryWallLookup;

import javax.annotation.Nonnull;

public interface IllusoryWallsApi {

    @Nonnull
    static IllusoryWallsApi getInstance() {
        return IllusoryWallsMod.getInstance();
    }

    IllusoryWallLookup lookup();
}
