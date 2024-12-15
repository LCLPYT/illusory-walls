package work.lclpnet.illwalls.entity;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.SpawnReason;
import org.slf4j.Logger;
import work.lclpnet.illwalls.network.EntityExtraSpawnS2CPacket;
import work.lclpnet.illwalls.network.StructureUpdateS2CPacket;
import work.lclpnet.illwalls.struct.StructureContainer;
import work.lclpnet.illwalls.struct.StructureHolder;

public class ClientEntityManager {

    private final Logger logger;

    public ClientEntityManager(Logger logger) {
        this.logger = logger;
    }

    public void spawnEntity(EntityExtraSpawnS2CPacket extraPacket, ClientWorld world) {
        final var packet = extraPacket.packet();

        var entityType = packet.getEntityType();
        var entity = entityType.create(world, SpawnReason.LOAD);

        if (entity == null) {
            logger.warn("Skipping entity with id {}", entityType);
            return;
        }

        entity.onSpawnPacket(packet);

        if (entity instanceof ExtraSpawnData extraSpawnData) {
            var data = extraPacket.data();
            extraSpawnData.readExtraSpawnData(data);
        }

        entity.setId(packet.getEntityId());
        world.addEntity(entity);
    }

    public void updateIllusoryWall(StructureUpdateS2CPacket packet, ClientWorld world) {
        final int entityId = packet.entityId();
        final var entity = world.getEntityById(entityId);

        if (!(entity instanceof StructureHolder holder)) {
            logger.warn("Skipping invalid illusory wall update for id {}", entityId);
            return;
        }

        StructureContainer structureContainer = holder.getStructureContainer();
        structureContainer.updateStructure(packet.deltaStructure());
    }
}
