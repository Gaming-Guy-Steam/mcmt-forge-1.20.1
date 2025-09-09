package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.recipes.FluidSlurryToSlurryRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class FluidSlurryToSlurryRecipeSerializer<RECIPE extends FluidSlurryToSlurryRecipe> implements RecipeSerializer<RECIPE> {
   private final FluidSlurryToSlurryRecipeSerializer.IFactory<RECIPE> factory;

   public FluidSlurryToSlurryRecipeSerializer(FluidSlurryToSlurryRecipeSerializer.IFactory<RECIPE> factory) {
      this.factory = factory;
   }

   @NotNull
   public RECIPE fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
      JsonElement fluidInput = (JsonElement)(GsonHelper.m_13885_(json, "fluidInput")
         ? GsonHelper.m_13933_(json, "fluidInput")
         : GsonHelper.m_13930_(json, "fluidInput"));
      FluidStackIngredient fluidIngredient = IngredientCreatorAccess.fluid().deserialize(fluidInput);
      JsonElement slurryInput = (JsonElement)(GsonHelper.m_13885_(json, "slurryInput")
         ? GsonHelper.m_13933_(json, "slurryInput")
         : GsonHelper.m_13930_(json, "slurryInput"));
      ChemicalStackIngredient.SlurryStackIngredient slurryIngredient = (ChemicalStackIngredient.SlurryStackIngredient)IngredientCreatorAccess.slurry()
         .deserialize(slurryInput);
      SlurryStack output = SerializerHelper.getSlurryStack(json, "output");
      if (output.isEmpty()) {
         throw new JsonSyntaxException("Recipe output must not be empty.");
      } else {
         return this.factory.create(recipeId, fluidIngredient, slurryIngredient, output);
      }
   }

   public RECIPE fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
      try {
         FluidStackIngredient fluidInput = IngredientCreatorAccess.fluid().read(buffer);
         ChemicalStackIngredient.SlurryStackIngredient slurryInput = (ChemicalStackIngredient.SlurryStackIngredient)IngredientCreatorAccess.slurry()
            .read(buffer);
         SlurryStack output = SlurryStack.readFromPacket(buffer);
         return this.factory.create(recipeId, fluidInput, slurryInput, output);
      } catch (Exception var6) {
         Mekanism.logger.error("Error reading fluid slurry to slurry recipe from packet.", var6);
         throw var6;
      }
   }

   public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull RECIPE recipe) {
      try {
         recipe.write(buffer);
      } catch (Exception var4) {
         Mekanism.logger.error("Error writing fluid slurry to slurry recipe to packet.", var4);
         throw var4;
      }
   }

   @FunctionalInterface
   public interface IFactory<RECIPE extends FluidSlurryToSlurryRecipe> {
      RECIPE create(ResourceLocation id, FluidStackIngredient fluidInput, ChemicalStackIngredient.SlurryStackIngredient slurryInput, SlurryStack output);
   }
}
