package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.recipe.component.IDecomposedRecipe;
import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler.For;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import java.util.Optional;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents;
import mekanism.common.integration.crafttweaker.recipe.manager.ItemStackToItemStackRecipeManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

@For(ItemStackToItemStackRecipe.class)
public class ItemStackToItemStackRecipeHandler extends MekanismRecipeHandler<ItemStackToItemStackRecipe> {
   public String dumpToCommandString(IRecipeManager<? super ItemStackToItemStackRecipe> manager, ItemStackToItemStackRecipe recipe) {
      return this.buildCommandString(manager, recipe, new Object[]{recipe.getInput(), recipe.getOutputDefinition()});
   }

   public <U extends Recipe<?>> boolean doesConflict(IRecipeManager<? super ItemStackToItemStackRecipe> manager, ItemStackToItemStackRecipe recipe, U o) {
      return o instanceof ItemStackToItemStackRecipe other && this.ingredientConflicts(recipe.getInput(), other.getInput());
   }

   public Optional<IDecomposedRecipe> decompose(IRecipeManager<? super ItemStackToItemStackRecipe> manager, ItemStackToItemStackRecipe recipe) {
      return this.decompose(new Object[]{recipe.getInput(), recipe.getOutputDefinition()});
   }

   public Optional<ItemStackToItemStackRecipe> recompose(IRecipeManager<? super ItemStackToItemStackRecipe> m, ResourceLocation name, IDecomposedRecipe recipe) {
      return m instanceof ItemStackToItemStackRecipeManager manager
         ? Optional.of(
            manager.makeRecipe(
               name,
               (ItemStackIngredient)recipe.getOrThrowSingle(CrTRecipeComponents.ITEM.input()),
               (IItemStack)recipe.getOrThrowSingle(CrTRecipeComponents.ITEM.output())
            )
         )
         : Optional.empty();
   }
}
