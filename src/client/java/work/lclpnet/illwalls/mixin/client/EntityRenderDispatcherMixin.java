package work.lclpnet.illwalls.mixin.client;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import work.lclpnet.illwalls.render.StructureEntityBatchRenderer;
import work.lclpnet.illwalls.render.IllusoryBatchRendererProvider;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin implements IllusoryBatchRendererProvider {

    @Unique
    private StructureEntityBatchRenderer structureRenderer = null;

    @Override
    public void illwalls$setStructureEntityRenderer(StructureEntityBatchRenderer renderer) {
        structureRenderer = renderer;
    }

    @Override
    public @Nullable StructureEntityBatchRenderer illwalls$getStructureEntityRenderer() {
        return structureRenderer;
    }
}
