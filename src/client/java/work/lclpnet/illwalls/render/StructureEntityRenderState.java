package work.lclpnet.illwalls.render;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.BlockPos;
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
