package mekanism.client.jei;

import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.gui.GuiGraphics;

public record NOOPDrawable(int width, int height) implements IDrawable {
   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }

   public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset) {
   }
}
