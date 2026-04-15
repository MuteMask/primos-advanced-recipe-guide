package com.mutemask.primorecipe.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mutemask.primorecipe.client.gui.widget.CraftHereButton;
import com.mutemask.primorecipe.client.gui.widget.RecipeArrowButton;
import com.mutemask.primorecipe.client.util.InventoryHelper;
import com.mutemask.primorecipe.client.util.RecipeHelper;
import com.mutemask.primorecipe.network.OpenCraftingTablePacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.core.NonNullList;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class RecipeViewScreen extends Screen {
    private static final ResourceLocation CRAFTING_TABLE_LOCATION = ResourceLocation.parse("textures/gui/container/crafting_table.png");
    private static final int SLOT_SIZE = 18;
    private static final int GRID_SIZE = 3;
    
    private final CraftingMenu menu;
    private final Inventory playerInventory;
    private final ItemStack targetItem;
    private final ItemSearchScreen parentScreen;
    
    private int leftPos;
    private int topPos;
    private int imageWidth = 176;
    private int imageHeight = 80; // Compact height for just the grid
    
    private List<RecipeHolder<CraftingRecipe>> recipes = new ArrayList<>();
    private int currentRecipeIndex = 0;
    private NonNullList<ItemStack> currentIngredients = NonNullList.withSize(9, ItemStack.EMPTY);
    
    private RecipeArrowButton leftArrow;
    private RecipeArrowButton rightArrow;
    private CraftHereButton craftHereButton;
    private Button backButton;
    
    private boolean isCraftable = false;

    protected RecipeViewScreen(CraftingMenu menu, Inventory playerInventory, ItemStack targetItem, 
                               ItemSearchScreen parentScreen) {
        super(Component.literal("Recipe: " + targetItem.getHoverName().getString()));
        this.menu = menu;
        this.playerInventory = playerInventory;
        this.targetItem = targetItem;
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        super.init();
        
        // Center the 3x3 grid
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
        
        // Load recipes for this item
        loadRecipes();
        
        // Navigation arrows (only if multiple recipes)
        if (recipes.size() > 1) {
            this.leftArrow = new RecipeArrowButton(
                this.leftPos + 10,
                this.topPos + 30,
                true, // left arrow
                button -> cycleRecipe(-1)
            );
            
            this.rightArrow = new RecipeArrowButton(
                this.leftPos + this.imageWidth - 30,
                this.topPos + 30,
                false, // right arrow
                button -> cycleRecipe(1)
            );
            
            this.addRenderableWidget(leftArrow);
            this.addRenderableWidget(rightArrow);
        }
        
        // Back button (inside GUI)
        this.backButton = Button.builder(
            Component.literal("ESC to go back"),
            button -> goBack()
        ).bounds(this.leftPos + 50, this.topPos + 60, 76, 20).build();
        
        this.addRenderableWidget(backButton);
        
        // Craft Here button (outside GUI, below it)
        updateCraftHereButton();
        
        updateRecipeDisplay();
    }

    private void loadRecipes() {
        recipes = RecipeHelper.getRecipesFor(targetItem, minecraft.level);
        if (!recipes.isEmpty()) {
            currentRecipeIndex = 0;
        }
    }

    private void updateRecipeDisplay() {
        if (recipes.isEmpty()) return;
        
        CraftingRecipe recipe = recipes.get(currentRecipeIndex).value();
        currentIngredients = RecipeHelper.getIngredientsAsGrid(recipe, minecraft.level.registryAccess());
        
        // Check if player has all ingredients
        isCraftable = InventoryHelper.hasAllIngredients(playerInventory, currentIngredients);
        
        // Update craft here button visibility
        updateCraftHereButton();
    }

    private void updateCraftHereButton() {
        // Remove old button if exists
        if (craftHereButton != null) {
            this.removeWidget(craftHereButton);
        }
        
        // Only show if craftable
        if (isCraftable) {
            // Position below the GUI (outside it)
            int buttonX = this.leftPos + (this.imageWidth - 70) / 2;
            int buttonY = this.topPos + this.imageHeight + 5;
            
            this.craftHereButton = new CraftHereButton(
                buttonX,
                buttonY,
                70,
                32, // 2 slots height
                Component.literal(""),
                button -> onCraftHere()
            );
            
            this.addRenderableWidget(craftHereButton);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Render background
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        
        // Render 3x3 crafting grid (centered)
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, CRAFTING_TABLE_LOCATION);
        
        // Draw only the crafting grid area
        guiGraphics.blit(CRAFTING_TABLE_LOCATION, this.leftPos, this.topPos, 29, 16, 116, 54);
        
        // Draw result slot
        guiGraphics.blit(CRAFTING_TABLE_LOCATION, this.leftPos + 124, this.topPos + 18, 149, 34, 26, 26);
        
        // Render ingredients in the 3x3 grid
        int gridStartX = this.leftPos + 30;
        int gridStartY = this.topPos + 17;
        
        for (int i = 0; i < 9; i++) {
            int row = i / 3;
            int col = i % 3;
            int x = gridStartX + col * (SLOT_SIZE + 4);
            int y = gridStartY + row * (SLOT_SIZE + 4);
            
            ItemStack ingredient = currentIngredients.get(i);
            
            // Check if player has this ingredient
            boolean hasIngredient = ingredient.isEmpty() || 
                InventoryHelper.hasIngredient(playerInventory, ingredient);
            
            // Draw slot background
            if (!ingredient.isEmpty()) {
                if (!hasIngredient) {
                    // Red background for missing items (like vanilla)
                    guiGraphics.fill(x - 1, y - 1, x + SLOT_SIZE + 1, y + SLOT_SIZE + 1, 0x66FF0000);
                }
                
                guiGraphics.renderItem(ingredient, x, y);
                guiGraphics.renderItemDecorations(this.font, ingredient, x, y);
            }
        }
        
        // Render result item
        guiGraphics.renderItem(targetItem, this.leftPos + 131, this.topPos + 25);
        guiGraphics.renderItemDecorations(this.font, targetItem, this.leftPos + 131, this.topPos + 25);
        
        // Render title
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, this.topPos - 15, 0xFFFFFF);
        
        // Render recipe counter if multiple recipes
        if (recipes.size() > 1) {
            String counter = (currentRecipeIndex + 1) + "/" + recipes.size();
            guiGraphics.drawCenteredString(this.font, counter, this.width / 2, this.topPos - 5, 0xAAAAAA);
        }
        
        // Render status messages (inside GUI, below the grid)
        renderStatusMessages(guiGraphics);
        
        // Render widgets (arrows, buttons)
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        // Render tooltips for ingredients
        renderIngredientTooltips(guiGraphics, mouseX, mouseY, gridStartX, gridStartY);
    }

    private void renderStatusMessages(GuiGraphics guiGraphics) {
        int messageY = this.topPos + 58; // Below the crafting grid
        
        if (isCraftable) {
            // ✅ You have all the required materials! Ready to craft. (Pure Green)
            Component message = Component.literal("✅ You have all the required materials! Ready to craft.")
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x00FF00)));
            guiGraphics.drawCenteredString(this.font, message, this.width / 2, messageY, 0x00FF00);
        } else {
            // 🔍 You are missing some materials (Pure Yellow)
            Component message = Component.literal("🔍 You are missing some materials")
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFFF00)));
            guiGraphics.drawCenteredString(this.font, message, this.width / 2, messageY, 0xFFFF00);
        }
    }

    private void renderIngredientTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY, 
                                          int gridStartX, int gridStartY) {
        for (int i = 0; i < 9; i++) {
            int row = i / 3;
            int col = i % 3;
            int x = gridStartX + col * (SLOT_SIZE + 4);
            int y = gridStartY + row * (SLOT_SIZE + 4);
            
            if (mouseX >= x && mouseX < x + SLOT_SIZE && 
                mouseY >= y && mouseY < y + SLOT_SIZE) {
                ItemStack ingredient = currentIngredients.get(i);
                if (!ingredient.isEmpty()) {
                    guiGraphics.renderTooltip(this.font, ingredient, mouseX, mouseY);
                }
                break;
            }
        }
        
        // Result item tooltip
        int resultX = this.leftPos + 131;
        int resultY = this.topPos + 25;
        if (mouseX >= resultX && mouseX < resultX + SLOT_SIZE && 
            mouseY >= resultY && mouseY < resultY + SLOT_SIZE) {
            guiGraphics.renderTooltip(this.font, targetItem, mouseX, mouseY);
        }
    }

    private void cycleRecipe(int direction) {
        if (recipes.isEmpty()) return;
        
        currentRecipeIndex += direction;
        if (currentRecipeIndex < 0) {
            currentRecipeIndex = recipes.size() - 1;
        } else if (currentRecipeIndex >= recipes.size()) {
            currentRecipeIndex = 0;
        }
        
        updateRecipeDisplay();
    }

    private void onCraftHere() {
        if (!isCraftable) return;
        
        // Send packet to server to open crafting table with pre-filled recipe
        List<ItemStack> ingredients = new ArrayList<>();
        for (ItemStack stack : currentIngredients) {
            ingredients.add(stack.copy());
        }
        
        ClientPlayNetworking.send(new OpenCraftingTablePacket(ingredients));
        
        // Close this screen and open vanilla crafting screen
        minecraft.setScreen(null);
    }

    private void goBack() {
        minecraft.setScreen(parentScreen);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            goBack();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
