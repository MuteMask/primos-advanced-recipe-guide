package com.mutemask.primorecipe.client.util;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class RecipeHelper {
    
    public static List<RecipeHolder<CraftingRecipe>> getRecipesFor(ItemStack output, Level level) {
        List<RecipeHolder<CraftingRecipe>> results = new ArrayList<>();
        
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
    
    public static NonNullList<ItemStack> getIngredientsAsGrid(CraftingRecipe recipe, RegistryAccess registryAccess) {
        NonNullList<ItemStack> grid = NonNullList.withSize(9, ItemStack.EMPTY);
        NonNullList<Ingredient> ingredients = recipe.getIngredients();
        
        // Handle shaped recipes
        if (recipe.isShapeless()) {
            // Shapeless: fill from top-left
            for (int i = 0; i < ingredients.size() && i < 9; i++) {
                grid.set(i, getFirstMatchingItem(ingredients.get(i)));
            }
        } else {
            // Shaped: try to determine pattern
            // For simplicity, place in 3x3 grid
            int width = 3;
            int height = 3;
            
            if (recipe instanceof net.minecraft.world.item.crafting.ShapedRecipe shaped) {
                width = shaped.getWidth();
                height = shaped.getHeight();
            }
            
            for (int i = 0; i < ingredients.size() && i < width * height; i++) {
                int row = i / width;
                int col = i % width;
                int gridIndex = row * 3 + col;
                
                if (gridIndex < 9) {
                    grid.set(gridIndex, getFirstMatchingItem(ingredients.get(i)));
                }
            }
        }
        
        return grid;
    }
    
    private static ItemStack getFirstMatchingItem(Ingredient ingredient) {
        if (ingredient.isEmpty()) return ItemStack.EMPTY;
        
        ItemStack[] matching = ingredient.getItems();
        if (matching.length > 0) {
            return matching[0].copyWithCount(1);
        }
        
        return ItemStack.EMPTY;
    }
}
