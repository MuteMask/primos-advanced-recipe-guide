package com.mutemask.primorecipe.client.gui.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public class ItemResultWidget extends AbstractWidget {
    private final ItemStack itemStack;
    private final Consumer<ItemStack> onClick;

    public ItemResultWidget(int x, int y, int size, ItemStack itemStack, Consumer<ItemStack> onClick) {
        super(x, y, size, size, Component.empty());
        this.itemStack = itemStack;
        this.onClick = onClick;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!this.visible) return;
        
        // Render item
        guiGraphics.renderItem(this.itemStack, this.getX(), this.getY());
        guiGraphics.renderItemDecorations(guiGraphics.font, this.itemStack, this.getX(), this.getY());
        
        // Highlight on hover
        if (this.isHovered) {
            guiGraphics.fill(this.getX(), this.getY(), 
                           this.getX() + this.width, this.getY() + this.height, 
                           0x80FFFFFF);
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (this.onClick != null) {
            this.onClick.accept(this.itemStack);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, 
            Component.literal("Item: " + this.itemStack.getHoverName().getString()));
    }

    public ItemStack getItemStack() {
        return this.itemStack;
    }
}
