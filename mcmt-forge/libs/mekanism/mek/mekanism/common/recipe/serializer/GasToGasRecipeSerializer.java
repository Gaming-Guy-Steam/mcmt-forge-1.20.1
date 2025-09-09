package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class GasToGasRecipeSerializer<RECIPE extends GasToGasRecipe> implements RecipeSerializer<RECIPE> {
   private final GasToGasRecipeSerializer.IFactory<RECIPE> factory;

   public GasToGasRecipeSerializer(GasToGasRecipeSerializer.IFactory<RECIPE> factory) {
      this.factory = factory;
   }

   @NotNull
   public RECIPE fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
      JsonElement input = (JsonElement)(GsonHelper.m_13885_(json, "input") ? GsonHelper.m_13933_(json, "input") : GsonHelper.m_13930_(json, "input"));
      ChemicalStackIngredient.GasStackIngredient inputIngredient = (ChemicalStackIngredient.GasStackIngredient)IngredientCreatorAccess.gas().deserialize(input);
      GasStack output = SerializerHelper.getGasStack(json, "output");
      if (output.isEmpty()) {
         throw new JsonSyntaxException("Recipe output must not be empty.");
      } else {
         return this.factory.create(recipeId, inputIngredient, output);
      }
   }

   public RECIPE fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
      try {
         ChemicalStackIngredient.GasStackIngredient inputIngredient = (ChemicalStackIngredient.GasStackIngredient)IngredientCreatorAccess.gas().read(buffer);
         GasStack output = GasStack.readFromPacket(buffer);
         return this.factory.create(recipeId, inputIngredient, output);
      } catch (Exception var5) {
         Mekanism.logger.error("Error reading gas to gas recipe from packet.", var5);
         throw var5;
      }
   }

   public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull RECIPE recipe) {
      try {
         recipe.write(buffer);
      } catch (Exception var4) {
         Mekanism.logger.error("Error writing gas to gas recipe to packet.", var4);
         throw var4;
      }
   }

   @FunctionalInterface
   public interface IFactory<RECIPE extends GasToGasRecipe> {
      RECIPE create(ResourceLocation id, ChemicalStackIngredient.GasStackIngredient input, GasStack output);
   }
}
