package mekanism.common.recipe.lookup.cache.type;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.ingredients.InputIngredient;
import org.jetbrains.annotations.Nullable;

public abstract class NBTSensitiveInputCache<KEY, NBT_KEY, INPUT, INGREDIENT extends InputIngredient<INPUT>, RECIPE extends MekanismRecipe>
   extends BaseInputCache<KEY, INPUT, INGREDIENT, RECIPE> {
   private final Map<NBT_KEY, Set<RECIPE>> nbtInputCache = new HashMap<>();

   @Override
   public void clear() {
      super.clear();
      this.nbtInputCache.clear();
   }

   @Override
   public boolean contains(INPUT input) {
      return this.nbtInputCache.containsKey(this.createNbtKey(input)) || super.contains(input);
   }

   @Override
   public boolean contains(INPUT input, Predicate<RECIPE> matchCriteria) {
      Set<RECIPE> recipes = this.nbtInputCache.get(this.createNbtKey(input));
      return recipes != null && recipes.stream().anyMatch(matchCriteria) || super.contains(input, matchCriteria);
   }

   @Nullable
   @Override
   public RECIPE findFirstRecipe(INPUT input, Predicate<RECIPE> matchCriteria) {
      RECIPE recipe = this.findFirstRecipe(this.nbtInputCache.get(this.createNbtKey(input)), matchCriteria);
      return recipe == null ? super.findFirstRecipe(input, matchCriteria) : recipe;
   }

   protected abstract NBT_KEY createNbtKey(INPUT input);

   protected void addNbtInputCache(NBT_KEY input, RECIPE recipe) {
      this.nbtInputCache.computeIfAbsent(input, i -> new HashSet<>()).add(recipe);
   }
}
