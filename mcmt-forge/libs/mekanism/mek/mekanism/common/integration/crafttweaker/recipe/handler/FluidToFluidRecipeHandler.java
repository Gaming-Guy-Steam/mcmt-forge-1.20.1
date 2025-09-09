package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.recipe.component.IDecomposedRecipe;
import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler.For;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import java.util.Optional;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents;
import mekanism.common.integration.crafttweaker.recipe.manager.FluidToFluidRecipeManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

@For(FluidToFluidRecipe.class)
public class FluidToFluidRecipeHandler extends MekanismRecipeHandler<FluidToFluidRecipe> {
   public String dumpToCommandString(IRecipeManager<? super FluidToFluidRecipe> manager, FluidToFluidRecipe recipe) {
      return this.buildCommandString(manager, recipe, new Object[]{recipe.getInput(), recipe.getOutputDefinition()});
   }

   public <U extends Recipe<?>> boolean doesConflict(IRecipeManager<? super FluidToFluidRecipe> manager, FluidToFluidRecipe recipe, U o) {
      return o instanceof FluidToFluidRecipe other && this.ingredientConflicts(recipe.getInput(), other.getInput());
   }

   public Optional<IDecomposedRecipe> decompose(IRecipeManager<? super FluidToFluidRecipe> manager, FluidToFluidRecipe recipe) {
      return this.decompose(new Object[]{recipe.getInput(), recipe.getOutputDefinition()});
   }

   public Optional<FluidToFluidRecipe> recompose(IRecipeManager<? super FluidToFluidRecipe> m, ResourceLocation name, IDecomposedRecipe recipe) {
      return m instanceof FluidToFluidRecipeManager manager
         ? Optional.of(
            manager.makeRecipe(
               name,
               (FluidStackIngredient)recipe.getOrThrowSingle(CrTRecipeComponents.FLUID.input()),
               (IFluidStack)recipe.getOrThrowSingle(CrTRecipeComponents.FLUID.output())
            )
         )
         : Optional.empty();
   }
}
