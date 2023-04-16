package work.lclpnet.illwalls.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import work.lclpnet.illwalls.util.PlayerInfo;
import work.lclpnet.illwalls.util.PlayerInfoView;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements PlayerInfoView {

    @Unique
    private PlayerInfo playerInfo;

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    public void illwalls$onInit(MinecraftServer server, ServerWorld world, GameProfile profile, CallbackInfo ci) {
        playerInfo = PlayerInfo.create((ServerPlayerEntity) (Object) this);
    }

    @Override
    public PlayerInfo illwalls$getPlayerInfo() {
        return playerInfo;
    }
}
