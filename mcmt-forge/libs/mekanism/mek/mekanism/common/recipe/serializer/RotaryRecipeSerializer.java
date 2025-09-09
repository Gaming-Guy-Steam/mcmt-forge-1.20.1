package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class RotaryRecipeSerializer<RECIPE extends RotaryRecipe> implements RecipeSerializer<RECIPE> {
   private final RotaryRecipeSerializer.IFactory<RECIPE> factory;

   public RotaryRecipeSerializer(RotaryRecipeSerializer.IFactory<RECIPE> factory) {
      this.factory = factory;
   }

   @NotNull
   public RECIPE fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
      FluidStackIngredient fluidInputIngredient = null;
      ChemicalStackIngredient.GasStackIngredient gasInputIngredient = null;
      GasStack gasOutput = null;
      FluidStack fluidOutput = null;
      boolean hasFluidToGas = false;
      boolean hasGasToFluid = false;
      if (json.has("fluidInput") || json.has("gasOutput")) {
         JsonElement fluidInput = (JsonElement)(GsonHelper.m_13885_(json, "fluidInput")
            ? GsonHelper.m_13933_(json, "fluidInput")
            : GsonHelper.m_13930_(json, "fluidInput"));
         fluidInputIngredient = IngredientCreatorAccess.fluid().deserialize(fluidInput);
         gasOutput = SerializerHelper.getGasStack(json, "gasOutput");
         hasFluidToGas = true;
         if (gasOutput.isEmpty()) {
            throw new JsonSyntaxException("Rotary recipe gas output cannot be empty if it is defined.");
         }
      }

      if (json.has("gasInput") || json.has("fluidOutput")) {
         JsonElement gasInput = (JsonElement)(GsonHelper.m_13885_(json, "gasInput")
            ? GsonHelper.m_13933_(json, "gasInput")
            : GsonHelper.m_13930_(json, "gasInput"));
         gasInputIngredient = (ChemicalStackIngredient.GasStackIngredient)IngredientCreatorAccess.gas().deserialize(gasInput);
         fluidOutput = SerializerHelper.getFluidStack(json, "fluidOutput");
         hasGasToFluid = true;
         if (fluidOutput.isEmpty()) {
            throw new JsonSyntaxException("Rotary recipe fluid output cannot be empty if it is defined.");
         }
      }

      if (hasFluidToGas && hasGasToFluid) {
         return this.factory.create(recipeId, fluidInputIngredient, gasInputIngredient, gasOutput, fluidOutput);
      } else if (hasFluidToGas) {
         return this.factory.create(recipeId, fluidInputIngredient, gasOutput);
      } else if (hasGasToFluid) {
         return this.factory.create(recipeId, gasInputIngredient, fluidOutput);
      } else {
         throw new JsonSyntaxException("Rotary recipes require at least a gas to fluid or fluid to gas conversion.");
      }
   }

   public RECIPE fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
      try {
         FluidStackIngredient fluidInputIngredient = null;
         ChemicalStackIngredient.GasStackIngredient gasInputIngredient = null;
         GasStack gasOutput = null;
         FluidStack fluidOutput = null;
         boolean hasFluidToGas = buffer.readBoolean();
         if (hasFluidToGas) {
            fluidInputIngredient = IngredientCreatorAccess.fluid().read(buffer);
            gasOutput = GasStack.readFromPacket(buffer);
         }

         boolean hasGasToFluid = buffer.readBoolean();
         if (hasGasToFluid) {
            gasInputIngredient = (ChemicalStackIngredient.GasStackIngredient)IngredientCreatorAccess.gas().read(buffer);
            fluidOutput = FluidStack.readFromPacket(buffer);
         }

         if (hasFluidToGas && hasGasToFluid) {
            return this.factory.create(recipeId, fluidInputIngredient, gasInputIngredient, gasOutput, fluidOutput);
         } else if (hasFluidToGas) {
            return this.factory.create(recipeId, fluidInputIngredient, gasOutput);
         } else if (hasGasToFluid) {
            return this.factory.create(recipeId, gasInputIngredient, fluidOutput);
         } else {
            Mekanism.logger.error("Error reading rotary recipe from packet. A recipe got sent with no conversion in either direction.");
            return null;
         }
      } catch (Exception var9) {
         Mekanism.logger.error("Error reading rotary recipe from packet.", var9);
         throw var9;
      }
   }

   public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull RECIPE recipe) {
      if (!recipe.hasFluidToGas() && !recipe.hasGasToFluid()) {
         Mekanism.logger.error("Error writing rotary recipe to packet. {} has no conversion in either direction, so was not sent.", recipe);
      } else {
         try {
            recipe.write(buffer);
         } catch (Exception var4) {
            Mekanism.logger.error("Error writing rotary recipe to packet.", var4);
            throw var4;
         }
      }
   }

   public interface IFactory<RECIPE extends RotaryRecipe> {
      RECIPE create(ResourceLocation id, FluidStackIngredient fluidInput, GasStack gasOutput);

      RECIPE create(ResourceLocation id, ChemicalStackIngredient.GasStackIngredient gasInput, FluidStack fluidOutput);

      RECIPE create(
         ResourceLocation id, FluidStackIngredient fluidInput, ChemicalStackIngredient.GasStackIngredient gasInput, GasStack gasOutput, FluidStack fluidOutput
      );
   }
}
