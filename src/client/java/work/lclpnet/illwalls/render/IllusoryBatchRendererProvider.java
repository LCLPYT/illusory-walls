package work.lclpnet.illwalls.render;

public interface IllusoryBatchRendererProvider {

    void illwalls$setStructureEntityRenderer(StructureEntityBatchRenderer renderer);

    StructureEntityBatchRenderer illwalls$getStructureEntityRenderer();
}
