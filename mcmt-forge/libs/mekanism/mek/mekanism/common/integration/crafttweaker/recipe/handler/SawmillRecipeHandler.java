package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.recipe.component.DecomposedRecipeBuilder;
import com.blamejared.crafttweaker.api.recipe.component.IDecomposedRecipe;
import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler.For;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import java.util.List;
import java.util.Optional;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.recipe.manager.SawmillRecipeManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

@For(SawmillRecipe.class)
public class SawmillRecipeHandler extends MekanismRecipeHandler<SawmillRecipe> {
   public String dumpToCommandString(IRecipeManager<? super SawmillRecipe> manager, SawmillRecipe recipe) {
      boolean hasSecondary = recipe.getSecondaryChance() > 0.0;
      List<ItemStack> mainOutputDefinition = recipe.getMainOutputDefinition();
      return this.buildCommandString(
         manager,
         recipe,
         new Object[]{
            recipe.getInput(),
            mainOutputDefinition.isEmpty() ? SKIP_OPTIONAL_PARAM : mainOutputDefinition,
            hasSecondary ? recipe.getSecondaryOutputDefinition() : SKIP_OPTIONAL_PARAM,
            hasSecondary ? recipe.getSecondaryChance() : SKIP_OPTIONAL_PARAM
         }
      );
   }

   public <U extends Recipe<?>> boolean doesConflict(IRecipeManager<? super SawmillRecipe> manager, SawmillRecipe recipe, U o) {
      return o instanceof SawmillRecipe other && this.ingredientConflicts(recipe.getInput(), other.getInput());
   }

   public Optional<IDecomposedRecipe> decompose(IRecipeManager<? super SawmillRecipe> manager, SawmillRecipe recipe) {
      List<ItemStack> mainOutputDefinition = recipe.getMainOutputDefinition();
      if (mainOutputDefinition.size() > 1) {
         return Optional.empty();
      } else {
         List<ItemStack> secondaryOutputDefinition = recipe.getSecondaryOutputDefinition();
         if (secondaryOutputDefinition.size() > 1 || secondaryOutputDefinition.isEmpty() == recipe.getSecondaryChance() > 0.0) {
            return Optional.empty();
         } else if (mainOutputDefinition.isEmpty() && secondaryOutputDefinition.isEmpty()) {
            return Optional.empty();
         } else {
            DecomposedRecipeBuilder builder = IDecomposedRecipe.builder().with(CrTRecipeComponents.ITEM.input(), recipe.getInput());
            if (mainOutputDefinition.isEmpty()) {
               builder.with(CrTRecipeComponents.ITEM.output(), CrTUtils.convertItems(secondaryOutputDefinition))
                  .with(CrTRecipeComponents.CHANCE, recipe.getSecondaryChance());
            } else if (secondaryOutputDefinition.isEmpty()) {
               builder.with(CrTRecipeComponents.ITEM.output(), CrTUtils.convertItems(mainOutputDefinition));
            } else {
               builder.with(CrTRecipeComponents.ITEM.output(), CrTUtils.convertItems(List.of(mainOutputDefinition.get(0), secondaryOutputDefinition.get(0))))
                  .with(CrTRecipeComponents.CHANCE, recipe.getSecondaryChance());
            }

            return Optional.of(builder.build());
         }
      }
   }

   public Optional<SawmillRecipe> recompose(IRecipeManager<? super SawmillRecipe> m, ResourceLocation name, IDecomposedRecipe recipe) {
      if (!(m instanceof SawmillRecipeManager manager)) {
         return Optional.empty();
      } else {
         ItemStackIngredient input = (ItemStackIngredient)recipe.getOrThrowSingle(CrTRecipeComponents.ITEM.input());
         List<IItemStack> outputs = recipe.get(CrTRecipeComponents.ITEM.output());
         if (outputs != null && !outputs.isEmpty() && outputs.size() <= 2) {
            double chance = CrTUtils.<Double>getSingleIfPresent(recipe, CrTRecipeComponents.CHANCE).orElse(0.0);
            if (chance == 0.0 && outputs.size() == 2) {
               throw new IllegalArgumentException("No chance of specified secondary output.");
            } else {
               IItemStack output = outputs.get(0);
               IItemStack secondaryOutput;
               if (outputs.size() == 1) {
                  if (chance > 1.0 && chance < 2.0) {
                     chance--;
                     secondaryOutput = output.copy();
                  } else {
                     secondaryOutput = IItemStack.empty();
                  }
               } else {
                  secondaryOutput = outputs.get(1);
               }

               if (!secondaryOutput.isEmpty()) {
                  return Optional.of(manager.makeRecipe(name, input, output, secondaryOutput, chance));
               } else {
                  return chance != 0.0 && chance != 1.0
                     ? Optional.of(manager.makeRecipe(name, input, output, chance))
                     : Optional.of(manager.makeRecipe(name, input, output));
               }
            }
         } else {
            throw new IllegalArgumentException("Incorrect number of outputs specified. Must be either one or two outputs, and have a secondary chance if two.");
         }
      }
   }
}
