package com.mutemask.primorecipe.client.util;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class InventoryHelper {
    
    public static boolean hasAllIngredients(Inventory inventory, NonNullList<ItemStack> ingredients) {
        for (ItemStack ingredient : ingredients) {
            if (!ingredient.isEmpty() && !hasIngredient(inventory, ingredient)) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean hasIngredient(Inventory inventory, ItemStack required) {
        int requiredCount = required.getCount();
        int foundCount = 0;
        
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (ItemStack.isSameItemSameComponents(stack, required)) {
                foundCount += stack.getCount();
                if (foundCount >= requiredCount) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public static int countIngredient(Inventory inventory, ItemStack ingredient) {
        int count = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (ItemStack.isSameItemSameComponents(stack, ingredient)) {
                count += stack.getCount();
            }
        }
        return count;
    }
}
