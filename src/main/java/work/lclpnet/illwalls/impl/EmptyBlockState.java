package work.lclpnet.illwalls.impl;

import work.lclpnet.kibu.mc.BlockState;

public class EmptyBlockState implements BlockState {

    public static final String ID = "@EMPTY";
    public static final EmptyBlockState INSTANCE = new EmptyBlockState();

    private EmptyBlockState() {}

    @Override
    public String getAsString() {
        return ID;
    }

    @Override
    public boolean isAir() {
        return false;  // this block state is air, but it should be serialized so that we can receive it on the client
    }
}
