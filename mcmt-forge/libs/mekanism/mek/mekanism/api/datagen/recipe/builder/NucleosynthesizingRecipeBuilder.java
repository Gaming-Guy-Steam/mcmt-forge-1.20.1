package mekanism.api.datagen.recipe.builder;

import com.google.gson.JsonObject;
import java.util.function.Consumer;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public class NucleosynthesizingRecipeBuilder extends MekanismRecipeBuilder<NucleosynthesizingRecipeBuilder> {
   private final ItemStackIngredient itemInput;
   private final ChemicalStackIngredient.GasStackIngredient gasInput;
   private final ItemStack output;
   private final int duration;

   protected NucleosynthesizingRecipeBuilder(ItemStackIngredient itemInput, ChemicalStackIngredient.GasStackIngredient gasInput, ItemStack output, int duration) {
      super(mekSerializer("nucleosynthesizing"));
      this.itemInput = itemInput;
      this.gasInput = gasInput;
      this.output = output;
      this.duration = duration;
   }

   public static NucleosynthesizingRecipeBuilder nucleosynthesizing(
      ItemStackIngredient itemInput, ChemicalStackIngredient.GasStackIngredient gasInput, ItemStack output, int duration
   ) {
      if (output.m_41619_()) {
         throw new IllegalArgumentException("This nucleosynthesizing recipe requires a non empty item output.");
      } else if (duration <= 0) {
         throw new IllegalArgumentException("This nucleosynthesizing recipe must have a positive duration.");
      } else {
         return new NucleosynthesizingRecipeBuilder(itemInput, gasInput, output, duration);
      }
   }

   protected NucleosynthesizingRecipeBuilder.NucleosynthesizingRecipeResult getResult(ResourceLocation id) {
      return new NucleosynthesizingRecipeBuilder.NucleosynthesizingRecipeResult(id);
   }

   public void build(Consumer<FinishedRecipe> consumer) {
      this.build(consumer, this.output.m_41720_());
   }

   public class NucleosynthesizingRecipeResult extends MekanismRecipeBuilder<NucleosynthesizingRecipeBuilder>.RecipeResult {
      protected NucleosynthesizingRecipeResult(ResourceLocation id) {
         super(id);
      }

      public void m_7917_(@NotNull JsonObject json) {
         json.add("itemInput", NucleosynthesizingRecipeBuilder.this.itemInput.serialize());
         json.add("gasInput", NucleosynthesizingRecipeBuilder.this.gasInput.serialize());
         json.add("output", SerializerHelper.serializeItemStack(NucleosynthesizingRecipeBuilder.this.output));
         json.addProperty("duration", NucleosynthesizingRecipeBuilder.this.duration);
      }
   }
}
