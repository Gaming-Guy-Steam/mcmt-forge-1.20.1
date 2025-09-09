package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.recipe.component.IDecomposedRecipe;
import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler.For;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import java.util.Optional;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.ChemicalInfuserRecipe;
import mekanism.api.recipes.PigmentMixingRecipe;
import mekanism.api.recipes.chemical.ChemicalChemicalToChemicalRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.integration.crafttweaker.recipe.manager.ChemicalChemicalToChemicalRecipeManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

public abstract class ChemicalChemicalToChemicalRecipeHandler<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK>, CRT_STACK extends ICrTChemicalStack<CHEMICAL, STACK, CRT_STACK>, RECIPE extends ChemicalChemicalToChemicalRecipe<CHEMICAL, STACK, INGREDIENT>>
   extends MekanismRecipeHandler<RECIPE> {
   public String dumpToCommandString(IRecipeManager<? super RECIPE> manager, RECIPE recipe) {
      return this.buildCommandString(manager, recipe, new Object[]{recipe.getLeftInput(), recipe.getRightInput(), recipe.getOutputDefinition()});
   }

   public <U extends Recipe<?>> boolean doesConflict(IRecipeManager<? super RECIPE> manager, RECIPE recipe, U other) {
      if (!this.recipeIsInstance(other)) {
         return false;
      } else {
         ChemicalChemicalToChemicalRecipe<?, ?, ?> otherRecipe = (ChemicalChemicalToChemicalRecipe<?, ?, ?>)other;
         return this.chemicalIngredientConflicts(recipe.getLeftInput(), otherRecipe.getLeftInput())
               && this.chemicalIngredientConflicts(recipe.getRightInput(), otherRecipe.getRightInput())
            || this.chemicalIngredientConflicts(recipe.getLeftInput(), otherRecipe.getRightInput())
               && this.chemicalIngredientConflicts(recipe.getRightInput(), otherRecipe.getLeftInput());
      }
   }

   public Optional<IDecomposedRecipe> decompose(IRecipeManager<? super RECIPE> manager, RECIPE recipe) {
      return this.decompose(new Object[]{recipe.getLeftInput(), recipe.getRightInput(), recipe.getOutputDefinition()});
   }

   public Optional<RECIPE> recompose(IRecipeManager<? super RECIPE> m, ResourceLocation name, IDecomposedRecipe recipe) {
      if (m instanceof ChemicalChemicalToChemicalRecipeManager<CHEMICAL, STACK, INGREDIENT, CRT_STACK, RECIPE> manager) {
         CrTUtils.UnaryTypePair<INGREDIENT> inputs = CrTUtils.getPair(recipe, this.getChemicalComponent().input());
         return Optional.of(manager.makeRecipe(name, inputs.a(), inputs.b(), (CRT_STACK)recipe.getOrThrowSingle(this.getChemicalComponent().output())));
      } else {
         return Optional.empty();
      }
   }

   protected abstract CrTRecipeComponents.ChemicalRecipeComponent<CHEMICAL, STACK, INGREDIENT, CRT_STACK> getChemicalComponent();

   protected abstract boolean recipeIsInstance(Recipe<?> other);

   @For(ChemicalInfuserRecipe.class)
   public static class ChemicalInfuserRecipeHandler
      extends ChemicalChemicalToChemicalRecipeHandler<Gas, GasStack, ChemicalStackIngredient.GasStackIngredient, ICrTChemicalStack.ICrTGasStack, ChemicalInfuserRecipe> {
      @Override
      protected CrTRecipeComponents.ChemicalRecipeComponent<Gas, GasStack, ChemicalStackIngredient.GasStackIngredient, ICrTChemicalStack.ICrTGasStack> getChemicalComponent() {
         return CrTRecipeComponents.GAS;
      }

      @Override
      protected boolean recipeIsInstance(Recipe<?> other) {
         return other instanceof ChemicalInfuserRecipe;
      }
   }

   @For(PigmentMixingRecipe.class)
   public static class PigmentMixingRecipeHandler
      extends ChemicalChemicalToChemicalRecipeHandler<Pigment, PigmentStack, ChemicalStackIngredient.PigmentStackIngredient, ICrTChemicalStack.ICrTPigmentStack, PigmentMixingRecipe> {
      @Override
      protected CrTRecipeComponents.ChemicalRecipeComponent<Pigment, PigmentStack, ChemicalStackIngredient.PigmentStackIngredient, ICrTChemicalStack.ICrTPigmentStack> getChemicalComponent() {
         return CrTRecipeComponents.PIGMENT;
      }

      @Override
      protected boolean recipeIsInstance(Recipe<?> other) {
         return other instanceof PigmentMixingRecipe;
      }
   }
}
