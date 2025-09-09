package mekanism.common.recipe.serializer;

import com.google.gson.JsonObject;
import java.util.function.Function;
import mekanism.common.Mekanism;
import mekanism.common.recipe.WrappedShapedRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.jetbrains.annotations.NotNull;

public class WrappedShapedRecipeSerializer<RECIPE extends WrappedShapedRecipe> implements RecipeSerializer<RECIPE> {
   private final Function<ShapedRecipe, RECIPE> wrapper;

   public WrappedShapedRecipeSerializer(Function<ShapedRecipe, RECIPE> wrapper) {
      this.wrapper = wrapper;
   }

   @NotNull
   public RECIPE fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
      return this.wrapper.apply((ShapedRecipe)RecipeSerializer.f_44076_.m_6729_(recipeId, json));
   }

   public RECIPE fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
      try {
         return this.wrapper.apply((ShapedRecipe)RecipeSerializer.f_44076_.m_8005_(recipeId, buffer));
      } catch (Exception var4) {
         Mekanism.logger.error("Error reading wrapped shaped recipe from packet.", var4);
         throw var4;
      }
   }

   public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull RECIPE recipe) {
      try {
         RecipeSerializer.f_44076_.m_6178_(buffer, recipe.getInternal());
      } catch (Exception var4) {
         Mekanism.logger.error("Error writing wrapped shaped recipe to packet.", var4);
         throw var4;
      }
   }
}
