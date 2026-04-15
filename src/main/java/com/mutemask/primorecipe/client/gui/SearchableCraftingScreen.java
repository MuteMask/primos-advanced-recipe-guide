package com.mutemask.primorecipe.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mutemask.primorecipe.client.gui.widget.SearchBarWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class SearchableCraftingScreen extends AbstractContainerScreen<CraftingMenu> {
    private static final ResourceLocation CRAFTING_TABLE_LOCATION = ResourceLocation.parse("textures/gui/container/crafting_table.png");
    
    private SearchBarWidget searchBar;
    private boolean searchActive = false;
    private String currentSearch = "";
    
    public SearchableCraftingScreen(CraftingMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        
        // Position crafting table GUI in center
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
        
        // Create search bar - same width as GUI (176 pixels), positioned above
        int searchBarWidth = this.imageWidth;
        int searchBarHeight = 16;
        int searchBarX = this.leftPos;
        int searchBarY = this.topPos - 25; // 25 pixels above the GUI
        
        this.searchBar = new SearchBarWidget(
            this.font,
            searchBarX,
            searchBarY,
            searchBarWidth,
            searchBarHeight,
            Component.literal("Search 🔍"),
            this::onSearchTextChanged
        );
        
        this.addRenderableWidget(this.searchBar);
        this.setInitialFocus(this.searchBar);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Render background
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        
        // Render crafting table GUI
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, CRAFTING_TABLE_LOCATION);
        
        int x = this.leftPos;
        int y = this.topPos;
        guiGraphics.blit(CRAFTING_TABLE_LOCATION, x, y, 0, 0, this.imageWidth, this.imageHeight);
        
        // Render search bar
        this.searchBar.render(guiGraphics, mouseX, mouseY, partialTick);
        
        // Render labels
        guiGraphics.drawString(this.font, this.title, x + 28, y + 6, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, x + 8, y + this.imageHeight - 94, 4210752, false);
        
        // Render tooltips
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // ESC is the only working keybind after typing starts
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (searchActive && !currentSearch.isEmpty()) {
                // Clear search and go back to crafting table
                currentSearch = "";
                searchBar.setValue("");
                searchActive = false;
                return true;
            }
            this.onClose();
            return true;
        }
        
        // Disable T key for search when in this screen
        if (keyCode == GLFW.GLFW_KEY_T && searchActive) {
            return true;
        }
        
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (searchActive) {
            return searchBar.charTyped(codePoint, modifiers);
        }
        return super.charTyped(codePoint, modifiers);
    }

    private void onSearchTextChanged(String text) {
        currentSearch = text;
        searchActive = !text.isEmpty();
        
        if (searchActive && text.length() >= 1) {
            // Transition to item search screen
            List<ItemStack> matchingItems = getMatchingItems(text);
            if (!matchingItems.isEmpty()) {
                minecraft.setScreen(new ItemSearchScreen(
                    this.menu,
                    this.playerInventory,
                    this.title,
                    text,
                    matchingItems,
                    this
                ));
            }
        }
    }

    private List<ItemStack> getMatchingItems(String search) {
        List<ItemStack> results = new ArrayList<>();
        // Get all craftable items from recipe manager
        var recipeManager = minecraft.level.getRecipeManager();
        var recipes = recipeManager.getRecipes();
        
        for (var recipe : recipes) {
            if (recipe.value().getType().toString().contains("crafting")) {
                ItemStack output = recipe.value().getResultItem(minecraft.level.registryAccess());
                if (!output.isEmpty()) {
                    String itemName = output.getHoverName().getString().toLowerCase();
                    if (itemName.startsWith(search.toLowerCase())) {
                        results.add(output);
                    }
                }
            }
        }
        
        return results;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Ensure search bar must be clicked manually
        if (searchBar.isMouseOver(mouseX, mouseY)) {
            searchBar.setFocused(true);
            return true;
        } else {
            searchBar.setFocused(false);
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void resize(net.minecraft.client.Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
        
        // Reposition search bar
        searchBar.setX(this.leftPos);
        searchBar.setY(this.topPos - 25);
    }

    public String getCurrentSearch() {
        return currentSearch;
    }
}
