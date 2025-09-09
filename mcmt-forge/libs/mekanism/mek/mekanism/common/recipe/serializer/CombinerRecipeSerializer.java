package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import mekanism.api.SerializerHelper;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class CombinerRecipeSerializer<RECIPE extends CombinerRecipe> implements RecipeSerializer<RECIPE> {
   private final CombinerRecipeSerializer.IFactory<RECIPE> factory;

   public CombinerRecipeSerializer(CombinerRecipeSerializer.IFactory<RECIPE> factory) {
      this.factory = factory;
   }

   @NotNull
   public RECIPE fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
      JsonElement mainInput = (JsonElement)(GsonHelper.m_13885_(json, "mainInput")
         ? GsonHelper.m_13933_(json, "mainInput")
         : GsonHelper.m_13930_(json, "mainInput"));
      ItemStackIngredient mainIngredient = IngredientCreatorAccess.item().deserialize(mainInput);
      JsonElement extraInput = (JsonElement)(GsonHelper.m_13885_(json, "extraInput")
         ? GsonHelper.m_13933_(json, "extraInput")
         : GsonHelper.m_13930_(json, "extraInput"));
      ItemStackIngredient extraIngredient = IngredientCreatorAccess.item().deserialize(extraInput);
      ItemStack output = SerializerHelper.getItemStack(json, "output");
      if (output.m_41619_()) {
         throw new JsonSyntaxException("Combiner recipe output must not be empty.");
      } else {
         return this.factory.create(recipeId, mainIngredient, extraIngredient, output);
      }
   }

   public RECIPE fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
      try {
         ItemStackIngredient mainInput = IngredientCreatorAccess.item().read(buffer);
         ItemStackIngredient extraInput = IngredientCreatorAccess.item().read(buffer);
         ItemStack output = buffer.m_130267_();
         return this.factory.create(recipeId, mainInput, extraInput, output);
      } catch (Exception var6) {
         Mekanism.logger.error("Error reading combiner recipe from packet.", var6);
         throw var6;
      }
   }

   public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull RECIPE recipe) {
      try {
         recipe.write(buffer);
      } catch (Exception var4) {
         Mekanism.logger.error("Error writing combiner recipe to packet.", var4);
         throw var4;
      }
   }

   @FunctionalInterface
   public interface IFactory<RECIPE extends CombinerRecipe> {
      RECIPE create(ResourceLocation id, ItemStackIngredient mainInput, ItemStackIngredient extraInput, ItemStack output);
   }
}
