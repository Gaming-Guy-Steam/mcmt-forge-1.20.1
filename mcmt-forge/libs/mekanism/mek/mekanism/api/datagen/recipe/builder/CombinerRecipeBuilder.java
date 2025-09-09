package mekanism.api.datagen.recipe.builder;

import com.google.gson.JsonObject;
import java.util.function.Consumer;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public class CombinerRecipeBuilder extends MekanismRecipeBuilder<CombinerRecipeBuilder> {
   private final ItemStackIngredient mainInput;
   private final ItemStackIngredient extraInput;
   private final ItemStack output;

   protected CombinerRecipeBuilder(ItemStackIngredient mainInput, ItemStackIngredient extraInput, ItemStack output) {
      super(mekSerializer("combining"));
      this.mainInput = mainInput;
      this.extraInput = extraInput;
      this.output = output;
   }

   public static CombinerRecipeBuilder combining(ItemStackIngredient mainInput, ItemStackIngredient extraInput, ItemStack output) {
      if (output.m_41619_()) {
         throw new IllegalArgumentException("This combining recipe requires a non empty item output.");
      } else {
         return new CombinerRecipeBuilder(mainInput, extraInput, output);
      }
   }

   protected CombinerRecipeBuilder.CombinerRecipeResult getResult(ResourceLocation id) {
      return new CombinerRecipeBuilder.CombinerRecipeResult(id);
   }

   public void build(Consumer<FinishedRecipe> consumer) {
      this.build(consumer, this.output.m_41720_());
   }

   public class CombinerRecipeResult extends MekanismRecipeBuilder<CombinerRecipeBuilder>.RecipeResult {
      protected CombinerRecipeResult(ResourceLocation id) {
         super(id);
      }

      public void m_7917_(@NotNull JsonObject json) {
         json.add("mainInput", CombinerRecipeBuilder.this.mainInput.serialize());
         json.add("extraInput", CombinerRecipeBuilder.this.extraInput.serialize());
         json.add("output", SerializerHelper.serializeItemStack(CombinerRecipeBuilder.this.output));
      }
   }
}
