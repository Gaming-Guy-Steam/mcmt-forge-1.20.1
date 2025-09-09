package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.recipe.component.IDecomposedRecipe;
import com.blamejared.crafttweaker.api.recipe.component.BuiltinRecipeComponents.Processing;
import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler.For;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import java.util.Optional;
import mekanism.api.recipes.NucleosynthesizingRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents;
import mekanism.common.integration.crafttweaker.recipe.manager.NucleosynthesizingRecipeManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

@For(NucleosynthesizingRecipe.class)
public class NucleosynthesizingRecipeHandler extends MekanismRecipeHandler<NucleosynthesizingRecipe> {
   public String dumpToCommandString(IRecipeManager<? super NucleosynthesizingRecipe> manager, NucleosynthesizingRecipe recipe) {
      return this.buildCommandString(
         manager, recipe, new Object[]{recipe.getItemInput(), recipe.getChemicalInput(), recipe.getOutputDefinition(), recipe.getDuration()}
      );
   }

   public <U extends Recipe<?>> boolean doesConflict(IRecipeManager<? super NucleosynthesizingRecipe> manager, NucleosynthesizingRecipe recipe, U o) {
      return !(o instanceof NucleosynthesizingRecipe other)
         ? false
         : this.ingredientConflicts(recipe.getItemInput(), other.getItemInput())
            && this.ingredientConflicts(recipe.getChemicalInput(), other.getChemicalInput());
   }

   public Optional<IDecomposedRecipe> decompose(IRecipeManager<? super NucleosynthesizingRecipe> manager, NucleosynthesizingRecipe recipe) {
      return this.decompose(new Object[]{recipe.getItemInput(), recipe.getChemicalInput(), recipe.getOutputDefinition(), recipe.getDuration()});
   }

   public Optional<NucleosynthesizingRecipe> recompose(IRecipeManager<? super NucleosynthesizingRecipe> m, ResourceLocation name, IDecomposedRecipe recipe) {
      return m instanceof NucleosynthesizingRecipeManager manager
         ? Optional.of(
            manager.makeRecipe(
               name,
               (ItemStackIngredient)recipe.getOrThrowSingle(CrTRecipeComponents.ITEM.input()),
               (ChemicalStackIngredient.GasStackIngredient)recipe.getOrThrowSingle(CrTRecipeComponents.GAS.input()),
               (IItemStack)recipe.getOrThrowSingle(CrTRecipeComponents.ITEM.output()),
               (Integer)recipe.getOrThrowSingle(Processing.TIME)
            )
         )
         : Optional.empty();
   }
}
