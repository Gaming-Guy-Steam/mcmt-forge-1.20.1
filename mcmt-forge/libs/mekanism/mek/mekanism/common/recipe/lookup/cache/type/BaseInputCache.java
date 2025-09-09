package mekanism.common.recipe.lookup.cache.type;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.ingredients.InputIngredient;
import org.jetbrains.annotations.Nullable;

public abstract class BaseInputCache<KEY, INPUT, INGREDIENT extends InputIngredient<INPUT>, RECIPE extends MekanismRecipe>
   implements IInputCache<INPUT, INGREDIENT, RECIPE> {
   private final Map<KEY, Set<RECIPE>> inputCache = new HashMap<>();

   @Override
   public void clear() {
      this.inputCache.clear();
   }

   @Override
   public boolean contains(INPUT input) {
      return this.inputCache.containsKey(this.createKey(input));
   }

   @Override
   public boolean contains(INPUT input, Predicate<RECIPE> matchCriteria) {
      Set<RECIPE> recipes = this.inputCache.get(this.createKey(input));
      return recipes != null && recipes.stream().anyMatch(matchCriteria);
   }

   @Nullable
   @Override
   public RECIPE findFirstRecipe(INPUT input, Predicate<RECIPE> matchCriteria) {
      return this.findFirstRecipe(this.inputCache.get(this.createKey(input)), matchCriteria);
   }

   @Nullable
   protected RECIPE findFirstRecipe(@Nullable Collection<RECIPE> recipes, Predicate<RECIPE> matchCriteria) {
      return recipes == null ? null : recipes.stream().filter(matchCriteria).findFirst().orElse(null);
   }

   protected abstract KEY createKey(INPUT input);

   protected void addInputCache(KEY input, RECIPE recipe) {
      this.inputCache.computeIfAbsent(input, i -> new HashSet<>()).add(recipe);
   }
}
