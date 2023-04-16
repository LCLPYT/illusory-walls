package work.lclpnet.illwalls.struct;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import work.lclpnet.illwalls.network.ServerNetworkHandler;
import work.lclpnet.illwalls.network.StructureUpdatePacket;
import work.lclpnet.kibu.structure.BlockStructure;

import static work.lclpnet.illwalls.struct.FabricStructureWrapper.createSimpleStructure;

public class StructureContainer {

    private final Entity entity;
    private FabricStructureWrapper structure;

    public StructureContainer(Entity entity) {
        this.entity = entity;
        this.structure = makeStructure(FabricStructureWrapper.createSimpleStructure());
    }

    private boolean existsInWorld() {
        return entity.world.getEntityById(entity.getId()) != null;
    }

    private void onUpdate(BlockPos pos, BlockState state) {
        if (entity.world.isClient) return;

        if (this.structure.getStructure().isEmpty()) {
            entity.discard();
            return;
        }

        center(entity, this.structure);

        // on the server world, update send a delta update structure to the players
        var deltaStructure = createSimpleStructure();
        var adapter = FabricBlockStateAdapter.getInstance();

        if (pos == null && state == null && structure instanceof StructureBatchUpdate batchUpdate) {
            for (var entry : batchUpdate.getBatch().entrySet()) {
                BlockPos batchPos = entry.getKey();
                BlockState batchState = entry.getValue();

                // air blocks will be skipped by the serializer. Therefore, put a special block state with a unique id
                var deltaState = batchState.isAir() ? EmptyBlockState.INSTANCE : adapter.adapt(batchState);
                deltaStructure.setBlockState(adapter.adapt(batchPos), deltaState);
            }
        } else if (pos != null && state != null) {
            // air blocks will be skipped by the serializer. Therefore, put a special block state with a unique id
            var deltaState = state.isAir() ? EmptyBlockState.INSTANCE : adapter.adapt(state);
            deltaStructure.setBlockState(adapter.adapt(pos), deltaState);
        }

        if (!deltaStructure.isEmpty()) {
            updateStructure(deltaStructure);
        }
    }

    public void updateStructure(BlockStructure delta) {
        if (!entity.world.isClient) {
            if (!this.existsInWorld()) return;  // too early

            // if we are in the server world, send an update packet
            var updatePacket = new StructureUpdatePacket(entity.getId(), delta);
            ServerNetworkHandler.send(updatePacket, PlayerLookup.tracking(entity));
            return;
        }

        // sync the received delta structure
        var deltaWrapper = new FabricStructureWrapper(delta);
        var positions = deltaWrapper.getBlockPositions();

        positions.forEach(pos -> structure.setBlockState(pos, deltaWrapper.getBlockState(pos)));
    }

    private FabricStructureWrapper makeStructure(BlockStructure structure) {
        return new ListenerStructureWrapper(structure, this::onUpdate);
    }

    public FabricStructureWrapper getWrapper() {
        return structure;
    }

    public void setStructure(BlockStructure structure) {
        this.structure = makeStructure(structure);
    }

    /**
     * Moves this entity to the nearest block of the center of the structure.
     * This is done, so that the wall loads evenly from all directions.
     */
    public static void center(Entity entity, FabricStructureWrapper structure) {
        BlockPos center = structure.getCenter();
        int centerX = center.getX();
        int centerY = center.getY();
        int centerZ = center.getZ();

        // check whether the wall is in the center already
        var currentPos = entity.getBlockPos();
        if (currentPos.getX() == centerX && currentPos.getY() == centerY && currentPos.getZ() == centerZ) return;

        entity.setPosition(centerX, centerY, centerZ);
    }
}
