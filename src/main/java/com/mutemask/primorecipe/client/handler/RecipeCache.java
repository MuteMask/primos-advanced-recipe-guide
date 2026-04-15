package com.mutemask.primorecipe.client.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeCache {
    private final Minecraft minecraft;
    private final Map<ItemStack, List<RecipeHolder<CraftingRecipe>>> recipeCache;
    private long lastCacheTime = 0;
    private static final long CACHE_DURATION = 60000; // 1 minute
    
    public RecipeCache() {
        this.minecraft = Minecraft.getInstance();
        this.recipeCache = new HashMap<>();
    }
    
    public List<RecipeHolder<CraftingRecipe>> getRecipesFor(ItemStack output) {
        long currentTime = System.currentTimeMillis();
        
        // Clear cache if expired
        if (currentTime - lastCacheTime > CACHE_DURATION) {
            recipeCache.clear();
            lastCacheTime = currentTime;
        }
        
        return recipeCache.computeIfAbsent(output, this::loadRecipes);
    }
    
    private List<RecipeHolder<CraftingRecipe>> loadRecipes(ItemStack output) {
        List<RecipeHolder<CraftingRecipe>> results = new ArrayList<>();
        Level level = minecraft.level;
        
        if (level == null) return results;
        
        var recipeManager = level.getRecipeManager();
        var recipes = recipeManager.getRecipes();
        
        for (var recipe : recipes) {
            if (recipe.value() instanceof CraftingRecipe craftingRecipe) {
                ItemStack recipeOutput = craftingRecipe.getResultItem(level.registryAccess());
                if (ItemStack.isSameItemSameComponents(recipeOutput, output)) {
                    results.add((RecipeHolder<CraftingRecipe>) recipe);
                }
            }
        }
        
        return results;
    }
    
    public void clearCache() {
        recipeCache.clear();
        lastCacheTime = System.currentTimeMillis();
    }
}
