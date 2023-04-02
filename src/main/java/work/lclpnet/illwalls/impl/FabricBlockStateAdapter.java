package work.lclpnet.illwalls.impl;

import net.minecraft.registry.Registries;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import work.lclpnet.kibu.mc.BlockPos;
import work.lclpnet.kibu.mc.BlockState;
import work.lclpnet.kibu.mc.BlockStateAdapter;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FabricBlockStateAdapter implements BlockStateAdapter {

    private final Map<net.minecraft.block.BlockState, BlockState> states = new HashMap<>();

    protected FabricBlockStateAdapter() {
    }

    public static FabricBlockStateAdapter getInstance() {
        return InstanceHolder.instance;
    }

    @Nullable
    @Override
    public BlockState getBlockState(String string) {
        var nativeBlockState = getNativeBlockState(string);
        return nativeBlockState != null ? adapt(nativeBlockState) : null;
    }

    @Nullable
    public net.minecraft.block.BlockState getNativeBlockState(String string) {
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

    @Nullable
    public net.minecraft.block.BlockState revert(BlockState state) {
        if (state instanceof FabricBlockState fState) {
            return fState.getState();
        }

        // fallback to parsing the string representation (slow)
        return getNativeBlockState(state.getAsString());
    }

    public net.minecraft.util.math.BlockPos revert(BlockPos pos) {
        return new net.minecraft.util.math.BlockPos(pos.getX(), pos.getY(), pos.getZ());
    }

    public BlockState adapt(net.minecraft.block.BlockState state) {
        return states.computeIfAbsent(state, FabricBlockState::new);
    }

    public BlockPos adapt(net.minecraft.util.math.BlockPos pos) {
        return new BlockPos(pos.getX(), pos.getY(), pos.getZ());
    }

    private <T extends Comparable<T>> net.minecraft.block.BlockState with(net.minecraft.block.BlockState state, Property<T> property, String rawValue) {
        Optional<T> value = property.parse(rawValue);
        if (value.isEmpty()) return state;

        return state.with(property, value.get());
    }

    private static final class InstanceHolder {
        private static final FabricBlockStateAdapter instance = new FabricBlockStateAdapter();
    }
}
