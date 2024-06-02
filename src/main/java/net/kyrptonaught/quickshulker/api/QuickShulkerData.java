package net.kyrptonaught.quickshulker.api;

import net.kyrptonaught.quickshulker.QuickShulkerMod;
import net.kyrptonaught.shulkerutils.ShulkerUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class QuickShulkerData {
    public BiConsumer<PlayerEntity, ItemStack> openConsumer;
    BiFunction<PlayerEntity, ItemStack, Inventory> bundleInvGetter;
    CanBundleInsertItemFunction canBundleInsertItem;

    public boolean supportsBundleing = false;
    public boolean ignoreSingleStackCheck = false;
    public boolean canOpenInHand = true;

    public QuickShulkerData() {

    }

    public QuickShulkerData(BiConsumer<PlayerEntity, ItemStack> openConsumer, Boolean supportsBundleing) {
        this.openConsumer = openConsumer;
        this.supportsBundleing = supportsBundleing;
    }

    public QuickShulkerData(BiConsumer<PlayerEntity, ItemStack> openConsumer, Boolean supportsBundleing, Boolean ignoreSingleStackCheck) {
        this.openConsumer = openConsumer;
        this.supportsBundleing = supportsBundleing;
        this.ignoreSingleStackCheck = ignoreSingleStackCheck;
    }

    public Inventory getInventory(PlayerEntity player, ItemStack stack) {
        if (bundleInvGetter != null) return bundleInvGetter.apply(player, stack);
        return ShulkerUtils.getInventoryFromShulker(stack);
    }

    public boolean canBundleInsertItem(PlayerEntity player, Inventory inventory, ItemStack hostStack, ItemStack insertStack) {
        // Check if the insertStack is a shulker box with the forbidden names
        if (hostStack.getName().getString().equals("更新抑制器") || hostStack.getName().getString().equalsIgnoreCase("updateSuppression")) {
            // If it is, prevent insertion
            player.sendMessage(Text.literal("你不能向更新抑制器潜影盒里添加物品！因为这会崩服！"), false);
            return false;
        }
        // Otherwise, proceed with default insert check
        if (canBundleInsertItem != null)
            return canBundleInsertItem.canBundleInsertItem(player, inventory, hostStack, insertStack);
        return true;
    }

    public static class QuickEnderData extends QuickShulkerData {
        public QuickEnderData() {
            super();
            canBundleInsertItem = CanBundleInsertItemFunction.ALWAYS;
        }

        public QuickEnderData(BiConsumer<PlayerEntity, ItemStack> openConsumer, Boolean supportsBundleing) {
            super(openConsumer, supportsBundleing);
            canBundleInsertItem = CanBundleInsertItemFunction.ALWAYS;
        }

        public QuickEnderData(BiConsumer<PlayerEntity, ItemStack> openConsumer, Boolean supportsBundleing, Boolean ignoreSingleStackCheck) {
            super(openConsumer, supportsBundleing, ignoreSingleStackCheck);
            canBundleInsertItem = CanBundleInsertItemFunction.ALWAYS;
        }

        public Inventory getInventory(PlayerEntity player, ItemStack stack) {
            if (!QuickShulkerMod.getConfig().quickEChest)
                return null;
            return player.getEnderChestInventory();
        }
    }
}
