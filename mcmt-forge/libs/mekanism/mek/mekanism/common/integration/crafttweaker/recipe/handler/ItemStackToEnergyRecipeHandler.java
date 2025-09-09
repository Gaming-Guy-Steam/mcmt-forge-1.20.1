package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.recipe.component.IDecomposedRecipe;
import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler.For;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import java.util.Optional;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ItemStackToEnergyRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents;
import mekanism.common.integration.crafttweaker.recipe.manager.ItemStackToEnergyRecipeManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

@For(ItemStackToEnergyRecipe.class)
public class ItemStackToEnergyRecipeHandler extends MekanismRecipeHandler<ItemStackToEnergyRecipe> {
   public String dumpToCommandString(IRecipeManager<? super ItemStackToEnergyRecipe> manager, ItemStackToEnergyRecipe recipe) {
      return this.buildCommandString(manager, recipe, new Object[]{recipe.getInput(), recipe.getOutputDefinition()});
   }

   public <U extends Recipe<?>> boolean doesConflict(IRecipeManager<? super ItemStackToEnergyRecipe> manager, ItemStackToEnergyRecipe recipe, U o) {
      return o instanceof ItemStackToEnergyRecipe other && this.ingredientConflicts(recipe.getInput(), other.getInput());
   }

   public Optional<IDecomposedRecipe> decompose(IRecipeManager<? super ItemStackToEnergyRecipe> manager, ItemStackToEnergyRecipe recipe) {
      return this.decompose(new Object[]{recipe.getInput(), recipe.getOutputDefinition()});
   }

   public Optional<ItemStackToEnergyRecipe> recompose(IRecipeManager<? super ItemStackToEnergyRecipe> m, ResourceLocation name, IDecomposedRecipe recipe) {
      return m instanceof ItemStackToEnergyRecipeManager manager
         ? Optional.of(
            manager.makeRecipe(
               name,
               (ItemStackIngredient)recipe.getOrThrowSingle(CrTRecipeComponents.ITEM.input()),
               (FloatingLong)recipe.getOrThrowSingle(CrTRecipeComponents.ENERGY)
            )
         )
         : Optional.empty();
   }
}
