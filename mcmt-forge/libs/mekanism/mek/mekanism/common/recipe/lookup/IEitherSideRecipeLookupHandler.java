package mekanism.common.recipe.lookup;

import java.util.function.BiPredicate;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.chemical.ChemicalChemicalToChemicalRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.common.recipe.lookup.cache.EitherSideInputRecipeCache;
import mekanism.common.recipe.lookup.cache.InputRecipeCache;
import mekanism.common.util.ChemicalUtil;
import org.jetbrains.annotations.Nullable;

public interface IEitherSideRecipeLookupHandler<INPUT, RECIPE extends MekanismRecipe & BiPredicate<INPUT, INPUT>, INPUT_CACHE extends EitherSideInputRecipeCache<INPUT, ?, RECIPE, ?>>
   extends IRecipeLookupHandler.IRecipeTypedLookupHandler<RECIPE, INPUT_CACHE> {
   default boolean containsRecipe(INPUT input) {
      return this.getRecipeType().getInputCache().containsInput(this.getHandlerWorld(), input);
   }

   default boolean containsRecipe(INPUT inputA, INPUT inputB) {
      return this.getRecipeType().getInputCache().containsInput(this.getHandlerWorld(), inputA, inputB);
   }

   @Nullable
   default RECIPE findFirstRecipe(INPUT inputA, INPUT inputB) {
      return this.getRecipeType().getInputCache().findFirstRecipe(this.getHandlerWorld(), inputA, inputB);
   }

   @Nullable
   default RECIPE findFirstRecipe(IInputHandler<INPUT> inputAHandler, IInputHandler<INPUT> inputBHandler) {
      return this.findFirstRecipe(inputAHandler.getInput(), inputBHandler.getInput());
   }

   public interface EitherSideChemicalRecipeLookupHandler<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, RECIPE extends ChemicalChemicalToChemicalRecipe<CHEMICAL, STACK, ? extends ChemicalStackIngredient<CHEMICAL, STACK>>>
      extends IEitherSideRecipeLookupHandler<STACK, RECIPE, InputRecipeCache.EitherSideChemical<CHEMICAL, STACK, RECIPE>> {
      default boolean containsRecipe(CHEMICAL input) {
         return this.containsRecipe(ChemicalUtil.withAmount(input, 1L));
      }

      default boolean containsRecipe(CHEMICAL inputA, STACK inputB) {
         return this.containsRecipe(ChemicalUtil.withAmount(inputA, 1L), inputB);
      }
   }
}
