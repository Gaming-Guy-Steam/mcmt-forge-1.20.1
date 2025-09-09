package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import mekanism.api.SerializerHelper;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ItemStackToEnergyRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class ItemStackToEnergyRecipeSerializer<RECIPE extends ItemStackToEnergyRecipe> implements RecipeSerializer<RECIPE> {
   private final ItemStackToEnergyRecipeSerializer.IFactory<RECIPE> factory;

   public ItemStackToEnergyRecipeSerializer(ItemStackToEnergyRecipeSerializer.IFactory<RECIPE> factory) {
      this.factory = factory;
   }

   @NotNull
   public RECIPE fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
      JsonElement input = (JsonElement)(GsonHelper.m_13885_(json, "input") ? GsonHelper.m_13933_(json, "input") : GsonHelper.m_13930_(json, "input"));
      ItemStackIngredient inputIngredient = IngredientCreatorAccess.item().deserialize(input);
      FloatingLong output = SerializerHelper.getFloatingLong(json, "output");
      if (output.isZero()) {
         throw new JsonSyntaxException("Expected output to be greater than zero.");
      } else {
         return this.factory.create(recipeId, inputIngredient, output);
      }
   }

   public RECIPE fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
      try {
         ItemStackIngredient inputIngredient = IngredientCreatorAccess.item().read(buffer);
         FloatingLong output = FloatingLong.readFromBuffer(buffer);
         return this.factory.create(recipeId, inputIngredient, output);
      } catch (Exception var5) {
         Mekanism.logger.error("Error reading itemstack to energy recipe from packet.", var5);
         throw var5;
      }
   }

   public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull RECIPE recipe) {
      try {
         recipe.write(buffer);
      } catch (Exception var4) {
         Mekanism.logger.error("Error writing itemstack to energy recipe to packet.", var4);
         throw var4;
      }
   }

   @FunctionalInterface
   public interface IFactory<RECIPE extends ItemStackToEnergyRecipe> {
      RECIPE create(ResourceLocation id, ItemStackIngredient input, FloatingLong output);
   }
}
