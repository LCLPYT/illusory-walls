package work.lclpnet.illwalls.struct;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import work.lclpnet.kibu.schematic.FabricStructureWrapper;
import work.lclpnet.kibu.structure.BlockStructure;

public class ExtendedStructureWrapper extends FabricStructureWrapper {

    public ExtendedStructureWrapper() {
        super(createSimpleStructure());
    }

    public ExtendedStructureWrapper(BlockStructure structure) {
        super(structure, ExtendedBlockStateAdapter.getInstance());
    }

    public boolean isInBounds(Vec3i pos) {
        var state = getStructure().getBlockState(adapter.adapt(pos));
        return state != null && !state.isAir();
    }

    public BlockPos getCenter() {
        BlockStructure structure = getStructure();
        var origin = structure.getOrigin();

        return new BlockPos(
                origin.getX() + structure.getWidth() / 2,
                origin.getY() + structure.getHeight() / 2,
                origin.getZ() + structure.getLength() / 2
        );
    }
}
