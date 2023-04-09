package work.lclpnet.illwalls.entity;

import net.minecraft.client.world.ClientWorld;
import org.slf4j.Logger;
import work.lclpnet.illwalls.network.EntityExtraSpawnPacket;
import work.lclpnet.illwalls.network.StructureUpdatePacket;

public class ClientEntityManager {

    private final Logger logger;

    public ClientEntityManager(Logger logger) {
        this.logger = logger;
    }

    public void spawnEntity(EntityExtraSpawnPacket extraPacket, ClientWorld world) {
        final var packet = extraPacket.getPacket();

        var entityType = packet.getEntityType();
        var entity = entityType.create(world);

        if (entity == null) {
            logger.warn("Skipping entity with id {}", entityType);
            return;
        }

        entity.onSpawnPacket(packet);

        if (entity instanceof ExtraSpawnData extraSpawnData) {
            var data = extraPacket.getData();
            extraSpawnData.readExtraSpawnData(data);
        }

        int id = packet.getId();
        world.addEntity(id, entity);
    }

    public void updateIllusoryWall(StructureUpdatePacket packet, ClientWorld world) {
        final int entityId = packet.getEntityId();
        final var entity = world.getEntityById(entityId);

        if (!(entity instanceof StructureHolder holder)) {
            logger.warn("Skipping invalid illusory wall update for id {}", entityId);
            return;
        }

        StructureContainer structureContainer = holder.getStructureContainer();
        structureContainer.updateStructure(packet.getDeltaStructure());
    }
}
