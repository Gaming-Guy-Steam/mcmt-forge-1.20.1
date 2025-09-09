package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.chemical.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ingredient.chemical.ChemicalIngredientDeserializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public abstract class ItemStackChemicalToItemStackRecipeSerializer<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK>, RECIPE extends ItemStackChemicalToItemStackRecipe<CHEMICAL, STACK, INGREDIENT>>
   implements RecipeSerializer<RECIPE> {
   private final ItemStackChemicalToItemStackRecipeSerializer.IFactory<CHEMICAL, STACK, INGREDIENT, RECIPE> factory;

   protected ItemStackChemicalToItemStackRecipeSerializer(ItemStackChemicalToItemStackRecipeSerializer.IFactory<CHEMICAL, STACK, INGREDIENT, RECIPE> factory) {
      this.factory = factory;
   }

   protected abstract ChemicalIngredientDeserializer<CHEMICAL, STACK, INGREDIENT> getDeserializer();

   @NotNull
   public RECIPE fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
      JsonElement itemInput = (JsonElement)(GsonHelper.m_13885_(json, "itemInput")
         ? GsonHelper.m_13933_(json, "itemInput")
         : GsonHelper.m_13930_(json, "itemInput"));
      ItemStackIngredient itemIngredient = IngredientCreatorAccess.item().deserialize(itemInput);
      JsonElement chemicalInput = (JsonElement)(GsonHelper.m_13885_(json, "chemicalInput")
         ? GsonHelper.m_13933_(json, "chemicalInput")
         : GsonHelper.m_13930_(json, "chemicalInput"));
      INGREDIENT chemicalIngredient = this.getDeserializer().deserialize(chemicalInput);
      ItemStack output = SerializerHelper.getItemStack(json, "output");
      if (output.m_41619_()) {
         throw new JsonSyntaxException("Recipe output must not be empty.");
      } else {
         return this.factory.create(recipeId, itemIngredient, chemicalIngredient, output);
      }
   }

   public RECIPE fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
      try {
         ItemStackIngredient itemInput = IngredientCreatorAccess.item().read(buffer);
         INGREDIENT chemicalInput = this.getDeserializer().read(buffer);
         ItemStack output = buffer.m_130267_();
         return this.factory.create(recipeId, itemInput, chemicalInput, output);
      } catch (Exception var6) {
         Mekanism.logger.error("Error reading itemstack chemical to itemstack recipe from packet.", var6);
         throw var6;
      }
   }

   public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull RECIPE recipe) {
      try {
         recipe.write(buffer);
      } catch (Exception var4) {
         Mekanism.logger.error("Error writing itemstack chemical to itemstack recipe to packet.", var4);
         throw var4;
      }
   }

   @FunctionalInterface
   public interface IFactory<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK>, RECIPE extends ItemStackChemicalToItemStackRecipe<CHEMICAL, STACK, INGREDIENT>> {
      RECIPE create(ResourceLocation id, ItemStackIngredient itemInput, INGREDIENT chemicalInput, ItemStack output);
   }
}
