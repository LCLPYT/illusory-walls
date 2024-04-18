package work.lclpnet.illwalls.event;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
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

            if (player == null || !player.getStackInHand(hand).isOf(STAFF_OF_ILLUSION_ITEM)) {
                return ActionResult.PASS;
            }

            if (!world.isClient && pos != null) {
                ActionResult result = StaffOfIllusionItem.onRightClickBlockEarlyServer((ServerPlayerEntity) player, (ServerWorld) world, pos);

                if (result != null) {
                    return result;
                }
            }

            return ActionResult.success(world.isClient);
        });
    }

    private void registryEvents() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.OPERATOR).register(entries -> {
            //noinspection UnstableApiUsage
            if (!entries.getContext().hasPermissions()) return;

            entries.add(STAFF_OF_ILLUSION_ITEM);
        });
    }

    private void preventWallModification() {
        BlockModificationHooks.PLACE_BLOCK.register((world, pos, entity, newState) -> {
            if (world.isClient || !(world instanceof ServerWorld serverWorld)) return false;

            // prevent block placement in an illusory wall
            return wallLookup.getWallAt(serverWorld, pos).isPresent();
        });

        BlockModificationHooks.PLACE_FLUID.register((world, pos, entity, newState) -> {
            if (world.isClient || !(world instanceof ServerWorld serverWorld)) return false;

            // prevent fluid placement in an illusory wall
            return wallLookup.getWallAt(serverWorld, pos).isPresent();
        });
    }

    private void registerDestroyWallEvents() {
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (world.isClient || hand != Hand.MAIN_HAND) return ActionResult.PASS;

            ItemStack stack = player.getStackInHand(hand);
            if (stack.isOf(STAFF_OF_ILLUSION_ITEM)) return ActionResult.PASS;

            BlockPos from = pos.offset(direction);
            boolean success = wallManager.fadeWallAtIfPresent((ServerWorld) world, pos, from);
            return success ? ActionResult.CONSUME : ActionResult.PASS;
        });

        ProjectileHooks.HIT_BLOCK.register((projectile, hit) -> {
            if (projectile.getWorld().isClient) return;

            BlockPos pos = hit.getBlockPos();
            IllusoryWallManager manager = IllusoryWallsApi.getInstance().manager();

            BlockPos from = pos.offset(hit.getSide());
            manager.fadeWallAtIfPresent((ServerWorld) projectile.getWorld(), pos, from);
        });
    }

    private void registerStaffHeldEvents() {
        PlayerInventoryHooks.SLOT_CHANGE.register((player, slot) -> PlayerInfo.get(player).updatePlayerCanSeeIllusoryWalls());
        PlayerGameModeChangeCallback.HOOK.register((player, gameMode) -> PlayerInfo.get(player).updatePlayerCanSeeIllusoryWalls());

        PlayerInventoryHooks.MODIFY_CREATIVE_INVENTORY.register(event -> {
            ServerPlayerEntity player = event.player();

            int handlerSlotIdx = event.slot();
            if (handlerSlotIdx < 1 || handlerSlotIdx > 45) return;

            Slot handlerSlot = player.currentScreenHandler.getSlot(handlerSlotIdx);
            if (handlerSlot == null) return;

            int slot = handlerSlot.getIndex();
            if (player.getInventory().selectedSlot != slot) return;

            ItemStack handStack = event.stack();
            if (handStack.isOf(STAFF_OF_ILLUSION_ITEM)) {
                PlayerInfo.get(player).setCanSeeIllusoryWalls(true);
                return;
            }

            ItemStack stack = player.getInventory().getStack(slot);

            if (stack.isOf(STAFF_OF_ILLUSION_ITEM) && handStack.isEmpty()) {
                PlayerInfo.get(player).setCanSeeIllusoryWalls(false);
            }
        });

        PlayerInventoryHooks.DROP_ITEM.register((player, slot) -> {
            if (player.getWorld().isClient) return false;

            ItemStack stack = player.getInventory().getStack(slot);

            if (stack.isOf(STAFF_OF_ILLUSION_ITEM)) {
                PlayerInfo.get((ServerPlayerEntity) player).setCanSeeIllusoryWalls(false);
            }

            return false;
        });

        PlayerInventoryHooks.PLAYER_PICKED_UP.register((player, itemEntity) -> {
            if (player.getWorld().isClient) return;

            PlayerInfo.get((ServerPlayerEntity) player).updatePlayerCanSeeIllusoryWalls();
        });
    }
}
