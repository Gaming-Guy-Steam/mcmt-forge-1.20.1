package mekanism.api.datagen.recipe.builder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.function.Function;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public class ChemicalChemicalToChemicalRecipeBuilder<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK>>
   extends MekanismRecipeBuilder<ChemicalChemicalToChemicalRecipeBuilder<CHEMICAL, STACK, INGREDIENT>> {
   private final Function<STACK, JsonElement> outputSerializer;
   private final INGREDIENT leftInput;
   private final INGREDIENT rightInput;
   private final STACK output;

   protected ChemicalChemicalToChemicalRecipeBuilder(
      ResourceLocation serializerName, INGREDIENT leftInput, INGREDIENT rightInput, STACK output, Function<STACK, JsonElement> outputSerializer
   ) {
      super(serializerName);
      this.leftInput = leftInput;
      this.rightInput = rightInput;
      this.output = output;
      this.outputSerializer = outputSerializer;
   }

   public static ChemicalChemicalToChemicalRecipeBuilder<Gas, GasStack, ChemicalStackIngredient.GasStackIngredient> chemicalInfusing(
      ChemicalStackIngredient.GasStackIngredient leftInput, ChemicalStackIngredient.GasStackIngredient rightInput, GasStack output
   ) {
      if (output.isEmpty()) {
         throw new IllegalArgumentException("This chemical infusing recipe requires a non empty gas output.");
      } else {
         return new ChemicalChemicalToChemicalRecipeBuilder<>(
            mekSerializer("chemical_infusing"), (INGREDIENT)leftInput, (INGREDIENT)rightInput, output, SerializerHelper::serializeGasStack
         );
      }
   }

   public static ChemicalChemicalToChemicalRecipeBuilder<Pigment, PigmentStack, ChemicalStackIngredient.PigmentStackIngredient> pigmentMixing(
      ChemicalStackIngredient.PigmentStackIngredient leftInput, ChemicalStackIngredient.PigmentStackIngredient rightInput, PigmentStack output
   ) {
      if (output.isEmpty()) {
         throw new IllegalArgumentException("This pigment mixing recipe requires a non empty gas output.");
      } else {
         return new ChemicalChemicalToChemicalRecipeBuilder<>(
            mekSerializer("pigment_mixing"), (INGREDIENT)leftInput, (INGREDIENT)rightInput, output, SerializerHelper::serializePigmentStack
         );
      }
   }

   protected ChemicalChemicalToChemicalRecipeBuilder<CHEMICAL, STACK, INGREDIENT>.ChemicalChemicalToChemicalRecipeResult getResult(ResourceLocation id) {
      return new ChemicalChemicalToChemicalRecipeBuilder.ChemicalChemicalToChemicalRecipeResult(id);
   }

   public class ChemicalChemicalToChemicalRecipeResult
      extends MekanismRecipeBuilder<ChemicalChemicalToChemicalRecipeBuilder<CHEMICAL, STACK, INGREDIENT>>.RecipeResult {
      protected ChemicalChemicalToChemicalRecipeResult(ResourceLocation id) {
         super(id);
      }

      public void m_7917_(@NotNull JsonObject json) {
         json.add("leftInput", ChemicalChemicalToChemicalRecipeBuilder.this.leftInput.serialize());
         json.add("rightInput", ChemicalChemicalToChemicalRecipeBuilder.this.rightInput.serialize());
         json.add("output", ChemicalChemicalToChemicalRecipeBuilder.this.outputSerializer.apply(ChemicalChemicalToChemicalRecipeBuilder.this.output));
      }
   }
}
