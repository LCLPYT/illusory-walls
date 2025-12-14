package work.lclpnet.illwalls.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import work.lclpnet.illwalls.util.PlayerInfo;
import work.lclpnet.illwalls.util.PlayerInfoView;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin implements PlayerInfoView {

    @Unique
    private PlayerInfo playerInfo;

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    public void illwalls$onInit(MinecraftServer server, ServerLevel world, GameProfile profile, ClientInformation clientOptions, CallbackInfo ci) {
        playerInfo = PlayerInfo.create((ServerPlayer) (Object) this);
    }

    @Override
    public PlayerInfo illwalls$getPlayerInfo() {
        return playerInfo;
    }
}
