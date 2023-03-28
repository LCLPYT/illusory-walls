package work.lclpnet.illwalls.structure;

import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import java.util.Optional;

public class DirectBlockStateGetter implements BlockStateGetter {

    @Override
    public @Nullable BlockState getBlockState(String string) {
        String blockPart = string;
        String propertiesPart = null;

        int propertiesStart = string.indexOf('[');

        if (propertiesStart != -1) {
            blockPart = string.substring(0, propertiesStart);

            int propertiesEnd = string.indexOf(']');
            if (propertiesEnd <= propertiesStart) return null;

            propertiesPart = string.substring(propertiesStart + 1, propertiesEnd);
        }

        var identifier = new Identifier(blockPart);
        var block = Registries.BLOCK.get(identifier);
        var state = block.getDefaultState();

        if (propertiesPart == null) return state;

        var stateManager = block.getStateManager();
        var properties = propertiesPart.split(",");

        for (var property : properties) {
            var parsed = property.split("=");
            if (parsed.length != 2) continue;

            Property<?> prop = stateManager.getProperty(parsed[0]);
            if (prop == null) continue;

            state = with(state, prop, parsed[1]);
        }

        return state;
    }

    private <T extends Comparable<T>> BlockState with(BlockState state, Property<T> property, String rawValue) {
        Optional<T> value = property.parse(rawValue);
        if (value.isEmpty()) return state;

        return state.with(property, value.get());
    }
}
