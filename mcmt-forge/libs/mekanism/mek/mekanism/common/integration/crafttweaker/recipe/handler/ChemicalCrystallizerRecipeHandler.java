package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.recipe.component.IDecomposedRecipe;
import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler.For;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import java.util.Optional;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.recipe.manager.ChemicalCrystallizerRecipeManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

@For(ChemicalCrystallizerRecipe.class)
public class ChemicalCrystallizerRecipeHandler extends MekanismRecipeHandler<ChemicalCrystallizerRecipe> {
   public String dumpToCommandString(IRecipeManager<? super ChemicalCrystallizerRecipe> manager, ChemicalCrystallizerRecipe recipe) {
      return this.buildCommandString(manager, recipe, new Object[]{recipe.getInput(), recipe.getOutputDefinition()});
   }

   public <U extends Recipe<?>> boolean doesConflict(IRecipeManager<? super ChemicalCrystallizerRecipe> manager, ChemicalCrystallizerRecipe recipe, U o) {
      return o instanceof ChemicalCrystallizerRecipe other && this.chemicalIngredientConflicts(recipe.getInput(), other.getInput());
   }

   public Optional<IDecomposedRecipe> decompose(IRecipeManager<? super ChemicalCrystallizerRecipe> manager, ChemicalCrystallizerRecipe recipe) {
      return this.decompose(new Object[]{recipe.getInput(), recipe.getOutputDefinition()});
   }

   public Optional<ChemicalCrystallizerRecipe> recompose(IRecipeManager<? super ChemicalCrystallizerRecipe> m, ResourceLocation name, IDecomposedRecipe recipe) {
      if (m instanceof ChemicalCrystallizerRecipeManager manager) {
         ChemicalStackIngredient<?, ?> input = CrTRecipeComponents.CHEMICAL_COMPONENTS
            .stream()
            .map(chemicalComponent -> CrTUtils.getSingleIfPresent(recipe, chemicalComponent.input()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No chemical input ingredient provided."));
         return Optional.of(manager.makeRecipe(name, input, (IItemStack)recipe.getOrThrowSingle(CrTRecipeComponents.ITEM.output())));
      } else {
         return Optional.empty();
      }
   }
}
