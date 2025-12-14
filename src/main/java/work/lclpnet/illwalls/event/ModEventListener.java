package work.lclpnet.illwalls.event;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import work.lclpnet.illwalls.IllusoryWallsApi;
import work.lclpnet.illwalls.item.StaffOfIllusionItem;
import work.lclpnet.illwalls.util.PlayerInfo;
import work.lclpnet.illwalls.wall.IllusoryWallLookup;
import work.lclpnet.illwalls.wall.IllusoryWallManager;
import work.lclpnet.kibu.hook.entity.ProjectileHooks;
import work.lclpnet.kibu.hook.player.PlayerGameModeChangeCallback;
import work.lclpnet.kibu.hook.player.PlayerInventoryHooks;
import work.lclpnet.kibu.hook.world.BlockModificationHooks;

import static work.lclpnet.illwalls.IllusoryWallsMod.STAFF_OF_ILLUSION_ITEM;

public class ModEventListener {

    private final IllusoryWallManager wallManager;
    private final IllusoryWallLookup wallLookup;

    public ModEventListener(IllusoryWallManager wallManager, IllusoryWallLookup wallLookup) {
        this.wallManager = wallManager;
        this.wallLookup = wallLookup;
    }

    public void register() {
        registryEvents();

        registerEditWallEvents();

        registerDestroyWallEvents();

        preventWallModification();

        registerStaffHeldEvents();
    }

    private void registerEditWallEvents() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            BlockPos pos = hitResult.getBlockPos();

            if (player == null || !player.getItemInHand(hand).is(STAFF_OF_ILLUSION_ITEM)) {
                return InteractionResult.PASS;
            }

            if (!world.isClientSide() && pos != null) {
                InteractionResult result = StaffOfIllusionItem.onRightClickBlockEarlyServer((ServerPlayer) player, (ServerLevel) world, pos);

                if (result != null) {
                    return result;
                }
            }

            return InteractionResult.SUCCESS;
        });
    }

    private void registryEvents() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.OP_BLOCKS).register(entries -> {
            if (!entries.getContext().hasPermissions()) return;

            entries.accept(STAFF_OF_ILLUSION_ITEM);
        });
    }

    private void preventWallModification() {
        BlockModificationHooks.PLACE_BLOCK.register((world, pos, entity, newState) -> {
            if (world.isClientSide() || !(world instanceof ServerLevel serverWorld)) return false;

            // prevent block placement in an illusory wall
            return wallLookup.getWallAt(serverWorld, pos).isPresent();
        });

        BlockModificationHooks.PLACE_FLUID.register((world, pos, entity, newState) -> {
            if (world.isClientSide() || !(world instanceof ServerLevel serverWorld)) return false;

            // prevent fluid placement in an illusory wall
            return wallLookup.getWallAt(serverWorld, pos).isPresent();
        });
    }

    private void registerDestroyWallEvents() {
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (world.isClientSide() || hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

            ItemStack stack = player.getItemInHand(hand);
            if (stack.is(STAFF_OF_ILLUSION_ITEM)) return InteractionResult.PASS;

            BlockPos from = pos.relative(direction);
            boolean success = wallManager.fadeWallAtIfPresent((ServerLevel) world, pos, from);
            return success ? InteractionResult.CONSUME : InteractionResult.PASS;
        });

        ProjectileHooks.HIT_BLOCK.register((projectile, hit) -> {
            if (projectile.level().isClientSide()) return;

            BlockPos pos = hit.getBlockPos();
            IllusoryWallManager manager = IllusoryWallsApi.getInstance().manager();

            BlockPos from = pos.relative(hit.getDirection());
            manager.fadeWallAtIfPresent((ServerLevel) projectile.level(), pos, from);
        });
    }

    private void registerStaffHeldEvents() {
        PlayerInventoryHooks.SLOT_CHANGE.register((player, slot) -> PlayerInfo.get(player).updatePlayerCanSeeIllusoryWalls());
        PlayerGameModeChangeCallback.HOOK.register((player, gameMode) -> PlayerInfo.get(player).updatePlayerCanSeeIllusoryWalls());

        PlayerInventoryHooks.MODIFY_CREATIVE_INVENTORY.register(event -> {
            ServerPlayer player = event.player();

            int handlerSlotIdx = event.slot();
            if (handlerSlotIdx < 1 || handlerSlotIdx > 45) return;

            Slot handlerSlot = player.containerMenu.getSlot(handlerSlotIdx);
            if (handlerSlot == null) return;

            int slot = handlerSlot.getContainerSlot();
            if (player.getInventory().getSelectedSlot() != slot) return;

            ItemStack handStack = event.stack();
            if (handStack.is(STAFF_OF_ILLUSION_ITEM)) {
                PlayerInfo.get(player).setCanSeeIllusoryWalls(true);
                return;
            }

            ItemStack stack = player.getInventory().getItem(slot);

            if (stack.is(STAFF_OF_ILLUSION_ITEM) && handStack.isEmpty()) {
                PlayerInfo.get(player).setCanSeeIllusoryWalls(false);
            }
        });

        PlayerInventoryHooks.DROP_ITEM.register((player, slot, inInventory) -> {
            if (player.level().isClientSide()) return false;

            Inventory inventory = player.getInventory();

            if (slot < 0 || slot >= inventory.getContainerSize()) return false;

            ItemStack stack = inventory.getItem(slot);

            if (stack.is(STAFF_OF_ILLUSION_ITEM)) {
                PlayerInfo.get((ServerPlayer) player).setCanSeeIllusoryWalls(false);
            }

            return false;
        });

        PlayerInventoryHooks.PLAYER_PICKED_UP.register((player, itemEntity) -> {
            if (player.level().isClientSide()) return;

            PlayerInfo.get((ServerPlayer) player).updatePlayerCanSeeIllusoryWalls();
        });
    }
}
