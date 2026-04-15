package com.mutemask.primorecipe.client.gui;

import com.mutemask.primorecipe.client.gui.widget.CraftHereButton;
import com.mutemask.primorecipe.client.gui.widget.RecipeArrowButton;
import com.mutemask.primorecipe.client.util.InventoryHelper;
import com.mutemask.primorecipe.client.util.RecipeHelper;
import com.mutemask.primorecipe.network.OpenCraftingTablePacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
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
    private static final ResourceLocation CRAFTING_TABLE_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/container/crafting_table.png");
    private static final int SLOT_SIZE = 18;
    private static final int GRID_SIZE = 3;
    
    private final CraftingMenu menu;
    private final Inventory playerInventory;
    private final ItemStack targetItem;
    private final ItemSearchScreen parentScreen;
    
    private int leftPos;
    private int topPos;
    private int imageWidth = 176;
    private int imageHeight = 80;
    
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
        
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
        
        loadRecipes();
        
        if (recipes.size() > 1) {
            this.leftArrow = new RecipeArrowButton(
                this.leftPos + 10,
                this.topPos + 30,
                true,
                button -> cycleRecipe(-1)
            );
            
            this.rightArrow = new RecipeArrowButton(
                this.leftPos + this.imageWidth - 30,
                this.topPos + 30,
                false,
                button -> cycleRecipe(1)
            );
            
            this.addRenderableWidget(leftArrow);
            this.addRenderableWidget(rightArrow);
        }
        
        this.backButton = Button.builder(
            Component.literal("ESC to go back"),
            button -> goBack()
        ).bounds(this.leftPos + 50, this.topPos + 60, 76, 20).build();
        
        this.addRenderableWidget(backButton);
        
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
        
        isCraftable = InventoryHelper.hasAllIngredients(playerInventory, currentIngredients);
        
        updateCraftHereButton();
    }

    private void updateCraftHereButton() {
        if (craftHereButton != null) {
            this.removeWidget(craftHereButton);
        }
        
        if (isCraftable) {
            int buttonX = this.leftPos + (this.imageWidth - 70) / 2;
            int buttonY = this.topPos + this.imageHeight + 5;
            
            this.craftHereButton = new CraftHereButton(
                buttonX,
                buttonY,
                70,
                32,
                Component.literal(""),
                button -> onCraftHere()
            );
            
            this.addRenderableWidget(craftHereButton);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        
        guiGraphics.blit(CRAFTING_TABLE_LOCATION, this.leftPos, this.topPos, 29, 16, 116, 54);
        
        guiGraphics.blit(CRAFTING_TABLE_LOCATION, this.leftPos + 124, this.topPos + 18, 149, 34, 26, 26);
        
        int gridStartX = this.leftPos + 30;
        int gridStartY = this.topPos + 17;
        
        for (int i = 0; i < 9; i++) {
            int row = i / 3;
            int col = i % 3;
            int x = gridStartX + col * (SLOT_SIZE + 4);
            int y = gridStartY + row * (SLOT_SIZE + 4);
            
            ItemStack ingredient = currentIngredients.get(i);
            
            boolean hasIngredient = ingredient.isEmpty() || 
                InventoryHelper.hasIngredient(playerInventory, ingredient);
            
            if (!ingredient.isEmpty()) {
                if (!hasIngredient) {
                    guiGraphics.fill(x - 1, y - 1, x + SLOT_SIZE + 1, y + SLOT_SIZE + 1, 0x66FF0000);
                }
                
                guiGraphics.renderItem(ingredient, x, y);
                guiGraphics.renderItemDecorations(this.font, ingredient, x, y);
            }
        }
        
        guiGraphics.renderItem(targetItem, this.leftPos + 131, this.topPos + 25);
        guiGraphics.renderItemDecorations(this.font, targetItem, this.leftPos + 131, this.topPos + 25);
        
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, this.topPos - 15, 0xFFFFFF);
        
        if (recipes.size() > 1) {
            String counter = (currentRecipeIndex + 1) + "/" + recipes.size();
            guiGraphics.drawCenteredString(this.font, counter, this.width / 2, this.topPos - 5, 0xAAAAAA);
        }
        
        renderStatusMessages(guiGraphics);
        
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        renderIngredientTooltips(guiGraphics, mouseX, mouseY, gridStartX, gridStartY);
    }

    private void renderStatusMessages(GuiGraphics guiGraphics) {
        int messageY = this.topPos + 58;
        
        if (isCraftable) {
            Component message = Component.literal("✅ You have all the required materials! Ready to craft.")
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x00FF00)));
            guiGraphics.drawCenteredString(this.font, message, this.width / 2, messageY, 0x00FF00);
        } else {
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
        
        List<ItemStack> ingredients = new ArrayList<>();
        for (ItemStack stack : currentIngredients) {
            ingredients.add(stack.copy());
        }
        
        ClientPlayNetworking.send(new OpenCraftingTablePacket(ingredients));
        
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
