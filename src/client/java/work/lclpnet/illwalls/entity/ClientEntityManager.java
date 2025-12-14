package work.lclpnet.illwalls.entity;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.EntitySpawnReason;
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

    public void spawnEntity(EntityExtraSpawnS2CPacket extraPacket, ClientLevel world) {
        final var packet = extraPacket.packet();

        var entityType = packet.getType();
        var entity = entityType.create(world, EntitySpawnReason.LOAD);

        if (entity == null) {
            logger.warn("Skipping entity with id {}", entityType);
            return;
        }

        entity.recreateFromPacket(packet);

        if (entity instanceof ExtraSpawnData extraSpawnData) {
            var data = extraPacket.data();
            extraSpawnData.readExtraSpawnData(data);
        }

        entity.setId(packet.getId());
        world.addEntity(entity);
    }

    public void updateIllusoryWall(StructureUpdateS2CPacket packet, ClientLevel world) {
        final int entityId = packet.entityId();
        final var entity = world.getEntity(entityId);

        if (!(entity instanceof StructureHolder holder)) {
            logger.warn("Skipping invalid illusory wall update for id {}", entityId);
            return;
        }

        StructureContainer structureContainer = holder.getStructureContainer();
        structureContainer.updateStructure(packet.deltaStructure());
    }
}
