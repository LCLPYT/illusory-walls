package work.lclpnet.illwalls.impl;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import work.lclpnet.illwalls.entity.IllusoryWallEntity;
import work.lclpnet.kibu.structure.BlockStructure;

import java.util.Objects;

public class IllusoryWallStructure extends FabricStructureWrapper {

    private final IllusoryWallEntity entity;

    public IllusoryWallStructure(IllusoryWallEntity entity) {
        this(entity, createSimpleStructure());
    }

    public IllusoryWallStructure(IllusoryWallEntity entity, BlockStructure structure) {
        super(structure);
        this.entity = Objects.requireNonNull(entity);
    }

    @Override
    public void setBlockState(BlockPos pos, BlockState state) {
        super.setBlockState(pos, state);

        if (entity.world.isClient) return;

        // move the wall entity to the block in the center of the structure
        entity.center();

        // on the server world, update send a delta update structure to the players
        var deltaStructure = createSimpleStructure();
        deltaStructure.setBlockState(adapter.adapt(pos), adapter.adapt(state));

        entity.updateStructure(deltaStructure);
    }
}
