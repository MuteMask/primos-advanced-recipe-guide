package com.mutemask.primorecipe.client.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.level.GameType;

public class CraftingScreenHandler {
    private final Minecraft minecraft;
    
    public CraftingScreenHandler() {
        this.minecraft = Minecraft.getInstance();
    }
    
    public boolean shouldShowCustomSearch(CraftingScreen screen) {
        Player player = minecraft.player;
        if (player == null) return false;
        
        // Only show in survival mode
        if (minecraft.gameMode.getPlayerMode() != GameType.SURVIVAL) {
            return false;
        }
        
        // Only show if vanilla recipe book is disabled
        CraftingMenu menu = screen.getMenu();
        return !player.getRecipeBook().isOpen(menu);
    }
    
    public void openSearchableCrafting(CraftingMenu menu) {
        // This is handled by the ClientTickEvents in PrimoRecipeClient
    }
}
