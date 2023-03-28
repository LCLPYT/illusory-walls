package work.lclpnet.illwalls.wall;

import javax.annotation.Nonnull;
import java.util.UUID;

public interface IllusoryWallManager {

    @Nonnull
    IllusoryWall createNewWall();

    @Nonnull
    IllusoryWall getOrCreateWall(UUID uuid);
}
