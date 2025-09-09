package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.recipe.component.IDecomposedRecipe;
import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler.For;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import java.util.Optional;
import mekanism.api.recipes.ChemicalDissolutionRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.integration.crafttweaker.recipe.manager.ChemicalDissolutionRecipeManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

@For(ChemicalDissolutionRecipe.class)
public class ChemicalDissolutionRecipeHandler extends MekanismRecipeHandler<ChemicalDissolutionRecipe> {
   public String dumpToCommandString(IRecipeManager<? super ChemicalDissolutionRecipe> manager, ChemicalDissolutionRecipe recipe) {
      return this.buildCommandString(manager, recipe, new Object[]{recipe.getItemInput(), recipe.getGasInput(), recipe.getOutputDefinition()});
   }

   public <U extends Recipe<?>> boolean doesConflict(IRecipeManager<? super ChemicalDissolutionRecipe> manager, ChemicalDissolutionRecipe recipe, U o) {
      return !(o instanceof ChemicalDissolutionRecipe other)
         ? false
         : this.ingredientConflicts(recipe.getItemInput(), other.getItemInput()) && this.ingredientConflicts(recipe.getGasInput(), other.getGasInput());
   }

   public Optional<IDecomposedRecipe> decompose(IRecipeManager<? super ChemicalDissolutionRecipe> manager, ChemicalDissolutionRecipe recipe) {
      return this.decompose(new Object[]{recipe.getItemInput(), recipe.getGasInput(), recipe.getOutputDefinition()});
   }

   public Optional<ChemicalDissolutionRecipe> recompose(IRecipeManager<? super ChemicalDissolutionRecipe> m, ResourceLocation name, IDecomposedRecipe recipe) {
      if (m instanceof ChemicalDissolutionRecipeManager manager) {
         Optional<? extends ICrTChemicalStack<?, ?, ?>> output = CrTRecipeComponents.CHEMICAL_COMPONENTS
            .stream()
            .map(chemicalComponent -> CrTUtils.getSingleIfPresent(recipe, chemicalComponent.output()))
            .filter(Optional::isPresent)
            .findFirst()
            .flatMap(singleIfPresent -> (Optional<? extends ICrTChemicalStack<?, ?, ?>>)singleIfPresent);
         return Optional.of(
            manager.makeRecipe(
               name,
               (ItemStackIngredient)recipe.getOrThrowSingle(CrTRecipeComponents.ITEM.input()),
               (ChemicalStackIngredient.GasStackIngredient)recipe.getOrThrowSingle(CrTRecipeComponents.GAS.input()),
               (ICrTChemicalStack<?, ?, ?>)output.orElseThrow(() -> new IllegalArgumentException("No specified output chemical."))
            )
         );
      } else {
         return Optional.empty();
      }
   }
}
