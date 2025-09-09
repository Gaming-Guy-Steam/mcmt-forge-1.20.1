package mekanism.api.recipes;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@NothingNullByDefault
public abstract class NucleosynthesizingRecipe extends ItemStackGasToItemStackRecipe {
   private final int duration;

   public NucleosynthesizingRecipe(
      ResourceLocation id, ItemStackIngredient itemInput, ChemicalStackIngredient.GasStackIngredient gasInput, ItemStack output, int duration
   ) {
      super(id, itemInput, gasInput, output);
      if (duration <= 0) {
         throw new IllegalArgumentException("Duration must be a number greater than zero.");
      } else {
         this.duration = duration;
      }
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      super.write(buffer);
      buffer.m_130130_(this.duration);
   }

   public int getDuration() {
      return this.duration;
   }
}
