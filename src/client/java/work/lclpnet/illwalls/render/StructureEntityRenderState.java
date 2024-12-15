package work.lclpnet.illwalls.render;

import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.util.math.BlockPos;
import work.lclpnet.kibu.schematic.FabricStructureView;

public class StructureEntityRenderState extends EntityRenderState {

    public boolean fading;
    public long fadeStartMs;
    public int fadeMode;
    public BlockPos fadingFrom;
    public int blockLight;
    public int skyLight;
    public FabricStructureView structure;
}
