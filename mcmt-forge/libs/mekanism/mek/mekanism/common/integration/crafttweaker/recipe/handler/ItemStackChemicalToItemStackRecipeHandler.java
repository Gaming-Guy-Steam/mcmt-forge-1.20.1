package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.item.IItemStack;
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
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
import mekanism.api.recipes.PaintingRecipe;
import mekanism.api.recipes.chemical.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents;
import mekanism.common.integration.crafttweaker.recipe.manager.ItemStackChemicalToItemStackRecipeManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

public abstract class ItemStackChemicalToItemStackRecipeHandler<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK>, RECIPE extends ItemStackChemicalToItemStackRecipe<CHEMICAL, STACK, INGREDIENT>>
   extends MekanismRecipeHandler<RECIPE> {
   public String dumpToCommandString(IRecipeManager<? super RECIPE> manager, RECIPE recipe) {
      return this.buildCommandString(manager, recipe, new Object[]{recipe.getItemInput(), recipe.getChemicalInput(), recipe.getOutputDefinition()});
   }

   public <U extends Recipe<?>> boolean doesConflict(IRecipeManager<? super RECIPE> manager, RECIPE recipe, U o) {
      return !(o instanceof ItemStackChemicalToItemStackRecipe<?, ?, ?> other)
         ? false
         : this.chemicalIngredientConflicts(recipe.getChemicalInput(), other.getChemicalInput())
            && this.ingredientConflicts(recipe.getItemInput(), other.getItemInput());
   }

   public Optional<IDecomposedRecipe> decompose(IRecipeManager<? super RECIPE> manager, RECIPE recipe) {
      return this.decompose(new Object[]{recipe.getItemInput(), recipe.getChemicalInput(), recipe.getOutputDefinition()});
   }

   public Optional<RECIPE> recompose(IRecipeManager<? super RECIPE> m, ResourceLocation name, IDecomposedRecipe recipe) {
      return m instanceof ItemStackChemicalToItemStackRecipeManager<CHEMICAL, STACK, INGREDIENT, RECIPE> manager
         ? Optional.of(
            manager.makeRecipe(
               name,
               (ItemStackIngredient)recipe.getOrThrowSingle(CrTRecipeComponents.ITEM.input()),
               (INGREDIENT)recipe.getOrThrowSingle(this.getChemicalComponent().input()),
               (IItemStack)recipe.getOrThrowSingle(CrTRecipeComponents.ITEM.output())
            )
         )
         : Optional.empty();
   }

   protected abstract CrTRecipeComponents.ChemicalRecipeComponent<CHEMICAL, STACK, INGREDIENT, ?> getChemicalComponent();

   @For(ItemStackGasToItemStackRecipe.class)
   public static class ItemStackGasToItemStackRecipeHandler
      extends ItemStackChemicalToItemStackRecipeHandler<Gas, GasStack, ChemicalStackIngredient.GasStackIngredient, ItemStackGasToItemStackRecipe> {
      @Override
      protected CrTRecipeComponents.ChemicalRecipeComponent<Gas, GasStack, ChemicalStackIngredient.GasStackIngredient, ?> getChemicalComponent() {
         return CrTRecipeComponents.GAS;
      }
   }

   @For(MetallurgicInfuserRecipe.class)
   public static class MetallurgicInfuserRecipeHandler
      extends ItemStackChemicalToItemStackRecipeHandler<InfuseType, InfusionStack, ChemicalStackIngredient.InfusionStackIngredient, MetallurgicInfuserRecipe> {
      @Override
      protected CrTRecipeComponents.ChemicalRecipeComponent<InfuseType, InfusionStack, ChemicalStackIngredient.InfusionStackIngredient, ?> getChemicalComponent() {
         return CrTRecipeComponents.INFUSION;
      }
   }

   @For(PaintingRecipe.class)
   public static class PaintingRecipeHandler
      extends ItemStackChemicalToItemStackRecipeHandler<Pigment, PigmentStack, ChemicalStackIngredient.PigmentStackIngredient, PaintingRecipe> {
      @Override
      protected CrTRecipeComponents.ChemicalRecipeComponent<Pigment, PigmentStack, ChemicalStackIngredient.PigmentStackIngredient, ?> getChemicalComponent() {
         return CrTRecipeComponents.PIGMENT;
      }
   }
}
