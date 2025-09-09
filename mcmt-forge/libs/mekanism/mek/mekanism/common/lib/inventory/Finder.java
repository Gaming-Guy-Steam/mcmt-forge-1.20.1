package mekanism.common.lib.inventory;

import mekanism.common.lib.WildcardMatcher;
import mekanism.common.util.MekanismUtils;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.items.ItemHandlerHelper;

public interface Finder {
   Finder ANY = stack -> true;

   static Finder item(Item itemType) {
      return stack -> itemType != Items.f_41852_ && itemType == stack.m_41720_();
   }

   static Finder item(ItemStack itemType) {
      return item(itemType.m_41720_());
   }

   static Finder strict(ItemStack itemType) {
      return stack -> ItemHandlerHelper.canItemStacksStack(itemType, stack);
   }

   static Finder tag(String tagName) {
      return stack -> !stack.m_41619_() && stack.m_204131_().anyMatch(tag -> WildcardMatcher.matches(tagName, (TagKey<?>)tag));
   }

   static Finder modID(String modID) {
      return stack -> !stack.m_41619_() && WildcardMatcher.matches(modID, MekanismUtils.getModId(stack));
   }

   boolean modifies(ItemStack stack);
}
