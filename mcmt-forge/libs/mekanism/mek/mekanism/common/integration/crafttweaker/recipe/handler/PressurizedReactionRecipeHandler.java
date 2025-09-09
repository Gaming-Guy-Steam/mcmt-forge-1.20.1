package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.recipe.component.IDecomposedRecipe;
import com.blamejared.crafttweaker.api.recipe.component.BuiltinRecipeComponents.Processing;
import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler.For;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import java.util.List;
import java.util.Optional;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.integration.crafttweaker.recipe.manager.PressurizedReactionRecipeManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

@For(PressurizedReactionRecipe.class)
public class PressurizedReactionRecipeHandler extends MekanismRecipeHandler<PressurizedReactionRecipe> {
   public String dumpToCommandString(IRecipeManager<? super PressurizedReactionRecipe> manager, PressurizedReactionRecipe recipe) {
      List<PressurizedReactionRecipe.PressurizedReactionRecipeOutput> outputs = recipe.getOutputDefinition();
      ItemStack itemOutput;
      GasStack gasOutput;
      if (outputs.isEmpty()) {
         itemOutput = ItemStack.f_41583_;
         gasOutput = GasStack.EMPTY;
      } else {
         PressurizedReactionRecipe.PressurizedReactionRecipeOutput output = outputs.get(0);
         itemOutput = output.item();
         gasOutput = output.gas();
      }

      return this.buildCommandString(
         manager,
         recipe,
         new Object[]{
            recipe.getInputSolid(),
            recipe.getInputFluid(),
            recipe.getInputGas(),
            recipe.getDuration(),
            itemOutput.m_41619_() ? SKIP_OPTIONAL_PARAM : itemOutput,
            gasOutput.isEmpty() ? SKIP_OPTIONAL_PARAM : gasOutput,
            recipe.getEnergyRequired().isZero() ? SKIP_OPTIONAL_PARAM : recipe.getEnergyRequired()
         }
      );
   }

   public <U extends Recipe<?>> boolean doesConflict(IRecipeManager<? super PressurizedReactionRecipe> manager, PressurizedReactionRecipe recipe, U o) {
      return !(o instanceof PressurizedReactionRecipe other)
         ? false
         : this.ingredientConflicts(recipe.getInputSolid(), other.getInputSolid())
            && this.ingredientConflicts(recipe.getInputFluid(), other.getInputFluid())
            && this.ingredientConflicts(recipe.getInputGas(), other.getInputGas());
   }

   public Optional<IDecomposedRecipe> decompose(IRecipeManager<? super PressurizedReactionRecipe> manager, PressurizedReactionRecipe recipe) {
      return this.decompose(
         new Object[]{
            recipe.getInputSolid(),
            recipe.getInputFluid(),
            recipe.getInputGas(),
            recipe.getDuration(),
            recipe.getOutputDefinition(),
            recipe.getEnergyRequired()
         }
      );
   }

   public Optional<PressurizedReactionRecipe> recompose(IRecipeManager<? super PressurizedReactionRecipe> m, ResourceLocation name, IDecomposedRecipe recipe) {
      if (m instanceof PressurizedReactionRecipeManager manager) {
         Optional<IItemStack> optionalOutputItem = CrTUtils.getSingleIfPresent(recipe, CrTRecipeComponents.ITEM.output());
         ItemStack outputItem;
         GasStack outputGas;
         if (optionalOutputItem.isPresent()) {
            outputItem = optionalOutputItem.get().getImmutableInternal();
            outputGas = CrTUtils.getSingleIfPresent(recipe, CrTRecipeComponents.GAS.output())
               .map(ICrTChemicalStack::getImmutableInternal)
               .orElse(GasStack.EMPTY);
         } else {
            outputItem = ItemStack.f_41583_;
            outputGas = (GasStack)((ICrTChemicalStack.ICrTGasStack)recipe.getOrThrowSingle(CrTRecipeComponents.GAS.output())).getImmutableInternal();
         }

         return Optional.of(
            manager.makeRecipe(
               name,
               (ItemStackIngredient)recipe.getOrThrowSingle(CrTRecipeComponents.ITEM.input()),
               (FluidStackIngredient)recipe.getOrThrowSingle(CrTRecipeComponents.FLUID.input()),
               (ChemicalStackIngredient.GasStackIngredient)recipe.getOrThrowSingle(CrTRecipeComponents.GAS.input()),
               (Integer)recipe.getOrThrowSingle(Processing.TIME),
               outputItem,
               outputGas,
               CrTUtils.<FloatingLong>getSingleIfPresent(recipe, CrTRecipeComponents.ENERGY).orElse(FloatingLong.ZERO)
            )
         );
      } else {
         return Optional.empty();
      }
   }
}
