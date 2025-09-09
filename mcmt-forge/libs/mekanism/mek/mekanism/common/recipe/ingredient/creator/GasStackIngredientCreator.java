package mekanism.common.recipe.ingredient.creator;

import java.util.Objects;
import java.util.stream.Stream;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.providers.IChemicalProvider;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.recipe.ingredient.chemical.ChemicalIngredientDeserializer;
import mekanism.common.recipe.ingredient.chemical.ChemicalIngredientInfo;
import mekanism.common.recipe.ingredient.chemical.SingleChemicalStackIngredient;
import mekanism.common.recipe.ingredient.chemical.TaggedChemicalStackIngredient;
import net.minecraft.tags.TagKey;

@NothingNullByDefault
public class GasStackIngredientCreator extends ChemicalStackIngredientCreator<Gas, GasStack, ChemicalStackIngredient.GasStackIngredient> {
   public static final GasStackIngredientCreator INSTANCE = new GasStackIngredientCreator();

   private GasStackIngredientCreator() {
   }

   @Override
   protected ChemicalIngredientDeserializer<Gas, GasStack, ChemicalStackIngredient.GasStackIngredient> getDeserializer() {
      return ChemicalIngredientDeserializer.GAS;
   }

   public ChemicalStackIngredient.GasStackIngredient from(IChemicalProvider<Gas> provider, long amount) {
      Objects.requireNonNull(provider, "GasStackIngredients cannot be created from a null chemical provider.");
      Gas gas = provider.getChemical();
      this.assertNonEmpty(gas);
      this.assertPositiveAmount(amount);
      return new GasStackIngredientCreator.SingleGasStackIngredient(gas.getStack(amount));
   }

   public ChemicalStackIngredient.GasStackIngredient from(TagKey<Gas> tag, long amount) {
      Objects.requireNonNull(tag, "GasStackIngredients cannot be created from a null tag.");
      this.assertPositiveAmount(amount);
      return new GasStackIngredientCreator.TaggedGasStackIngredient(tag, amount);
   }

   public ChemicalStackIngredient.GasStackIngredient from(Stream<ChemicalStackIngredient.GasStackIngredient> ingredients) {
      return this.createMulti(ingredients.toArray(ChemicalStackIngredient.GasStackIngredient[]::new));
   }

   public static class SingleGasStackIngredient extends SingleChemicalStackIngredient<Gas, GasStack> implements ChemicalStackIngredient.GasStackIngredient {
      private SingleGasStackIngredient(GasStack stack) {
         super(stack);
      }

      @Override
      protected ChemicalIngredientInfo<Gas, GasStack> getIngredientInfo() {
         return ChemicalIngredientInfo.GAS;
      }
   }

   public static class TaggedGasStackIngredient extends TaggedChemicalStackIngredient<Gas, GasStack> implements ChemicalStackIngredient.GasStackIngredient {
      private TaggedGasStackIngredient(TagKey<Gas> tag, long amount) {
         super(ChemicalTags.GAS, tag, amount);
      }

      @Override
      protected ChemicalIngredientInfo<Gas, GasStack> getIngredientInfo() {
         return ChemicalIngredientInfo.GAS;
      }
   }
}
