package com.mutemask.primorecipe.client.gui;

import com.mutemask.primorecipe.client.gui.widget.ItemResultWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ItemSearchScreen extends Screen {
    private static final ResourceLocation RECIPE_BOOK_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/recipe_book.png");
    private static final int GRID_COLUMNS = 5;
    private static final int GRID_ROWS = 4;
    private static final int SLOT_SIZE = 25;
    private static final int GRID_WIDTH = GRID_COLUMNS * SLOT_SIZE;
    private static final int GRID_HEIGHT = GRID_ROWS * SLOT_SIZE;
    
    private final CraftingMenu menu;
    private final Inventory playerInventory;
    private final String searchQuery;
    private final List<ItemStack> matchingItems;
    private final SearchableCraftingScreen parentScreen;
    
    private int leftPos;
    private int topPos;
    private int scrollOffset = 0;
    private List<ItemResultWidget> itemWidgets = new ArrayList<>();
    private Button backButton;

    protected ItemSearchScreen(CraftingMenu menu, Inventory playerInventory, Component title, 
                              String searchQuery, List<ItemStack> matchingItems,
                              SearchableCraftingScreen parentScreen) {
        super(Component.literal("Search Results: " + searchQuery));
        this.menu = menu;
        this.playerInventory = playerInventory;
        this.searchQuery = searchQuery;
        this.matchingItems = matchingItems;
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        super.init();
        
        this.leftPos = (this.width - GRID_WIDTH) / 2;
        this.topPos = (this.height - GRID_HEIGHT) / 2;
        
        updateItemWidgets();
        
        this.backButton = Button.builder(
            Component.literal("ESC to go back"),
            button -> goBack()
        ).bounds(this.width / 2 - 50, this.topPos + GRID_HEIGHT + 10, 100, 20).build();
        
        this.addRenderableWidget(backButton);
    }

    private void updateItemWidgets() {
        this.itemWidgets.clear();
        
        int visibleItems = GRID_COLUMNS * GRID_ROWS;
        int startIndex = scrollOffset * GRID_COLUMNS;
        
        for (int i = 0; i < visibleItems && (startIndex + i) < matchingItems.size(); i++) {
            int row = i / GRID_COLUMNS;
            int col = i % GRID_COLUMNS;
            
            int x = this.leftPos + col * SLOT_SIZE + 4;
            int y = this.topPos + row * SLOT_SIZE + 4;
            
            ItemStack item = matchingItems.get(startIndex + i);
            ItemResultWidget widget = new ItemResultWidget(
                x, y, SLOT_SIZE - 2, item,
                this::onItemSelected
            );
            
            this.itemWidgets.add(widget);
            this.addRenderableWidget(widget);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLUMNS; col++) {
                int x = this.leftPos + col * SLOT_SIZE;
                int y = this.topPos + row * SLOT_SIZE;
                guiGraphics.blit(RECIPE_BOOK_LOCATION, x, y, 0, 0, 24, 24, 256, 256);
            }
        }
        
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, this.topPos - 20, 0xFFFFFF);
        
        String countText = matchingItems.size() + " items found";
        guiGraphics.drawCenteredString(this.font, countText, this.width / 2, this.topPos - 10, 0xAAAAAA);
        
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        for (ItemResultWidget widget : itemWidgets) {
            if (widget.isMouseOver(mouseX, mouseY)) {
                guiGraphics.renderTooltip(this.font, widget.getItemStack(), mouseX, mouseY);
                break;
            }
        }
    }

    private void onItemSelected(ItemStack item) {
        minecraft.setScreen(new RecipeViewScreen(
            menu,
            playerInventory,
            item,
            this
        ));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            goBack();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void goBack() {
        minecraft.setScreen(parentScreen);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int maxScroll = Math.max(0, (matchingItems.size() - 1) / GRID_COLUMNS - GRID_ROWS + 1);
        
        if (verticalAmount > 0 && scrollOffset > 0) {
            scrollOffset--;
            updateItemWidgets();
            return true;
        } else if (verticalAmount < 0 && scrollOffset < maxScroll) {
            scrollOffset++;
            updateItemWidgets();
            return true;
        }
        
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public String getSearchQuery() {
        return searchQuery;
    }
}
