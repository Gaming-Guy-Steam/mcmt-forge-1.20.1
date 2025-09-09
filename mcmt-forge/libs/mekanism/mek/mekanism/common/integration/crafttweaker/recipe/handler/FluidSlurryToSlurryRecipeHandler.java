package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.recipe.component.IDecomposedRecipe;
import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler.For;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import java.util.Optional;
import mekanism.api.recipes.FluidSlurryToSlurryRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.integration.crafttweaker.recipe.manager.FluidSlurryToSlurryRecipeManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

@For(FluidSlurryToSlurryRecipe.class)
public class FluidSlurryToSlurryRecipeHandler extends MekanismRecipeHandler<FluidSlurryToSlurryRecipe> {
   public String dumpToCommandString(IRecipeManager<? super FluidSlurryToSlurryRecipe> manager, FluidSlurryToSlurryRecipe recipe) {
      return this.buildCommandString(manager, recipe, new Object[]{recipe.getFluidInput(), recipe.getChemicalInput(), recipe.getOutputDefinition()});
   }

   public <U extends Recipe<?>> boolean doesConflict(IRecipeManager<? super FluidSlurryToSlurryRecipe> manager, FluidSlurryToSlurryRecipe recipe, U o) {
      return !(o instanceof FluidSlurryToSlurryRecipe other)
         ? false
         : this.ingredientConflicts(recipe.getFluidInput(), other.getFluidInput())
            && this.ingredientConflicts(recipe.getChemicalInput(), other.getChemicalInput());
   }

   public Optional<IDecomposedRecipe> decompose(IRecipeManager<? super FluidSlurryToSlurryRecipe> manager, FluidSlurryToSlurryRecipe recipe) {
      return this.decompose(new Object[]{recipe.getFluidInput(), recipe.getChemicalInput(), recipe.getOutputDefinition()});
   }

   public Optional<FluidSlurryToSlurryRecipe> recompose(IRecipeManager<? super FluidSlurryToSlurryRecipe> m, ResourceLocation name, IDecomposedRecipe recipe) {
      return m instanceof FluidSlurryToSlurryRecipeManager manager
         ? Optional.of(
            manager.makeRecipe(
               name,
               (FluidStackIngredient)recipe.getOrThrowSingle(CrTRecipeComponents.FLUID.input()),
               (ChemicalStackIngredient.SlurryStackIngredient)recipe.getOrThrowSingle(CrTRecipeComponents.SLURRY.input()),
               (ICrTChemicalStack.ICrTSlurryStack)recipe.getOrThrowSingle(CrTRecipeComponents.SLURRY.output())
            )
         )
         : Optional.empty();
   }
}
