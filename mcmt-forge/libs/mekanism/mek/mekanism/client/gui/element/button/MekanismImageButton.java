package mekanism.client.gui.element.button;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MekanismImageButton extends MekanismButton {
   private final ResourceLocation resourceLocation;
   private final int textureWidth;
   private final int textureHeight;

   public MekanismImageButton(IGuiWrapper gui, int x, int y, int size, ResourceLocation resource, @NotNull Runnable onPress) {
      this(gui, x, y, size, size, resource, onPress);
   }

   public MekanismImageButton(
      IGuiWrapper gui, int x, int y, int size, ResourceLocation resource, @NotNull Runnable onPress, @Nullable GuiElement.IHoverable onHover
   ) {
      this(gui, x, y, size, size, resource, onPress, onHover);
   }

   public MekanismImageButton(IGuiWrapper gui, int x, int y, int size, int textureSize, ResourceLocation resource, @NotNull Runnable onPress) {
      this(gui, x, y, size, textureSize, resource, onPress, null);
   }

   public MekanismImageButton(
      IGuiWrapper gui, int x, int y, int size, int textureSize, ResourceLocation resource, @NotNull Runnable onPress, @Nullable GuiElement.IHoverable onHover
   ) {
      this(gui, x, y, size, size, textureSize, textureSize, resource, onPress, onHover);
   }

   public MekanismImageButton(
      IGuiWrapper gui, int x, int y, int width, int height, int textureWidth, int textureHeight, ResourceLocation resource, @NotNull Runnable onPress
   ) {
      this(gui, x, y, width, height, textureWidth, textureHeight, resource, onPress, null);
   }

   public MekanismImageButton(
      IGuiWrapper gui,
      int x,
      int y,
      int width,
      int height,
      int textureWidth,
      int textureHeight,
      ResourceLocation resource,
      @NotNull Runnable onPress,
      @Nullable GuiElement.IHoverable onHover
   ) {
      super(gui, x, y, width, height, Component.m_237119_(), onPress, onHover);
      this.resourceLocation = resource;
      this.textureWidth = textureWidth;
      this.textureHeight = textureHeight;
   }

   @Override
   public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
      guiGraphics.m_280411_(
         this.getResource(),
         this.getButtonX(),
         this.getButtonY(),
         this.getButtonWidth(),
         this.getButtonHeight(),
         0.0F,
         0.0F,
         this.textureWidth,
         this.textureHeight,
         this.textureWidth,
         this.textureHeight
      );
   }

   protected ResourceLocation getResource() {
      return this.resourceLocation;
   }
}
