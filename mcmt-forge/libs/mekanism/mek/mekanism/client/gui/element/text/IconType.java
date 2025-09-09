package mekanism.client.gui.element.text;

import mekanism.common.util.MekanismUtils;
import net.minecraft.resources.ResourceLocation;

public enum IconType {
   DIGITAL(MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "digital_text_input.png"), 4, 7);

   private final ResourceLocation icon;
   private final int xSize;
   private final int ySize;

   private IconType(ResourceLocation icon, int xSize, int ySize) {
      this.icon = icon;
      this.xSize = xSize;
      this.ySize = ySize;
   }

   public ResourceLocation getIcon() {
      return this.icon;
   }

   public int getWidth() {
      return this.xSize;
   }

   public int getHeight() {
      return this.ySize;
   }

   public int getOffsetX() {
      return this.xSize + 4;
   }
}
