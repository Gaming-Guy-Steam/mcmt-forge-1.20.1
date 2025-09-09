package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.recipe.component.IDecomposedRecipe;
import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler.For;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import java.util.Optional;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.integration.crafttweaker.recipe.manager.ElectrolysisRecipeManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

@For(ElectrolysisRecipe.class)
public class ElectrolysisRecipeHandler extends MekanismRecipeHandler<ElectrolysisRecipe> {
   public String dumpToCommandString(IRecipeManager<? super ElectrolysisRecipe> manager, ElectrolysisRecipe recipe) {
      return this.buildCommandString(
         manager,
         recipe,
         new Object[]{
            recipe.getInput(),
            recipe.getOutputDefinition(),
            recipe.getEnergyMultiplier().equals(FloatingLong.ONE) ? SKIP_OPTIONAL_PARAM : recipe.getEnergyMultiplier()
         }
      );
   }

   public <U extends Recipe<?>> boolean doesConflict(IRecipeManager<? super ElectrolysisRecipe> manager, ElectrolysisRecipe recipe, U o) {
      return o instanceof ElectrolysisRecipe other && this.ingredientConflicts(recipe.getInput(), other.getInput());
   }

   public Optional<IDecomposedRecipe> decompose(IRecipeManager<? super ElectrolysisRecipe> manager, ElectrolysisRecipe recipe) {
      return this.decompose(new Object[]{recipe.getInput(), recipe.getOutputDefinition(), recipe.getEnergyMultiplier()});
   }

   public Optional<ElectrolysisRecipe> recompose(IRecipeManager<? super ElectrolysisRecipe> m, ResourceLocation name, IDecomposedRecipe recipe) {
      if (m instanceof ElectrolysisRecipeManager manager) {
         CrTUtils.UnaryTypePair<ICrTChemicalStack.ICrTGasStack> output = CrTUtils.getPair(recipe, CrTRecipeComponents.GAS.output());
         return Optional.of(
            manager.makeRecipe(
               name,
               (FluidStackIngredient)recipe.getOrThrowSingle(CrTRecipeComponents.FLUID.input()),
               output.a(),
               output.b(),
               CrTUtils.<FloatingLong>getSingleIfPresent(recipe, CrTRecipeComponents.ENERGY).orElse(FloatingLong.ONE)
            )
         );
      } else {
         return Optional.empty();
      }
   }
}
