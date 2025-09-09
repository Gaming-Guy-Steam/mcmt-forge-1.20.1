package mekanism.common.recipe.lookup.cache.type;

import java.util.function.Predicate;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.ingredients.InputIngredient;
import mekanism.common.recipe.ingredient.IMultiIngredient;
import org.jetbrains.annotations.Nullable;

public interface IInputCache<INPUT, INGREDIENT extends InputIngredient<INPUT>, RECIPE extends MekanismRecipe> {
   boolean contains(INPUT input);

   boolean contains(INPUT input, Predicate<RECIPE> matchCriteria);

   @Nullable
   RECIPE findFirstRecipe(INPUT input, Predicate<RECIPE> matchCriteria);

   boolean mapInputs(RECIPE recipe, INGREDIENT inputIngredient);

   default boolean mapMultiInputs(RECIPE recipe, IMultiIngredient<INPUT, ? extends INGREDIENT> multi) {
      return multi.forEachIngredient(ingredient -> this.mapInputs(recipe, (INGREDIENT)ingredient));
   }

   void clear();

   boolean isEmpty(INPUT input);
}
