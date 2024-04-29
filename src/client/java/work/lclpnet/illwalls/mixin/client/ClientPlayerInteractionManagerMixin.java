package work.lclpnet.illwalls.mixin.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import work.lclpnet.illwalls.network.AttackBlockAdventureC2SPacket;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {

    @Shadow @Final private MinecraftClient client;

    @Shadow private GameMode gameMode;

    @Inject(
            method = "attackBlock",
            at = @At("HEAD")
    )
    public void illwalls$onAttackBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        ClientPlayerEntity player = this.client.player;

        if (player == null || player.isSpectator() || !player.isBlockBreakingRestricted(this.client.world, pos, this.gameMode)) return;

        ClientPlayNetworking.send(new AttackBlockAdventureC2SPacket(pos, direction));
    }
}
