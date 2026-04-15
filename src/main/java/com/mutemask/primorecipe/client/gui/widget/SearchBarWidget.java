package com.mutemask.primorecipe.client.gui.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public class SearchBarWidget extends EditBox {
    private static final ResourceLocation SEARCH_BAR_TEXTURE = ResourceLocation.parse("textures/gui/container/creative_inventory/tab_item_search.png");
    private static final int TEXT_COLOR = 0xFFFFFF;
    private static final int PLACEHOLDER_COLOR = 0xAAAAAA;
    
    private final Consumer<String> onTextChanged;
    private final Component placeholder;
    private boolean isExpanded = false;

    public SearchBarWidget(Font font, int x, int y, int width, int height, 
                          Component placeholder, Consumer<String> onTextChanged) {
        super(font, x, y, width, height, placeholder);
        this.placeholder = placeholder;
        this.onTextChanged = onTextChanged;
        
        this.setMaxLength(50);
        this.setTextColor(TEXT_COLOR);
        this.setTextColorUneditable(PLACEHOLDER_COLOR);
        this.setBordered(false);
        this.setValue("");
        this.setFocused(false);
        this.isEditable = true;
        this.setCanLoseFocus(true);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!this.visible) return;
        
        // Render search bar background (vanilla style)
        RenderType renderType = RenderType.guiTextured(SEARCH_BAR_TEXTURE);
        
        // Draw the search bar background stretched to our width
        guiGraphics.blit(renderType, SEARCH_BAR_TEXTURE, this.getX(), this.getY(), 
                        0, 0, this.width, this.height, this.width, this.height);
        
        // Render the text or placeholder
        String text = this.getValue();
        if (text.isEmpty() && !this.isFocused()) {
            // Render placeholder
            guiGraphics.drawString(this.getFont(), this.placeholder, 
                                 this.getX() + 5, this.getY() + (this.height - 8) / 2, 
                                 PLACEHOLDER_COLOR, false);
        } else {
            // Render actual text with cursor
            int textX = this.getX() + 5;
            int textY = this.getY() + (this.height - 8) / 2;
            
            String displayText = text;
            if (this.getFont().width(text) > this.width - 10) {
                displayText = this.getFont().plainSubstrByWidth(text, this.width - 10, true);
            }
            
            guiGraphics.drawString(this.getFont(), displayText, textX, textY, TEXT_COLOR, false);
            
            // Render cursor if focused
            if (this.isFocused()) {
                int cursorPos = this.getCursorPosition();
                if (cursorPos <= displayText.length()) {
                    int cursorX = textX + this.getFont().width(displayText.substring(0, cursorPos));
                    guiGraphics.fill(cursorX, textY - 1, cursorX + 1, textY + 9, 0xFFD0D0D0);
                }
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Call the parent to handle text input
        boolean result = super.keyPressed(keyCode, scanCode, modifiers);
        
        // Notify listener of text change
        if (onTextChanged != null) {
            onTextChanged.accept(this.getValue());
        }
        
        return result;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        boolean result = super.charTyped(codePoint, modifiers);
        
        if (result && onTextChanged != null) {
            onTextChanged.accept(this.getValue());
        }
        
        return result;
    }

    @Override
    public void setValue(String text) {
        super.setValue(text);
        if (onTextChanged != null) {
            onTextChanged.accept(text);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean wasFocused = this.isFocused();
        boolean result = super.mouseClicked(mouseX, mouseY, button);
        
        // Must be clicked manually to focus
        if (!wasFocused && this.isFocused()) {
            this.setCursorPosition(this.getValue().length());
        }
        
        return result;
    }

    public void setX(int x) {
        this.setPosition(x, this.getY());
    }

    public void setY(int y) {
        this.setPosition(this.getX(), y);
    }
}
