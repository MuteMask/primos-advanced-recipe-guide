package com.mutemask.primorecipe.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class CraftHereButton extends Button {
    private static final ResourceLocation BUTTON_TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/crafting_table.png");
    
    public CraftHereButton(int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        
        int v = this.isHovered ? 32 : 0;
        
        guiGraphics.blit(BUTTON_TEXTURE, this.getX(), this.getY(), 0, 166, this.width, 16);
        guiGraphics.blit(BUTTON_TEXTURE, this.getX(), this.getY() + 16, 0, 166, this.width, 16);
        
        int borderColor = this.isHovered ? 0xFF00FF00 : 0xFFFFFFFF;
        guiGraphics.renderOutline(this.getX(), this.getY(), this.width, this.height, borderColor);
        
        String text = "Craft Here";
        int textWidth = guiGraphics.font.width(text);
        int textX = this.getX() + (this.width - textWidth) / 2;
        int textY = this.getY() + (this.height - 8) / 2;
        
        int textColor = this.active ? (this.isHovered ? 0xFFFFAA : 0xFFFFFF) : 0xAAAAAA;
        guiGraphics.drawString(guiGraphics.font, text, textX, textY, textColor, false);
        
        RenderSystem.disableBlend();
    }
}
