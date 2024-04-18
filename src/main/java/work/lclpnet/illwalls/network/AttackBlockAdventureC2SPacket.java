package work.lclpnet.illwalls.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import work.lclpnet.illwalls.IllusoryWallsMod;

public class AttackBlockAdventureC2SPacket implements PacketSerializer {

    public static final Identifier ID = IllusoryWallsMod.identifier("attack_block");
    private final BlockPos pos;
    private final Direction direction;

    public AttackBlockAdventureC2SPacket(BlockPos pos, Direction direction) {
        this.pos = pos;
        this.direction = direction;
    }

    public AttackBlockAdventureC2SPacket(PacketByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.direction = buf.readEnumConstant(Direction.class);
    }

    @Override
    public void writeTo(PacketByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeEnumConstant(direction);
    }

    @Override
    public Identifier getIdentifier() {
        return ID;
    }

    public BlockPos getPos() {
        return pos;
    }

    public Direction getDirection() {
        return direction;
    }
}
