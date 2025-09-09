package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.recipes.ChemicalDissolutionRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class ChemicalDissolutionRecipeSerializer<RECIPE extends ChemicalDissolutionRecipe> implements RecipeSerializer<RECIPE> {
   private final ChemicalDissolutionRecipeSerializer.IFactory<RECIPE> factory;

   public ChemicalDissolutionRecipeSerializer(ChemicalDissolutionRecipeSerializer.IFactory<RECIPE> factory) {
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
      ChemicalStack<?> output = SerializerHelper.getBoxedChemicalStack(json, "output");
      if (output.isEmpty()) {
         throw new JsonSyntaxException("Recipe output must not be empty.");
      } else {
         return this.factory.create(recipeId, itemIngredient, gasIngredient, output);
      }
   }

   public RECIPE fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
      try {
         ItemStackIngredient itemInput = IngredientCreatorAccess.item().read(buffer);
         ChemicalStackIngredient.GasStackIngredient gasInput = (ChemicalStackIngredient.GasStackIngredient)IngredientCreatorAccess.gas().read(buffer);
         ChemicalType chemicalType = (ChemicalType)buffer.m_130066_(ChemicalType.class);

         ChemicalStack<?> output = (ChemicalStack<?>)(switch (chemicalType) {
            case GAS -> GasStack.readFromPacket(buffer);
            case INFUSION -> InfusionStack.readFromPacket(buffer);
            case PIGMENT -> PigmentStack.readFromPacket(buffer);
            case SLURRY -> SlurryStack.readFromPacket(buffer);
         });
         return this.factory.create(recipeId, itemInput, gasInput, output);
      } catch (Exception var7) {
         Mekanism.logger.error("Error reading itemstack gas to gas recipe from packet.", var7);
         throw var7;
      }
   }

   public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull RECIPE recipe) {
      try {
         recipe.write(buffer);
      } catch (Exception var4) {
         Mekanism.logger.error("Error writing itemstack gas to gas recipe to packet.", var4);
         throw var4;
      }
   }

   @FunctionalInterface
   public interface IFactory<RECIPE extends ChemicalDissolutionRecipe> {
      RECIPE create(ResourceLocation id, ItemStackIngredient itemInput, ChemicalStackIngredient.GasStackIngredient gasInput, ChemicalStack<?> output);
   }
}
