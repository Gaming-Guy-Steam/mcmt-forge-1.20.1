package mekanism.common.recipe.lookup.cache.type;

import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.recipe.ingredient.creator.ItemStackIngredientCreator;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CompoundIngredient;
import net.minecraftforge.common.crafting.StrictNBTIngredient;

public class ItemInputCache<RECIPE extends MekanismRecipe> extends NBTSensitiveInputCache<Item, HashedItem, ItemStack, ItemStackIngredient, RECIPE> {
   public boolean mapInputs(RECIPE recipe, ItemStackIngredient inputIngredient) {
      if (inputIngredient instanceof ItemStackIngredientCreator.SingleItemStackIngredient single) {
         return this.mapIngredient(recipe, single.getInputRaw());
      } else {
         return inputIngredient instanceof ItemStackIngredientCreator.MultiItemStackIngredient multi ? this.mapMultiInputs(recipe, multi) : true;
      }
   }

   private boolean mapIngredient(RECIPE recipe, Ingredient input) {
      if (!input.isVanilla() && !input.isSimple()) {
         if (input instanceof CompoundIngredient compoundIngredient) {
            boolean result = false;

            for (Ingredient child : compoundIngredient.getChildren()) {
               result |= this.mapIngredient(recipe, child);
            }

            return result;
         }

         if (!(input instanceof StrictNBTIngredient)) {
            return true;
         }

         this.addNbtInputCache(HashedItem.create(input.m_43908_()[0]), recipe);
      } else {
         for (ItemStack item : input.m_43908_()) {
            if (!item.m_41619_()) {
               this.addInputCache(item.m_41720_(), recipe);
            }
         }
      }

      return false;
   }

   protected Item createKey(ItemStack stack) {
      return stack.m_41720_();
   }

   protected HashedItem createNbtKey(ItemStack stack) {
      return HashedItem.raw(stack);
   }

   public boolean isEmpty(ItemStack input) {
      return input.m_41619_();
   }
}
