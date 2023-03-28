package work.lclpnet.illwalls.wall;

import work.lclpnet.illwalls.entity.IllusoryWallEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class IllusoryWall {

    private final List<IllusoryWallEntity> blocks = new ArrayList<>();
    private final UUID uuid;

    IllusoryWall() {
        this(UUID.randomUUID());
    }

    IllusoryWall(UUID uuid) {
        this.uuid = uuid;
    }

    public void addBlockIllusion(IllusoryWallEntity entity) {
        this.blocks.add(Objects.requireNonNull(entity));
    }

    public void removeBlockIllusion(IllusoryWallEntity entity) {
        if (entity == null) return;

        this.blocks.remove(entity);
    }

    public List<IllusoryWallEntity> getBlockIllusions() {
        return blocks;
    }

    public UUID getUuid() {
        return uuid;
    }
}
