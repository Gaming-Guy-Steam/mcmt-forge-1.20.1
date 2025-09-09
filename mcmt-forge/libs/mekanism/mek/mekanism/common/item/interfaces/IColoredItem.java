package mekanism.common.item.interfaces;

import mekanism.api.text.EnumColor;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface IColoredItem {
   @Nullable
   default EnumColor getColor(ItemStack stack) {
      return ItemDataUtils.hasData(stack, "color", 3) ? EnumColor.byIndexStatic(ItemDataUtils.getInt(stack, "color")) : null;
   }

   default void setColor(ItemStack stack, @Nullable EnumColor color) {
      if (color == null) {
         ItemDataUtils.removeData(stack, "color");
      } else {
         ItemDataUtils.setInt(stack, "color", color.ordinal());
      }
   }
}
