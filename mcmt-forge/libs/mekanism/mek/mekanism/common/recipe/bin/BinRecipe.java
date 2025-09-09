package mekanism.common.recipe.bin;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.inventory.BinMekanismInventory;
import mekanism.common.inventory.slot.BinInventorySlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;

@NothingNullByDefault
public abstract class BinRecipe extends CustomRecipe {
   protected BinRecipe(ResourceLocation id, CraftingBookCategory category) {
      super(id, category);
   }

   protected static BinInventorySlot convertToSlot(ItemStack binStack) {
      return BinMekanismInventory.create(binStack).getBinSlot();
   }

   public boolean m_142505_() {
      return false;
   }
}
