package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.recipe.component.IDecomposedRecipe;
import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler.For;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import java.util.Optional;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.integration.crafttweaker.recipe.manager.RotaryRecipeManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

@For(RotaryRecipe.class)
public class RotaryRecipeHandler extends MekanismRecipeHandler<RotaryRecipe> {
   public String dumpToCommandString(IRecipeManager<? super RotaryRecipe> manager, RotaryRecipe recipe) {
      return this.buildCommandString(
         manager,
         recipe,
         new Object[]{
            recipe.hasFluidToGas() ? recipe.getFluidInput() : SKIP_OPTIONAL_PARAM,
            recipe.hasGasToFluid() ? recipe.getGasInput() : SKIP_OPTIONAL_PARAM,
            recipe.hasFluidToGas() ? recipe.getGasOutputDefinition() : SKIP_OPTIONAL_PARAM,
            recipe.hasGasToFluid() ? recipe.getFluidOutputDefinition() : SKIP_OPTIONAL_PARAM
         }
      );
   }

   public <U extends Recipe<?>> boolean doesConflict(IRecipeManager<? super RotaryRecipe> manager, RotaryRecipe recipe, U o) {
      return !(o instanceof RotaryRecipe other)
         ? false
         : recipe.hasFluidToGas() && other.hasFluidToGas() && this.ingredientConflicts(recipe.getFluidInput(), other.getFluidInput())
            || recipe.hasGasToFluid() && other.hasGasToFluid() && this.ingredientConflicts(recipe.getGasInput(), other.getGasInput());
   }

   public Optional<IDecomposedRecipe> decompose(IRecipeManager<? super RotaryRecipe> manager, RotaryRecipe recipe) {
      if (recipe.hasFluidToGas()) {
         return recipe.hasGasToFluid()
            ? this.decompose(new Object[]{recipe.getFluidInput(), recipe.getGasInput(), recipe.getGasOutputDefinition(), recipe.getFluidOutputDefinition()})
            : this.decompose(new Object[]{recipe.getFluidInput(), recipe.getGasOutputDefinition()});
      } else {
         return this.decompose(new Object[]{recipe.getGasInput(), recipe.getFluidOutputDefinition()});
      }
   }

   public Optional<RotaryRecipe> recompose(IRecipeManager<? super RotaryRecipe> m, ResourceLocation name, IDecomposedRecipe recipe) {
      if (m instanceof RotaryRecipeManager manager) {
         Optional<ChemicalStackIngredient.GasStackIngredient> gasInput = CrTUtils.getSingleIfPresent(recipe, CrTRecipeComponents.GAS.input());
         Optional<IFluidStack> fluidOutput = CrTUtils.getSingleIfPresent(recipe, CrTRecipeComponents.FLUID.output());
         if (gasInput.isPresent() != fluidOutput.isPresent()) {
            throw new IllegalArgumentException("Mismatched gas input and fluid output. Only one is present.");
         }

         Optional<FluidStackIngredient> fluidInput = CrTUtils.getSingleIfPresent(recipe, CrTRecipeComponents.FLUID.input());
         Optional<ICrTChemicalStack.ICrTGasStack> gasOutput = CrTUtils.getSingleIfPresent(recipe, CrTRecipeComponents.GAS.output());
         if (fluidInput.isPresent() != gasOutput.isPresent()) {
            throw new IllegalArgumentException("Mismatched fluid input and gas output. Only one is present.");
         }

         if (gasInput.isPresent()) {
            return fluidInput.<RotaryRecipe>map(
                  fluidIngredient -> manager.makeRecipe(name, fluidIngredient, gasInput.get(), gasOutput.get(), fluidOutput.get())
               )
               .or(() -> Optional.of(manager.makeRecipe(name, gasInput.get(), fluidOutput.get())));
         }

         if (fluidInput.isPresent()) {
            return Optional.of(manager.makeRecipe(name, fluidInput.get(), gasOutput.get()));
         }
      }

      return Optional.empty();
   }
}
