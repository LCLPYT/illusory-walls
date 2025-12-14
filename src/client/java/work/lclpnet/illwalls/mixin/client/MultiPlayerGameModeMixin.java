package work.lclpnet.illwalls.mixin.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import work.lclpnet.illwalls.network.AttackBlockAdventureC2SPacket;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {

    @Shadow @Final private Minecraft minecraft;

    @Shadow private GameType localPlayerMode;

    @Inject(
            method = "startDestroyBlock",
            at = @At("HEAD")
    )
    public void illwalls$onAttackBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        LocalPlayer player = this.minecraft.player;

        if (player == null || player.isSpectator() || !player.blockActionRestricted(this.minecraft.level, pos, this.localPlayerMode)) return;

        ClientPlayNetworking.send(new AttackBlockAdventureC2SPacket(pos, direction));
    }
}
