package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.chemical.ChemicalChemicalToChemicalRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ingredient.chemical.ChemicalIngredientDeserializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public abstract class ChemicalChemicalToChemicalRecipeSerializer<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK>, RECIPE extends ChemicalChemicalToChemicalRecipe<CHEMICAL, STACK, INGREDIENT>>
   implements RecipeSerializer<RECIPE> {
   private final ChemicalChemicalToChemicalRecipeSerializer.IFactory<CHEMICAL, STACK, INGREDIENT, RECIPE> factory;

   protected ChemicalChemicalToChemicalRecipeSerializer(ChemicalChemicalToChemicalRecipeSerializer.IFactory<CHEMICAL, STACK, INGREDIENT, RECIPE> factory) {
      this.factory = factory;
   }

   protected abstract ChemicalIngredientDeserializer<CHEMICAL, STACK, INGREDIENT> getDeserializer();

   protected abstract STACK fromJson(@NotNull JsonObject json, @NotNull String key);

   protected abstract STACK fromBuffer(@NotNull FriendlyByteBuf buffer);

   @NotNull
   public RECIPE fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
      JsonElement leftIngredients = (JsonElement)(GsonHelper.m_13885_(json, "leftInput")
         ? GsonHelper.m_13933_(json, "leftInput")
         : GsonHelper.m_13930_(json, "leftInput"));
      INGREDIENT leftInput = this.getDeserializer().deserialize(leftIngredients);
      JsonElement rightIngredients = (JsonElement)(GsonHelper.m_13885_(json, "rightInput")
         ? GsonHelper.m_13933_(json, "rightInput")
         : GsonHelper.m_13930_(json, "rightInput"));
      INGREDIENT rightInput = this.getDeserializer().deserialize(rightIngredients);
      STACK output = this.fromJson(json, "output");
      if (output.isEmpty()) {
         throw new JsonSyntaxException("Recipe output must not be empty.");
      } else {
         return this.factory.create(recipeId, leftInput, rightInput, output);
      }
   }

   public RECIPE fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
      try {
         INGREDIENT leftInput = this.getDeserializer().read(buffer);
         INGREDIENT rightInput = this.getDeserializer().read(buffer);
         STACK output = this.fromBuffer(buffer);
         return this.factory.create(recipeId, leftInput, rightInput, output);
      } catch (Exception var6) {
         Mekanism.logger.error("Error reading chemical chemical to chemical recipe from packet.", var6);
         throw var6;
      }
   }

   public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull RECIPE recipe) {
      try {
         recipe.write(buffer);
      } catch (Exception var4) {
         Mekanism.logger.error("Error writing chemical chemical to chemical recipe to packet.", var4);
         throw var4;
      }
   }

   @FunctionalInterface
   public interface IFactory<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK>, RECIPE extends ChemicalChemicalToChemicalRecipe<CHEMICAL, STACK, INGREDIENT>> {
      RECIPE create(ResourceLocation id, INGREDIENT leftInput, INGREDIENT rightInput, STACK output);
   }
}
