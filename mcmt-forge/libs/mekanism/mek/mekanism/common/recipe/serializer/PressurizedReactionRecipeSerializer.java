package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class PressurizedReactionRecipeSerializer<RECIPE extends PressurizedReactionRecipe> implements RecipeSerializer<RECIPE> {
   private final PressurizedReactionRecipeSerializer.IFactory<RECIPE> factory;

   public PressurizedReactionRecipeSerializer(PressurizedReactionRecipeSerializer.IFactory<RECIPE> factory) {
      this.factory = factory;
   }

   @NotNull
   public RECIPE fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
      JsonElement itemInput = (JsonElement)(GsonHelper.m_13885_(json, "itemInput")
         ? GsonHelper.m_13933_(json, "itemInput")
         : GsonHelper.m_13930_(json, "itemInput"));
      ItemStackIngredient solidIngredient = IngredientCreatorAccess.item().deserialize(itemInput);
      JsonElement fluidInput = (JsonElement)(GsonHelper.m_13885_(json, "fluidInput")
         ? GsonHelper.m_13933_(json, "fluidInput")
         : GsonHelper.m_13930_(json, "fluidInput"));
      FluidStackIngredient fluidIngredient = IngredientCreatorAccess.fluid().deserialize(fluidInput);
      JsonElement gasInput = (JsonElement)(GsonHelper.m_13885_(json, "gasInput")
         ? GsonHelper.m_13933_(json, "gasInput")
         : GsonHelper.m_13930_(json, "gasInput"));
      ChemicalStackIngredient.GasStackIngredient gasIngredient = (ChemicalStackIngredient.GasStackIngredient)IngredientCreatorAccess.gas()
         .deserialize(gasInput);
      FloatingLong energyRequired = FloatingLong.ZERO;
      if (json.has("energyRequired")) {
         energyRequired = SerializerHelper.getFloatingLong(json, "energyRequired");
      }

      JsonElement ticks = json.get("duration");
      if (!GsonHelper.m_13872_(ticks)) {
         throw new JsonSyntaxException("Expected duration to be a number greater than zero.");
      } else {
         int duration = ticks.getAsJsonPrimitive().getAsInt();
         if (duration <= 0) {
            throw new JsonSyntaxException("Expected duration to be a number greater than zero.");
         } else {
            ItemStack itemOutput = ItemStack.f_41583_;
            GasStack gasOutput = GasStack.EMPTY;
            if (json.has("itemOutput")) {
               itemOutput = SerializerHelper.getItemStack(json, "itemOutput");
               if (itemOutput.m_41619_()) {
                  throw new JsonSyntaxException("Reaction chamber item output must not be empty, if it is defined.");
               }

               if (json.has("gasOutput")) {
                  gasOutput = SerializerHelper.getGasStack(json, "gasOutput");
                  if (gasOutput.isEmpty()) {
                     throw new JsonSyntaxException("Reaction chamber gas output must not be empty, if it is defined.");
                  }
               }
            } else {
               gasOutput = SerializerHelper.getGasStack(json, "gasOutput");
               if (gasOutput.isEmpty()) {
                  throw new JsonSyntaxException("Reaction chamber gas output must not be empty, if there is no item output.");
               }
            }

            return this.factory.create(recipeId, solidIngredient, fluidIngredient, gasIngredient, energyRequired, duration, itemOutput, gasOutput);
         }
      }
   }

   public RECIPE fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
      try {
         ItemStackIngredient inputSolid = IngredientCreatorAccess.item().read(buffer);
         FluidStackIngredient inputFluid = IngredientCreatorAccess.fluid().read(buffer);
         ChemicalStackIngredient.GasStackIngredient inputGas = (ChemicalStackIngredient.GasStackIngredient)IngredientCreatorAccess.gas().read(buffer);
         FloatingLong energyRequired = FloatingLong.readFromBuffer(buffer);
         int duration = buffer.m_130242_();
         ItemStack outputItem = buffer.m_130267_();
         GasStack outputGas = GasStack.readFromPacket(buffer);
         return this.factory.create(recipeId, inputSolid, inputFluid, inputGas, energyRequired, duration, outputItem, outputGas);
      } catch (Exception var10) {
         Mekanism.logger.error("Error reading pressurized reaction recipe from packet.", var10);
         throw var10;
      }
   }

   public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull RECIPE recipe) {
      try {
         recipe.write(buffer);
      } catch (Exception var4) {
         Mekanism.logger.error("Error writing pressurized reaction recipe to packet.", var4);
         throw var4;
      }
   }

   @FunctionalInterface
   public interface IFactory<RECIPE extends PressurizedReactionRecipe> {
      RECIPE create(
         ResourceLocation id,
         ItemStackIngredient itemInput,
         FluidStackIngredient fluidInput,
         ChemicalStackIngredient.GasStackIngredient gasInput,
         FloatingLong energyRequired,
         int duration,
         ItemStack outputItem,
         GasStack outputGas
      );
   }
}
