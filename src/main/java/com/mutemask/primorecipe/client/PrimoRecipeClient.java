package com.mutemask.primorecipe.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.level.GameType;
import com.mojang.blaze3d.platform.InputConstants;
import com.mutemask.primorecipe.client.gui.SearchableCraftingScreen;
import com.mutemask.primorecipe.client.handler.CraftingScreenHandler;
import com.mutemask.primorecipe.client.handler.RecipeCache;

public class PrimoRecipeClient implements ClientModInitializer {
    public static final Minecraft minecraft = Minecraft.getInstance();
    private static CraftingScreenHandler screenHandler;
    private static RecipeCache recipeCache;

    @Override
    public void onInitializeClient() {
        PrimoRecipeMod.LOGGER.info("Initializing Primo Recipe Client");
        
        screenHandler = new CraftingScreenHandler();
        recipeCache = new RecipeCache();
        
        // Register tick event to handle screen replacement
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.screen instanceof CraftingScreen craftingScreen) {
                Player player = client.player;
                if (player != null && client.gameMode.getPlayerMode() == GameType.SURVIVAL) {
                    // Check if vanilla recipe book is disabled
                    if (!player.getRecipeBook().isOpen(craftingScreen.getMenu())) {
                        // Replace with our custom screen
                        client.setScreen(new SearchableCraftingScreen(
                            craftingScreen.getMenu(),
                            player.getInventory(),
                            craftingScreen.getTitle()
                        ));
                    }
                }
            }
        });
    }

    public static CraftingScreenHandler getScreenHandler() {
        return screenHandler;
    }

    public static RecipeCache getRecipeCache() {
        return recipeCache;
    }
}
