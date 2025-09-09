package mekanism.api.datagen.recipe.builder;

import com.google.gson.JsonObject;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public class ChemicalDissolutionRecipeBuilder extends MekanismRecipeBuilder<ChemicalDissolutionRecipeBuilder> {
   private final ItemStackIngredient itemInput;
   private final ChemicalStackIngredient.GasStackIngredient gasInput;
   private final BoxedChemicalStack output;

   protected ChemicalDissolutionRecipeBuilder(
      ResourceLocation serializerName, ItemStackIngredient itemInput, ChemicalStackIngredient.GasStackIngredient gasInput, ChemicalStack<?> output
   ) {
      super(serializerName);
      this.itemInput = itemInput;
      this.gasInput = gasInput;
      this.output = BoxedChemicalStack.box(output);
   }

   public static ChemicalDissolutionRecipeBuilder dissolution(
      ItemStackIngredient itemInput, ChemicalStackIngredient.GasStackIngredient gasInput, ChemicalStack<?> output
   ) {
      if (output.isEmpty()) {
         throw new IllegalArgumentException("This dissolution chamber recipe requires a non empty chemical output.");
      } else {
         return new ChemicalDissolutionRecipeBuilder(mekSerializer("dissolution"), itemInput, gasInput, output);
      }
   }

   protected ChemicalDissolutionRecipeBuilder.ChemicalDissolutionRecipeResult getResult(ResourceLocation id) {
      return new ChemicalDissolutionRecipeBuilder.ChemicalDissolutionRecipeResult(id);
   }

   public class ChemicalDissolutionRecipeResult extends MekanismRecipeBuilder<ChemicalDissolutionRecipeBuilder>.RecipeResult {
      protected ChemicalDissolutionRecipeResult(ResourceLocation id) {
         super(id);
      }

      public void m_7917_(@NotNull JsonObject json) {
         json.add("itemInput", ChemicalDissolutionRecipeBuilder.this.itemInput.serialize());
         json.add("gasInput", ChemicalDissolutionRecipeBuilder.this.gasInput.serialize());
         json.add("output", SerializerHelper.serializeBoxedChemicalStack(ChemicalDissolutionRecipeBuilder.this.output));
      }
   }
}
