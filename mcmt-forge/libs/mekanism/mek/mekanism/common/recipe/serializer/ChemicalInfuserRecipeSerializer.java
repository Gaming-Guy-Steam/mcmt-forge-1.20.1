package mekanism.common.recipe.serializer;

import com.google.gson.JsonObject;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ChemicalInfuserRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.recipe.ingredient.chemical.ChemicalIngredientDeserializer;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public class ChemicalInfuserRecipeSerializer<RECIPE extends ChemicalInfuserRecipe>
   extends ChemicalChemicalToChemicalRecipeSerializer<Gas, GasStack, ChemicalStackIngredient.GasStackIngredient, RECIPE> {
   public ChemicalInfuserRecipeSerializer(
      ChemicalChemicalToChemicalRecipeSerializer.IFactory<Gas, GasStack, ChemicalStackIngredient.GasStackIngredient, RECIPE> factory
   ) {
      super(factory);
   }

   @Override
   protected ChemicalIngredientDeserializer<Gas, GasStack, ChemicalStackIngredient.GasStackIngredient> getDeserializer() {
      return ChemicalIngredientDeserializer.GAS;
   }

   protected GasStack fromJson(@NotNull JsonObject json, @NotNull String key) {
      return SerializerHelper.getGasStack(json, key);
   }

   protected GasStack fromBuffer(@NotNull FriendlyByteBuf buffer) {
      return GasStack.readFromPacket(buffer);
   }
}
