package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class ElectrolysisRecipeSerializer<RECIPE extends ElectrolysisRecipe> implements RecipeSerializer<RECIPE> {
   private final ElectrolysisRecipeSerializer.IFactory<RECIPE> factory;

   public ElectrolysisRecipeSerializer(ElectrolysisRecipeSerializer.IFactory<RECIPE> factory) {
      this.factory = factory;
   }

   @NotNull
   public RECIPE fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
      JsonElement input = (JsonElement)(GsonHelper.m_13885_(json, "input") ? GsonHelper.m_13933_(json, "input") : GsonHelper.m_13930_(json, "input"));
      FluidStackIngredient inputIngredient = IngredientCreatorAccess.fluid().deserialize(input);
      GasStack leftGasOutput = SerializerHelper.getGasStack(json, "leftGasOutput");
      GasStack rightGasOutput = SerializerHelper.getGasStack(json, "rightGasOutput");
      FloatingLong energyMultiplier = FloatingLong.ONE;
      if (json.has("energyMultiplier")) {
         energyMultiplier = SerializerHelper.getFloatingLong(json, "energyMultiplier");
         if (energyMultiplier.smallerThan(FloatingLong.ONE)) {
            throw new JsonSyntaxException("Expected energyMultiplier to be at least one.");
         }
      }

      if (!leftGasOutput.isEmpty() && !rightGasOutput.isEmpty()) {
         return this.factory.create(recipeId, inputIngredient, energyMultiplier, leftGasOutput, rightGasOutput);
      } else {
         throw new JsonSyntaxException("Electrolysis recipe outputs must not be empty.");
      }
   }

   public RECIPE fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
      try {
         FluidStackIngredient input = IngredientCreatorAccess.fluid().read(buffer);
         FloatingLong energyMultiplier = FloatingLong.readFromBuffer(buffer);
         GasStack leftGasOutput = GasStack.readFromPacket(buffer);
         GasStack rightGasOutput = GasStack.readFromPacket(buffer);
         return this.factory.create(recipeId, input, energyMultiplier, leftGasOutput, rightGasOutput);
      } catch (Exception var7) {
         Mekanism.logger.error("Error reading electrolysis recipe from packet.", var7);
         throw var7;
      }
   }

   public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull RECIPE recipe) {
      try {
         recipe.write(buffer);
      } catch (Exception var4) {
         Mekanism.logger.error("Error writing electrolysis recipe to packet.", var4);
         throw var4;
      }
   }

   @FunctionalInterface
   public interface IFactory<RECIPE extends ElectrolysisRecipe> {
      RECIPE create(ResourceLocation id, FluidStackIngredient input, FloatingLong energyMultiplier, GasStack leftGasOutput, GasStack rightGasOutput);
   }
}
