package com.mutemask.primorecipe.client.gui;

import com.mutemask.primorecipe.client.gui.widget.SearchBarWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class SearchableCraftingScreen extends AbstractContainerScreen<CraftingMenu> {
    private static final ResourceLocation CRAFTING_TABLE_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/container/crafting_table.png");
    
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
        
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
        
        int searchBarWidth = this.imageWidth;
        int searchBarHeight = 16;
        int searchBarX = this.leftPos;
        int searchBarY = this.topPos - 25;
        
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
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        
        guiGraphics.blit(CRAFTING_TABLE_LOCATION, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        
        this.searchBar.render(guiGraphics, mouseX, mouseY, partialTick);
        
        guiGraphics.drawString(this.font, this.title, this.leftPos + 28, this.topPos + 6, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.leftPos + 8, this.topPos + this.imageHeight - 94, 4210752, false);
        
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (searchActive && !currentSearch.isEmpty()) {
                currentSearch = "";
                searchBar.setValue("");
                searchActive = false;
                return true;
            }
            this.onClose();
            return true;
        }
        
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
        
        searchBar.setX(this.leftPos);
        searchBar.setY(this.topPos - 25);
    }

    public String getCurrentSearch() {
        return currentSearch;
    }
}
