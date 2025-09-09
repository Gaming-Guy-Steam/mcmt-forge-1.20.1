package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.recipe.component.IDecomposedRecipe;
import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler.For;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import java.util.Optional;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.api.recipes.ItemStackToInfuseTypeRecipe;
import mekanism.api.recipes.ItemStackToPigmentRecipe;
import mekanism.api.recipes.chemical.ItemStackToChemicalRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.integration.crafttweaker.recipe.manager.ItemStackToChemicalRecipeManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

public abstract class ItemStackToChemicalRecipeHandler<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, CRT_STACK extends ICrTChemicalStack<CHEMICAL, STACK, CRT_STACK>, RECIPE extends ItemStackToChemicalRecipe<CHEMICAL, STACK>>
   extends MekanismRecipeHandler<RECIPE> {
   public String dumpToCommandString(IRecipeManager<? super RECIPE> manager, RECIPE recipe) {
      return this.buildCommandString(manager, recipe, new Object[]{recipe.getInput(), recipe.getOutputDefinition()});
   }

   public <U extends Recipe<?>> boolean doesConflict(IRecipeManager<? super RECIPE> manager, RECIPE recipe, U other) {
      return this.recipeIsInstance(other) && this.ingredientConflicts(recipe.getInput(), ((ItemStackToChemicalRecipe)other).getInput());
   }

   public Optional<IDecomposedRecipe> decompose(IRecipeManager<? super RECIPE> manager, RECIPE recipe) {
      return this.decompose(new Object[]{recipe.getInput(), recipe.getOutputDefinition()});
   }

   public Optional<RECIPE> recompose(IRecipeManager<? super RECIPE> m, ResourceLocation name, IDecomposedRecipe recipe) {
      return m instanceof ItemStackToChemicalRecipeManager<CHEMICAL, STACK, CRT_STACK, RECIPE> manager
         ? Optional.of(
            manager.makeRecipe(
               name,
               (ItemStackIngredient)recipe.getOrThrowSingle(CrTRecipeComponents.ITEM.input()),
               (CRT_STACK)recipe.getOrThrowSingle(this.getChemicalComponent().output())
            )
         )
         : Optional.empty();
   }

   protected abstract CrTRecipeComponents.ChemicalRecipeComponent<CHEMICAL, STACK, ?, CRT_STACK> getChemicalComponent();

   protected abstract boolean recipeIsInstance(Recipe<?> other);

   @For(ItemStackToGasRecipe.class)
   public static class ItemStackToGasRecipeHandler
      extends ItemStackToChemicalRecipeHandler<Gas, GasStack, ICrTChemicalStack.ICrTGasStack, ItemStackToGasRecipe> {
      @Override
      protected CrTRecipeComponents.ChemicalRecipeComponent<Gas, GasStack, ?, ICrTChemicalStack.ICrTGasStack> getChemicalComponent() {
         return CrTRecipeComponents.GAS;
      }

      @Override
      protected boolean recipeIsInstance(Recipe<?> other) {
         return other instanceof ItemStackToGasRecipe;
      }
   }

   @For(ItemStackToInfuseTypeRecipe.class)
   public static class ItemStackToInfuseTypeRecipeHandler
      extends ItemStackToChemicalRecipeHandler<InfuseType, InfusionStack, ICrTChemicalStack.ICrTInfusionStack, ItemStackToInfuseTypeRecipe> {
      @Override
      protected CrTRecipeComponents.ChemicalRecipeComponent<InfuseType, InfusionStack, ?, ICrTChemicalStack.ICrTInfusionStack> getChemicalComponent() {
         return CrTRecipeComponents.INFUSION;
      }

      @Override
      protected boolean recipeIsInstance(Recipe<?> other) {
         return other instanceof ItemStackToInfuseTypeRecipe;
      }
   }

   @For(ItemStackToPigmentRecipe.class)
   public static class ItemStackToPigmentRecipeHandler
      extends ItemStackToChemicalRecipeHandler<Pigment, PigmentStack, ICrTChemicalStack.ICrTPigmentStack, ItemStackToPigmentRecipe> {
      @Override
      protected CrTRecipeComponents.ChemicalRecipeComponent<Pigment, PigmentStack, ?, ICrTChemicalStack.ICrTPigmentStack> getChemicalComponent() {
         return CrTRecipeComponents.PIGMENT;
      }

      @Override
      protected boolean recipeIsInstance(Recipe<?> other) {
         return other instanceof ItemStackToPigmentRecipe;
      }
   }
}
