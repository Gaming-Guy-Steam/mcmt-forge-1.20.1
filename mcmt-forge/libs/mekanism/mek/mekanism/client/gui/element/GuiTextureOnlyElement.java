package mekanism.client.gui.element;

import mekanism.client.gui.IGuiWrapper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class GuiTextureOnlyElement extends GuiTexturedElement {
   private final int textureWidth;
   private final int textureHeight;

   public GuiTextureOnlyElement(ResourceLocation resource, IGuiWrapper gui, int x, int y, int width, int height) {
      this(resource, gui, x, y, width, height, width, height);
   }

   public GuiTextureOnlyElement(ResourceLocation resource, IGuiWrapper gui, int x, int y, int width, int height, int textureWidth, int textureHeight) {
      super(resource, gui, x, y, width, height);
      this.textureWidth = textureWidth;
      this.textureHeight = textureHeight;
      this.f_93623_ = false;
   }

   @Override
   public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
      guiGraphics.m_280163_(this.getResource(), this.relativeX, this.relativeY, 0.0F, 0.0F, this.f_93618_, this.f_93619_, this.textureWidth, this.textureHeight);
   }
}
