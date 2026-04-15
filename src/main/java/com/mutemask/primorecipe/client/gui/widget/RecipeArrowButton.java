package com.mutemask.primorecipe.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class RecipeArrowButton extends Button {
    private static final ResourceLocation RECIPE_BOOK_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/recipe_book.png");
    private final boolean isLeft;
    
    public RecipeArrowButton(int x, int y, boolean isLeft, OnPress onPress) {
        super(x, y, 20, 20, Component.empty(), onPress, DEFAULT_NARRATION);
        this.isLeft = isLeft;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        
        int u = isLeft ? 0 : 20;
        int v = this.isHovered ? 20 : 0;
        
        guiGraphics.blit(RECIPE_BOOK_LOCATION, this.getX(), this.getY(), u, v, 20, 20);
        
        if (this.isHovered) {
            guiGraphics.fill(this.getX(), this.getY(), 
                           this.getX() + 20, this.getY() + 20, 
                           0x40FFFFFF);
        }
    }
}
