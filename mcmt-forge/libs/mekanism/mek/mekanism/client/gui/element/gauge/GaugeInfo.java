package mekanism.client.gui.element.gauge;

import mekanism.api.text.EnumColor;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.util.MekanismUtils;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public enum GaugeInfo {
   STANDARD("normal.png", 2, 2, null),
   BLUE("blue.png", 2, 2, EnumColor.DARK_BLUE),
   RED("red.png", 2, 2, EnumColor.DARK_RED),
   YELLOW("yellow.png", 2, 2, EnumColor.YELLOW),
   ORANGE("orange.png", 2, 2, EnumColor.ORANGE),
   AQUA("aqua.png", 2, 2, EnumColor.AQUA);

   @Nullable
   private final EnumColor color;
   private final int sideWidth;
   private final int sideHeight;
   private final ResourceLocation resourceLocation;

   private GaugeInfo(String texture, int sideWidth, int sideHeight, @Nullable EnumColor color) {
      this.resourceLocation = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_GAUGE, texture);
      this.sideWidth = sideWidth;
      this.sideHeight = sideHeight;
      this.color = color;
   }

   @Nullable
   public EnumColor getColor() {
      return this.color;
   }

   public int getSideWidth() {
      return this.sideWidth;
   }

   public int getSideHeight() {
      return this.sideHeight;
   }

   public ResourceLocation getResourceLocation() {
      return this.resourceLocation;
   }

   public static GaugeInfo get(DataType type) {
      return switch (type) {
         case OUTPUT, OUTPUT_1 -> BLUE;
         case INPUT, INPUT_1 -> RED;
         case OUTPUT_2 -> AQUA;
         case INPUT_2 -> ORANGE;
         default -> STANDARD;
      };
   }
}
