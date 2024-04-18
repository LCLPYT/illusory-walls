package work.lclpnet.illwalls.struct;

import work.lclpnet.kibu.mc.KibuBlockState;

public class EmptyBlockState implements KibuBlockState {

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
