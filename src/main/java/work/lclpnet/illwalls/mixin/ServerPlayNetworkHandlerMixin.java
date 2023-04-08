package work.lclpnet.illwalls.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import work.lclpnet.illwalls.IllusoryWallsMod;
import work.lclpnet.illwalls.entity.PlayerInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

    @Shadow public ServerPlayerEntity player;

    @Inject(
            method = "onUpdateSelectedSlot",
            at = @At("TAIL")
    )
    public void illwalls$onUpdateSelectedSlot(UpdateSelectedSlotC2SPacket packet, CallbackInfo ci) {
        final ItemStack stack = this.player.getInventory().getMainHandStack();
        final PlayerInfo playerInfo = PlayerInfo.get(this.player);
        final boolean canSeeIllusoryWalls = playerInfo.canSeeIllusoryWalls();

        if (stack.isOf(IllusoryWallsMod.STAFF_OF_ILLUSION_ITEM)) {
            if (!canSeeIllusoryWalls) {
                playerInfo.setCanSeeIllusoryWalls(true);
            }
        } else {
            if (canSeeIllusoryWalls) {
                playerInfo.setCanSeeIllusoryWalls(false);
            }
        }
    }
}
