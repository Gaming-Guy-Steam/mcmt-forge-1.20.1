package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import mekanism.api.SerializerHelper;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class SawmillRecipeSerializer<RECIPE extends SawmillRecipe> implements RecipeSerializer<RECIPE> {
   private final SawmillRecipeSerializer.IFactory<RECIPE> factory;

   public SawmillRecipeSerializer(SawmillRecipeSerializer.IFactory<RECIPE> factory) {
      this.factory = factory;
   }

   @NotNull
   public RECIPE fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
      JsonElement input = (JsonElement)(GsonHelper.m_13885_(json, "input") ? GsonHelper.m_13933_(json, "input") : GsonHelper.m_13930_(json, "input"));
      ItemStackIngredient inputIngredient = IngredientCreatorAccess.item().deserialize(input);
      ItemStack mainOutput = ItemStack.f_41583_;
      ItemStack secondaryOutput = ItemStack.f_41583_;
      double secondaryChance = 0.0;
      if (!json.has("secondaryOutput") && !json.has("secondaryChance")) {
         mainOutput = SerializerHelper.getItemStack(json, "mainOutput");
         if (mainOutput.m_41619_()) {
            throw new JsonSyntaxException("Sawmill main recipe output must not be empty, if there is no secondary output.");
         }
      } else {
         if (json.has("mainOutput")) {
            mainOutput = SerializerHelper.getItemStack(json, "mainOutput");
            if (mainOutput.m_41619_()) {
               throw new JsonSyntaxException("Sawmill main recipe output must not be empty, if it is defined.");
            }
         }

         JsonElement chance = json.get("secondaryChance");
         if (!GsonHelper.m_13872_(chance)) {
            throw new JsonSyntaxException("Expected secondaryChance to be a number greater than zero.");
         }

         secondaryChance = chance.getAsJsonPrimitive().getAsDouble();
         if (secondaryChance <= 0.0 || secondaryChance > 1.0) {
            throw new JsonSyntaxException("Expected secondaryChance to be greater than zero, and less than or equal to one.");
         }

         secondaryOutput = SerializerHelper.getItemStack(json, "secondaryOutput");
         if (secondaryOutput.m_41619_()) {
            throw new JsonSyntaxException("Sawmill secondary recipe output must not be empty, if there is no main output.");
         }
      }

      return this.factory.create(recipeId, inputIngredient, mainOutput, secondaryOutput, secondaryChance);
   }

   public RECIPE fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
      try {
         ItemStackIngredient inputIngredient = IngredientCreatorAccess.item().read(buffer);
         ItemStack mainOutput = buffer.m_130267_();
         ItemStack secondaryOutput = buffer.m_130267_();
         double secondaryChance = buffer.readDouble();
         return this.factory.create(recipeId, inputIngredient, mainOutput, secondaryOutput, secondaryChance);
      } catch (Exception var8) {
         Mekanism.logger.error("Error reading sawmill recipe from packet.", var8);
         throw var8;
      }
   }

   public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull RECIPE recipe) {
      try {
         recipe.write(buffer);
      } catch (Exception var4) {
         Mekanism.logger.error("Error writing sawmill recipe to packet.", var4);
         throw var4;
      }
   }

   @FunctionalInterface
   public interface IFactory<RECIPE extends SawmillRecipe> {
      RECIPE create(ResourceLocation id, ItemStackIngredient input, ItemStack mainOutput, ItemStack secondaryOutput, double secondaryChance);
   }
}
