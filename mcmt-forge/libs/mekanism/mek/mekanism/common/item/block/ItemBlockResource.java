package mekanism.common.item.block;

import mekanism.common.block.basic.BlockResource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Nullable;

public class ItemBlockResource extends ItemBlockMekanism<BlockResource> {
   public ItemBlockResource(BlockResource block, Properties properties) {
      super(block, properties);
   }

   public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
      return this.m_40614_().getResourceInfo().getBurnTime();
   }
}
