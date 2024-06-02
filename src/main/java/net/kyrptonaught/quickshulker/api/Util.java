package net.kyrptonaught.quickshulker.api;

import net.kyrptonaught.quickshulker.ItemInventoryContainer;
import net.kyrptonaught.quickshulker.QuickShulkerMod;
import net.kyrptonaught.quickshulker.network.OpenInventoryPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class Util {

    public static void openItem(PlayerEntity player, int invSlot) {
        if (invSlot < 0) {
            System.out.println("[QuickShulker]: unknown slot opened");
            // 不阻止崩溃可能会更容易调试修复。
        }
        openItem(player, invSlot, player.currentScreenHandler.slots.get(invSlot).getIndex());
    }

    public static void openItem(PlayerEntity player, int invSlot, int playerInvIndex) {
        ItemStack stack = player.getInventory().getStack(playerInvIndex);

        // 拒绝打开名为“更新抑制器”的潜影盒
        if (stack.getName().getString().equals("更新抑制器") || stack.getName().getString().equalsIgnoreCase("updateSuppression")) {
            player.sendMessage(Text.literal("你不能打开更新抑制器潜影盒！"), false);
            return;
        }

        if (QuickShulkerMod.getConfig().rightClickClose && playerInvIndex == ((ItemInventoryContainer) player.currentScreenHandler).getUsedSlotInPlayerInv()) {
            ((ServerPlayerEntity) player).closeHandledScreen();
            OpenInventoryPacket.send((ServerPlayerEntity) player);
            return;
        }
        stack.removeSubNbt(QuickShulkerMod.MOD_ID);
        QuickShulkerData qsData = QuickOpenableRegistry.getQuickie(stack.getItem());
        if (qsData != null) {
            qsData.openConsumer.accept(player, stack);
            ((ItemInventoryContainer) player.currentScreenHandler).setUsedSlot(playerInvIndex);
            player.currentScreenHandler.addListener(forceCloseScreenIfNotPresent(player, playerInvIndex, stack));
        }
    }

    public static Boolean isOpenableItem(ItemStack stack) {
        QuickShulkerData qsdata = QuickOpenableRegistry.getQuickie(stack.getItem());
        if (qsdata == null) return false;
        return qsdata.ignoreSingleStackCheck || stack.getCount() <= 1;
    }

    public static Inventory getQuickItemInventory(PlayerEntity player, ItemStack stack) {
        QuickShulkerData qsData = QuickOpenableRegistry.getQuickie(stack.getItem());
        if (qsData != null) {
            if (qsData.supportsBundleing)
                return qsData.getInventory(player, stack);
        }
        return null;
    }

    public static boolean canOpenInHand(ItemStack stack) {
        QuickShulkerData qsData = QuickOpenableRegistry.getQuickie(stack.getItem());
        if (qsData != null) {
            return qsData.canOpenInHand;
        }
        return false;
    }

    public static boolean areItemsEqual(ItemStack stack1, ItemStack stack2) {
        return ItemStack.areItemsEqual(stack1, stack2) && ItemStack.areEqual(stack1, stack2) && stack1.getCount() == stack2.getCount();
    }

    public static ScreenHandlerListener forceCloseScreenIfNotPresent(PlayerEntity player, int slotID, ItemStack stack) {
        return new ScreenHandlerListener() {
            @Override
            public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stack) {
                isValid();
            }

            @Override
            public void onPropertyUpdate(ScreenHandler handler, int property, int value) {
                isValid();
            }

            public void isValid() {
                if (!areItemsEqual(stack, player.getInventory().getStack(slotID))) {
                    ((ServerPlayerEntity) player).closeHandledScreen();
                }
            }
        };
    }
}
