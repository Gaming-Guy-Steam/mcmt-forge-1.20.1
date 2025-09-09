package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.recipe.component.IDecomposedRecipe;
import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler.For;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import java.util.Optional;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.recipe.manager.CombinerRecipeManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

@For(CombinerRecipe.class)
public class CombinerRecipeHandler extends MekanismRecipeHandler<CombinerRecipe> {
   public String dumpToCommandString(IRecipeManager<? super CombinerRecipe> manager, CombinerRecipe recipe) {
      return this.buildCommandString(manager, recipe, new Object[]{recipe.getMainInput(), recipe.getExtraInput(), recipe.getOutputDefinition()});
   }

   public <U extends Recipe<?>> boolean doesConflict(IRecipeManager<? super CombinerRecipe> manager, CombinerRecipe recipe, U o) {
      return !(o instanceof CombinerRecipe other)
         ? false
         : this.ingredientConflicts(recipe.getMainInput(), other.getMainInput()) && this.ingredientConflicts(recipe.getExtraInput(), other.getExtraInput());
   }

   public Optional<IDecomposedRecipe> decompose(IRecipeManager<? super CombinerRecipe> manager, CombinerRecipe recipe) {
      return this.decompose(new Object[]{recipe.getMainInput(), recipe.getExtraInput(), recipe.getOutputDefinition()});
   }

   public Optional<CombinerRecipe> recompose(IRecipeManager<? super CombinerRecipe> m, ResourceLocation name, IDecomposedRecipe recipe) {
      if (m instanceof CombinerRecipeManager manager) {
         CrTUtils.UnaryTypePair<ItemStackIngredient> inputs = CrTUtils.getPair(recipe, CrTRecipeComponents.ITEM.input());
         return Optional.of(manager.makeRecipe(name, inputs.a(), inputs.b(), (IItemStack)recipe.getOrThrowSingle(CrTRecipeComponents.ITEM.output())));
      } else {
         return Optional.empty();
      }
   }
}
