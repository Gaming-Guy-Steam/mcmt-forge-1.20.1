package mekanism.common.recipe.ingredient;

import java.util.List;
import java.util.function.Predicate;
import mekanism.api.recipes.ingredients.InputIngredient;

public interface IMultiIngredient<TYPE, INGREDIENT extends InputIngredient<TYPE>> extends InputIngredient<TYPE> {
   boolean forEachIngredient(Predicate<INGREDIENT> checker);

   List<INGREDIENT> getIngredients();
}
