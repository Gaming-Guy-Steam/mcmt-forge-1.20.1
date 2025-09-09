package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.recipe.component.IDecomposedRecipe;
import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler.For;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import java.util.Optional;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.integration.crafttweaker.recipe.manager.GasToGasRecipeManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

@For(GasToGasRecipe.class)
public class GasToGasRecipeHandler extends MekanismRecipeHandler<GasToGasRecipe> {
   public String dumpToCommandString(IRecipeManager<? super GasToGasRecipe> manager, GasToGasRecipe recipe) {
      return this.buildCommandString(manager, recipe, new Object[]{recipe.getInput(), recipe.getOutputDefinition()});
   }

   public <U extends Recipe<?>> boolean doesConflict(IRecipeManager<? super GasToGasRecipe> manager, GasToGasRecipe recipe, U o) {
      return o instanceof GasToGasRecipe other && this.ingredientConflicts(recipe.getInput(), other.getInput());
   }

   public Optional<IDecomposedRecipe> decompose(IRecipeManager<? super GasToGasRecipe> manager, GasToGasRecipe recipe) {
      return this.decompose(new Object[]{recipe.getInput(), recipe.getOutputDefinition()});
   }

   public Optional<GasToGasRecipe> recompose(IRecipeManager<? super GasToGasRecipe> m, ResourceLocation name, IDecomposedRecipe recipe) {
      return m instanceof GasToGasRecipeManager manager
         ? Optional.of(
            manager.makeRecipe(
               name,
               (ChemicalStackIngredient.GasStackIngredient)recipe.getOrThrowSingle(CrTRecipeComponents.GAS.input()),
               (ICrTChemicalStack.ICrTGasStack)recipe.getOrThrowSingle(CrTRecipeComponents.GAS.output())
            )
         )
         : Optional.empty();
   }
}
