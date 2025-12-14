package work.lclpnet.illwalls.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import work.lclpnet.illwalls.entity.IllusoryWallEntity;
import work.lclpnet.illwalls.util.PlayerInfo;
import work.lclpnet.illwalls.wall.IllusoryWallManager;
import work.lclpnet.illwalls.wall.IllusoryWallPlayerSettings;

import java.util.Collection;

import static net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver;
import static work.lclpnet.illwalls.IllusoryWallsMod.STAFF_OF_ILLUSION_ITEM;

public class ServerNetworkHandler {

    private final IllusoryWallManager wallManager;

    public ServerNetworkHandler(IllusoryWallManager wallManager) {
        this.wallManager = wallManager;
    }

    public void init() {
        var playS2C = PayloadTypeRegistry.playS2C();
        playS2C.register(EntityExtraSpawnS2CPacket.ID, EntityExtraSpawnS2CPacket.CODEC);
        playS2C.register(StructureUpdateS2CPacket.ID, StructureUpdateS2CPacket.CODEC);
        playS2C.register(EditWallScreenS2CPacket.ID, EditWallScreenS2CPacket.CODEC);

        var playC2S = PayloadTypeRegistry.playC2S();
        playC2S.register(ApplyWallSettingsC2SPacket.ID, ApplyWallSettingsC2SPacket.CODEC);
        playC2S.register(AttackBlockAdventureC2SPacket.ID, AttackBlockAdventureC2SPacket.CODEC);

        registerGlobalReceiver(ApplyWallSettingsC2SPacket.ID, this::applyWallSettings);
        registerGlobalReceiver(AttackBlockAdventureC2SPacket.ID, this::attackBlockAdventure);
    }

    private void applyWallSettings(ApplyWallSettingsC2SPacket payload, ServerPlayNetworking.Context context) {
        ServerPlayer player = context.player();

        if (!player.canUseGameMasterBlocks()) return;

        final ServerLevel world = player.level();

        world.getServer().execute(() -> {
            int entityId = payload.entityId();
            IllusoryWallEntity wallEntity = null;

            if (entityId != -1) {
                Entity entity = world.getEntity(entityId);

                if (entity instanceof IllusoryWallEntity) {
                    wallEntity = (IllusoryWallEntity) entity;
                }
            }

            final IllusoryWallPlayerSettings settings = payload.settings();

            if (wallEntity != null) {
                settings.applyTo(wallEntity);
            } else {
                PlayerInfo.get(player).getWallSettings().copyFrom(settings);
            }
        });
    }

    private void attackBlockAdventure(AttackBlockAdventureC2SPacket payload, ServerPlayNetworking.Context context) {
        ServerPlayer player = context.player();
        final ServerLevel world = player.level();

        world.getServer().execute(() -> {
            BlockPos pos = payload.pos();

            if (player.isSpectator()) {
                // spectators should not be able to trigger illusory walls
                return;
            }

            if (!player.canInteractWithBlock(pos, 1.0)) {
                // too far
                return;
            }

            if (!player.blockActionRestricted(world, pos, player.gameMode.getGameModeForPlayer())) {
                // handled by default attack block packet
                return;
            }

            ItemStack stack = player.getMainHandItem();

            if (stack.is(STAFF_OF_ILLUSION_ITEM)) {
                // staff of illusion has different behaviour
                return;
            }

            Direction direction = payload.direction();
            BlockPos from = pos.relative(direction);

            wallManager.fadeWallAtIfPresent(world, pos, from);
        });
    }

    public static void send(CustomPacketPayload packet, Collection<ServerPlayer> players) {
        players.forEach(player -> ServerPlayNetworking.send(player, packet));
    }

    @SuppressWarnings("unchecked")
    public static <T extends ClientCommonPacketListener> Packet<T> createS2CPacket(CustomPacketPayload packet) {
        return (Packet<T>) ServerPlayNetworking.createS2CPacket(packet);
    }
}
