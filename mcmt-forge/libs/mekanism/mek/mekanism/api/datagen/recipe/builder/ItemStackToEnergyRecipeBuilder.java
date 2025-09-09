package mekanism.api.datagen.recipe.builder;

import com.google.gson.JsonObject;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public class ItemStackToEnergyRecipeBuilder extends MekanismRecipeBuilder<ItemStackToEnergyRecipeBuilder> {
   private final ItemStackIngredient input;
   private final FloatingLong output;

   protected ItemStackToEnergyRecipeBuilder(ItemStackIngredient input, FloatingLong output, ResourceLocation serializerName) {
      super(serializerName);
      this.input = input;
      this.output = output;
   }

   public static ItemStackToEnergyRecipeBuilder energyConversion(ItemStackIngredient input, FloatingLong output) {
      if (output.isZero()) {
         throw new IllegalArgumentException("This energy conversion recipe requires an energy output greater than zero");
      } else {
         return new ItemStackToEnergyRecipeBuilder(input, output, mekSerializer("energy_conversion"));
      }
   }

   protected ItemStackToEnergyRecipeBuilder.ItemStackToEnergyRecipeResult getResult(ResourceLocation id) {
      return new ItemStackToEnergyRecipeBuilder.ItemStackToEnergyRecipeResult(id);
   }

   public class ItemStackToEnergyRecipeResult extends MekanismRecipeBuilder<ItemStackToEnergyRecipeBuilder>.RecipeResult {
      protected ItemStackToEnergyRecipeResult(ResourceLocation id) {
         super(id);
      }

      public void m_7917_(@NotNull JsonObject json) {
         json.add("input", ItemStackToEnergyRecipeBuilder.this.input.serialize());
         json.addProperty("output", ItemStackToEnergyRecipeBuilder.this.output);
      }
   }
}
