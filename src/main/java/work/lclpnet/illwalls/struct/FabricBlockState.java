package work.lclpnet.illwalls.struct;

import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import work.lclpnet.kibu.mc.BlockState;

import javax.annotation.Nonnull;

public class FabricBlockState implements BlockState {

    private transient final net.minecraft.block.BlockState state;
    private volatile String string = null;

    public FabricBlockState(net.minecraft.block.BlockState state) {
        this.state = state;
    }

    @Override
    public String getAsString() {
        if (string == null) {
            synchronized (this) {
                if (string == null) {
                    string = buildString();
                }
            }
        }

        return string;
    }

    @Nonnull
    private String buildString() {
        Block block = state.getBlock();
        Identifier blockId = Registries.BLOCK.getId(block);

        var builder = new StringBuilder();
        builder.append(blockId);

        var props = state.getEntries();
        if (!props.isEmpty()) {
            boolean firstProp = true;

            builder.append('[');

            for (var entry : props.entrySet()) {
                if (entry == null) continue;

                var prop = entry.getKey();
                String value = nameValue(prop, entry.getValue());

                if (firstProp) {
                    firstProp = false;
                } else {
                    builder.append(",");
                }

                builder.append(prop.getName()).append("=").append(value);
            }

            builder.append(']');
        }

        return builder.toString();
    }

    @Override
    public boolean isAir() {
        return state.isAir();
    }

    public net.minecraft.block.BlockState getState() {
        return state;
    }

    @SuppressWarnings("unchecked")
    private <T extends Comparable<T>> String nameValue(Property<T> property, Comparable<?> value) {
        return property.name((T) value);
    }
}
