package mekanism.common.recipe.serializer;

import com.google.gson.JsonObject;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.PigmentMixingRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.recipe.ingredient.chemical.ChemicalIngredientDeserializer;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public class PigmentMixingRecipeSerializer<RECIPE extends PigmentMixingRecipe>
   extends ChemicalChemicalToChemicalRecipeSerializer<Pigment, PigmentStack, ChemicalStackIngredient.PigmentStackIngredient, RECIPE> {
   public PigmentMixingRecipeSerializer(
      ChemicalChemicalToChemicalRecipeSerializer.IFactory<Pigment, PigmentStack, ChemicalStackIngredient.PigmentStackIngredient, RECIPE> factory
   ) {
      super(factory);
   }

   @Override
   protected ChemicalIngredientDeserializer<Pigment, PigmentStack, ChemicalStackIngredient.PigmentStackIngredient> getDeserializer() {
      return ChemicalIngredientDeserializer.PIGMENT;
   }

   protected PigmentStack fromJson(@NotNull JsonObject json, @NotNull String key) {
      return SerializerHelper.getPigmentStack(json, key);
   }

   protected PigmentStack fromBuffer(@NotNull FriendlyByteBuf buffer) {
      return PigmentStack.readFromPacket(buffer);
   }
}
