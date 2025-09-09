package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.chemical.ItemStackToChemicalRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public abstract class ItemStackToChemicalRecipeSerializer<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, RECIPE extends ItemStackToChemicalRecipe<CHEMICAL, STACK>>
   implements RecipeSerializer<RECIPE> {
   private final ItemStackToChemicalRecipeSerializer.IFactory<CHEMICAL, STACK, RECIPE> factory;

   protected ItemStackToChemicalRecipeSerializer(ItemStackToChemicalRecipeSerializer.IFactory<CHEMICAL, STACK, RECIPE> factory) {
      this.factory = factory;
   }

   protected abstract STACK fromJson(@NotNull JsonObject json, @NotNull String key);

   protected abstract STACK fromBuffer(@NotNull FriendlyByteBuf buffer);

   @NotNull
   public RECIPE fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
      JsonElement input = (JsonElement)(GsonHelper.m_13885_(json, "input") ? GsonHelper.m_13933_(json, "input") : GsonHelper.m_13930_(json, "input"));
      ItemStackIngredient inputIngredient = IngredientCreatorAccess.item().deserialize(input);
      STACK output = this.fromJson(json, "output");
      if (output.isEmpty()) {
         throw new JsonSyntaxException("Recipe output must not be empty.");
      } else {
         return this.factory.create(recipeId, inputIngredient, output);
      }
   }

   public RECIPE fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
      try {
         ItemStackIngredient inputIngredient = IngredientCreatorAccess.item().read(buffer);
         STACK output = this.fromBuffer(buffer);
         return this.factory.create(recipeId, inputIngredient, output);
      } catch (Exception var5) {
         Mekanism.logger.error("Error reading itemstack to chemical recipe from packet.", var5);
         throw var5;
      }
   }

   public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull RECIPE recipe) {
      try {
         recipe.write(buffer);
      } catch (Exception var4) {
         Mekanism.logger.error("Error writing itemstack to chemical recipe to packet.", var4);
         throw var4;
      }
   }

   @FunctionalInterface
   public interface IFactory<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, RECIPE extends ItemStackToChemicalRecipe<CHEMICAL, STACK>> {
      RECIPE create(ResourceLocation id, ItemStackIngredient input, STACK output);
   }
}
