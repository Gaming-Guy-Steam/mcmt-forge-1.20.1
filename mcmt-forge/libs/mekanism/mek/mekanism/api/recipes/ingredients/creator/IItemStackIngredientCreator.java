package mekanism.api.recipes.ingredients.creator;

import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.StrictNBTIngredient;

@NothingNullByDefault
public interface IItemStackIngredientCreator extends IIngredientCreator<Item, ItemStack, ItemStackIngredient> {
   default ItemStackIngredient from(ItemStack instance) {
      Objects.requireNonNull(instance, "ItemStackIngredients cannot be created from a null ItemStack.");
      return this.from(instance, instance.m_41613_());
   }

   default ItemStackIngredient from(ItemStack stack, int amount) {
      Objects.requireNonNull(stack, "ItemStackIngredients cannot be created from a null ItemStack.");
      if (stack.m_41619_()) {
         throw new IllegalArgumentException("ItemStackIngredients cannot be created using the empty stack.");
      } else {
         stack = stack.m_41777_();
         Ingredient ingredient = (Ingredient)(stack.m_41782_() ? StrictNBTIngredient.of(stack) : Ingredient.m_43927_(new ItemStack[]{stack}));
         return this.from(ingredient, amount);
      }
   }

   default ItemStackIngredient from(ItemLike item) {
      return this.from(item, 1);
   }

   default ItemStackIngredient from(ItemLike item, int amount) {
      return this.from(new ItemStack(item), amount);
   }

   default ItemStackIngredient from(Item item, int amount) {
      return this.from((ItemLike)item, amount);
   }

   default ItemStackIngredient from(TagKey<Item> tag) {
      return this.from(tag, 1);
   }

   default ItemStackIngredient from(TagKey<Item> tag, int amount) {
      Objects.requireNonNull(tag, "ItemStackIngredients cannot be created from a null tag.");
      return this.from(Ingredient.m_204132_(tag), amount);
   }

   default ItemStackIngredient from(Ingredient ingredient) {
      return this.from(ingredient, 1);
   }

   ItemStackIngredient from(Ingredient var1, int var2);
}
