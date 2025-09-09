package mekanism.common.recipe.bin;

import mekanism.api.Action;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.inventory.slot.BinInventorySlot;
import mekanism.common.item.block.ItemBlockBin;
import mekanism.common.registries.MekanismRecipeSerializers;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

@NothingNullByDefault
public class BinExtractRecipe extends BinRecipe {
   public BinExtractRecipe(ResourceLocation id, CraftingBookCategory category) {
      super(id, category);
   }

   public boolean matches(CraftingContainer inv, Level world) {
      ItemStack binStack = this.findBinStack(inv);
      return binStack.m_41619_() ? false : !convertToSlot(binStack).isEmpty();
   }

   public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
      ItemStack binStack = this.findBinStack(inv);
      return binStack.m_41619_() ? ItemStack.f_41583_ : convertToSlot(binStack).getBottomStack();
   }

   private ItemStack findBinStack(CraftingContainer inv) {
      ItemStack binStack = ItemStack.f_41583_;
      int i = 0;

      for (int slots = inv.m_6643_(); i < slots; i++) {
         ItemStack stackInSlot = inv.m_8020_(i);
         if (!stackInSlot.m_41619_()) {
            if (!(stackInSlot.m_41720_() instanceof ItemBlockBin)) {
               return ItemStack.f_41583_;
            }

            if (!binStack.m_41619_()) {
               return ItemStack.f_41583_;
            }

            binStack = stackInSlot;
         }
      }

      return binStack;
   }

   public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
      int slots = inv.m_6643_();
      NonNullList<ItemStack> remaining = NonNullList.m_122780_(slots, ItemStack.f_41583_);

      for (int i = 0; i < slots; i++) {
         ItemStack stackInSlot = inv.m_8020_(i);
         if (stackInSlot.m_41720_() instanceof ItemBlockBin) {
            ItemStack binStack = stackInSlot.m_41777_();
            BinInventorySlot slot = convertToSlot(binStack);
            ItemStack bottomStack = slot.getBottomStack();
            if (!bottomStack.m_41619_()) {
               MekanismUtils.logMismatchedStackSize(slot.shrinkStack(bottomStack.m_41613_(), Action.EXECUTE), bottomStack.m_41613_());
               remaining.set(i, binStack);
            }
            break;
         }
      }

      return remaining;
   }

   public boolean m_8004_(int width, int height) {
      return width * height >= 1;
   }

   public RecipeSerializer<?> m_7707_() {
      return (RecipeSerializer<?>)MekanismRecipeSerializers.BIN_EXTRACT.get();
   }
}
