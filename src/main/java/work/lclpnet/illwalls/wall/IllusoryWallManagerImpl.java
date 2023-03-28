package work.lclpnet.illwalls.wall;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class IllusoryWallManagerImpl implements IllusoryWallManager {

    private final Map<UUID, IllusoryWall> wallsByUuid = new HashMap<>();

    public Optional<IllusoryWall> getWall(UUID uuid) {
        return Optional.ofNullable(wallsByUuid.get(uuid));
    }

    @Nonnull
    public IllusoryWall createNewWall() {
        return new IllusoryWall();
    }

    @Nonnull
    public IllusoryWall getOrCreateWall(UUID uuid) {
        return wallsByUuid.computeIfAbsent(uuid, IllusoryWall::new);
    }
}
