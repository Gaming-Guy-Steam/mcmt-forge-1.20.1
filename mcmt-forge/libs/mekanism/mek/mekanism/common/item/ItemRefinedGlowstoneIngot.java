package mekanism.common.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import org.jetbrains.annotations.NotNull;

public class ItemRefinedGlowstoneIngot extends Item {
   public ItemRefinedGlowstoneIngot(Properties properties) {
      super(properties);
   }

   public boolean isPiglinCurrency(@NotNull ItemStack stack) {
      return true;
   }
}
