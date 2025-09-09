package mekanism.api.datagen.recipe.builder;

import com.google.gson.JsonObject;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public class GasToGasRecipeBuilder extends MekanismRecipeBuilder<GasToGasRecipeBuilder> {
   private final ChemicalStackIngredient.GasStackIngredient input;
   private final GasStack output;

   protected GasToGasRecipeBuilder(ChemicalStackIngredient.GasStackIngredient input, GasStack output, ResourceLocation serializerName) {
      super(serializerName);
      this.input = input;
      this.output = output;
   }

   public static GasToGasRecipeBuilder activating(ChemicalStackIngredient.GasStackIngredient input, GasStack output) {
      if (output.isEmpty()) {
         throw new IllegalArgumentException("This solar neutron activator recipe requires a non empty gas output.");
      } else {
         return new GasToGasRecipeBuilder(input, output, mekSerializer("activating"));
      }
   }

   public static GasToGasRecipeBuilder centrifuging(ChemicalStackIngredient.GasStackIngredient input, GasStack output) {
      if (output.isEmpty()) {
         throw new IllegalArgumentException("This Isotopic Centrifuge recipe requires a non empty gas output.");
      } else {
         return new GasToGasRecipeBuilder(input, output, mekSerializer("centrifuging"));
      }
   }

   protected GasToGasRecipeBuilder.GasToGasRecipeResult getResult(ResourceLocation id) {
      return new GasToGasRecipeBuilder.GasToGasRecipeResult(id);
   }

   public class GasToGasRecipeResult extends MekanismRecipeBuilder<GasToGasRecipeBuilder>.RecipeResult {
      protected GasToGasRecipeResult(ResourceLocation id) {
         super(id);
      }

      public void m_7917_(@NotNull JsonObject json) {
         json.add("input", GasToGasRecipeBuilder.this.input.serialize());
         json.add("output", SerializerHelper.serializeGasStack(GasToGasRecipeBuilder.this.output));
      }
   }
}
