package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class ChemicalCrystallizerRecipeSerializer<RECIPE extends ChemicalCrystallizerRecipe> implements RecipeSerializer<RECIPE> {
   private final ChemicalCrystallizerRecipeSerializer.IFactory<RECIPE> factory;

   public ChemicalCrystallizerRecipeSerializer(ChemicalCrystallizerRecipeSerializer.IFactory<RECIPE> factory) {
      this.factory = factory;
   }

   @NotNull
   public RECIPE fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
      ChemicalType chemicalType = SerializerHelper.getChemicalType(json);
      JsonElement input = (JsonElement)(GsonHelper.m_13885_(json, "input") ? GsonHelper.m_13933_(json, "input") : GsonHelper.m_13930_(json, "input"));
      ChemicalStackIngredient<?, ?> inputIngredient = IngredientCreatorAccess.getCreatorForType(chemicalType).deserialize(input);
      ItemStack output = SerializerHelper.getItemStack(json, "output");
      if (output.m_41619_()) {
         throw new JsonSyntaxException("Recipe output must not be empty.");
      } else {
         return this.factory.create(recipeId, inputIngredient, output);
      }
   }

   public RECIPE fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
      try {
         ChemicalType chemicalType = (ChemicalType)buffer.m_130066_(ChemicalType.class);
         ChemicalStackIngredient<?, ?> inputIngredient = IngredientCreatorAccess.getCreatorForType(chemicalType).read(buffer);
         ItemStack output = buffer.m_130267_();
         return this.factory.create(recipeId, inputIngredient, output);
      } catch (Exception var6) {
         Mekanism.logger.error("Error reading boxed chemical to itemstack recipe from packet.", var6);
         throw var6;
      }
   }

   public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull RECIPE recipe) {
      try {
         recipe.write(buffer);
      } catch (Exception var4) {
         Mekanism.logger.error("Error writing boxed chemical to itemstack recipe to packet.", var4);
         throw var4;
      }
   }

   @FunctionalInterface
   public interface IFactory<RECIPE extends ChemicalCrystallizerRecipe> {
      RECIPE create(ResourceLocation id, ChemicalStackIngredient<?, ?> input, ItemStack output);
   }
}
