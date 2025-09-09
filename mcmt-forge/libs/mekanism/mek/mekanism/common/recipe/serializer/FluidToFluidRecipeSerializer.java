package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import mekanism.api.SerializerHelper;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class FluidToFluidRecipeSerializer<RECIPE extends FluidToFluidRecipe> implements RecipeSerializer<RECIPE> {
   private final FluidToFluidRecipeSerializer.IFactory<RECIPE> factory;

   public FluidToFluidRecipeSerializer(FluidToFluidRecipeSerializer.IFactory<RECIPE> factory) {
      this.factory = factory;
   }

   @NotNull
   public RECIPE fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
      JsonElement input = (JsonElement)(GsonHelper.m_13885_(json, "input") ? GsonHelper.m_13933_(json, "input") : GsonHelper.m_13930_(json, "input"));
      FluidStackIngredient inputIngredient = IngredientCreatorAccess.fluid().deserialize(input);
      FluidStack output = SerializerHelper.getFluidStack(json, "output");
      if (output.isEmpty()) {
         throw new JsonSyntaxException("Recipe output must not be empty.");
      } else {
         return this.factory.create(recipeId, inputIngredient, output);
      }
   }

   public RECIPE fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
      try {
         FluidStackIngredient inputIngredient = IngredientCreatorAccess.fluid().read(buffer);
         FluidStack output = FluidStack.readFromPacket(buffer);
         return this.factory.create(recipeId, inputIngredient, output);
      } catch (Exception var5) {
         Mekanism.logger.error("Error reading fluid to fluid recipe from packet.", var5);
         throw var5;
      }
   }

   public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull RECIPE recipe) {
      try {
         recipe.write(buffer);
      } catch (Exception var4) {
         Mekanism.logger.error("Error writing fluid to fluid recipe to packet.", var4);
         throw var4;
      }
   }

   @FunctionalInterface
   public interface IFactory<RECIPE extends FluidToFluidRecipe> {
      RECIPE create(ResourceLocation id, FluidStackIngredient input, FluidStack output);
   }
}
