package com.mutemask.primorecipe.mixin;

import com.mutemask.primorecipe.client.gui.SearchableCraftingScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingScreen.class)
public class CraftingScreenMixin {
    
    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void onInit(CallbackInfo ci) {
        CraftingScreen screen = (CraftingScreen) (Object) this;
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        
        if (player == null) return;
        
        // Check if we should use custom screen
        if (minecraft.gameMode.getPlayerMode() == GameType.SURVIVAL) {
            if (!player.getRecipeBook().isOpen(screen.getMenu())) {
                // Cancel vanilla init and switch to our screen
                ci.cancel();
                
                // Schedule screen switch for next tick to avoid recursion
                minecraft.execute(() -> {
                    minecraft.setScreen(new SearchableCraftingScreen(
                        screen.getMenu(),
                        player.getInventory(),
                        screen.getTitle()
                    ));
                });
            }
        }
    }
}
