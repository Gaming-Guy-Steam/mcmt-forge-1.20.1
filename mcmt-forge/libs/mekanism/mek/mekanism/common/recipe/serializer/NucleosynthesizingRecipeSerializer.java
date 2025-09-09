package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import mekanism.api.SerializerHelper;
import mekanism.api.recipes.NucleosynthesizingRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class NucleosynthesizingRecipeSerializer<RECIPE extends NucleosynthesizingRecipe> implements RecipeSerializer<RECIPE> {
   private final NucleosynthesizingRecipeSerializer.IFactory<RECIPE> factory;

   public NucleosynthesizingRecipeSerializer(NucleosynthesizingRecipeSerializer.IFactory<RECIPE> factory) {
      this.factory = factory;
   }

   @NotNull
   public RECIPE fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
      JsonElement itemInput = (JsonElement)(GsonHelper.m_13885_(json, "itemInput")
         ? GsonHelper.m_13933_(json, "itemInput")
         : GsonHelper.m_13930_(json, "itemInput"));
      ItemStackIngredient itemIngredient = IngredientCreatorAccess.item().deserialize(itemInput);
      JsonElement gasInput = (JsonElement)(GsonHelper.m_13885_(json, "gasInput")
         ? GsonHelper.m_13933_(json, "gasInput")
         : GsonHelper.m_13930_(json, "gasInput"));
      ChemicalStackIngredient.GasStackIngredient gasIngredient = (ChemicalStackIngredient.GasStackIngredient)IngredientCreatorAccess.gas()
         .deserialize(gasInput);
      JsonElement ticks = json.get("duration");
      if (!GsonHelper.m_13872_(ticks)) {
         throw new JsonSyntaxException("Expected duration to be a number greater than zero.");
      } else {
         int duration = ticks.getAsJsonPrimitive().getAsInt();
         if (duration <= 0) {
            throw new JsonSyntaxException("Expected duration to be a number greater than zero.");
         } else {
            ItemStack itemOutput = SerializerHelper.getItemStack(json, "output");
            if (itemOutput.m_41619_()) {
               throw new JsonSyntaxException("Nucleosynthesizing item output must not be empty.");
            } else {
               return this.factory.create(recipeId, itemIngredient, gasIngredient, itemOutput, duration);
            }
         }
      }
   }

   public RECIPE fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
      try {
         ItemStackIngredient inputSolid = IngredientCreatorAccess.item().read(buffer);
         ChemicalStackIngredient.GasStackIngredient inputGas = (ChemicalStackIngredient.GasStackIngredient)IngredientCreatorAccess.gas().read(buffer);
         ItemStack outputItem = buffer.m_130267_();
         int duration = buffer.m_130242_();
         return this.factory.create(recipeId, inputSolid, inputGas, outputItem, duration);
      } catch (Exception var7) {
         Mekanism.logger.error("Error reading nucleosynthesizing recipe from packet.", var7);
         throw var7;
      }
   }

   public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull RECIPE recipe) {
      try {
         recipe.write(buffer);
      } catch (Exception var4) {
         Mekanism.logger.error("Error writing nucleosynthesizing recipe to packet.", var4);
         throw var4;
      }
   }

   @FunctionalInterface
   public interface IFactory<RECIPE extends NucleosynthesizingRecipe> {
      RECIPE create(ResourceLocation id, ItemStackIngredient itemInput, ChemicalStackIngredient.GasStackIngredient gasInput, ItemStack outputItem, int duration);
   }
}
