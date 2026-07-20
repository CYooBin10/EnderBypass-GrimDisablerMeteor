package com.cyoo.addon.utils;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;
import meteordevelopment.meteorclient.utils.player.InvUtils;

public class TridentChecker {
    public static int findSlot(PlayerEntity player) {
        return InvUtils.findInHotbar(net.minecraft.item.Items.TRIDENT).slot();
    }

    public static ItemStack getStack(PlayerEntity player, int slot) {
        return player.getInventory().getStack(slot);
    }

    public static boolean hasRiptideIII(World world, ItemStack stack) {
        if (stack.isEmpty()) return false;
        Registry<net.minecraft.enchantment.Enchantment> reg = world.getRegistryManager()
            .getOrThrow(net.minecraft.registry.RegistryKeys.ENCHANTMENT);
        RegistryEntry<net.minecraft.enchantment.Enchantment> riptide = reg
            .getEntry(Enchantments.RIPTIDE.getValue()).orElse(null);
        if (riptide == null) return false;
        return EnchantmentHelper.getLevel(riptide, stack) >= 3;
    }
}
