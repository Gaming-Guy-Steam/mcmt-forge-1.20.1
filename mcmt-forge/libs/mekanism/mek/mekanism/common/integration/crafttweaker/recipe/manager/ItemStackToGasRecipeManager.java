package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.ChemicalOxidizerIRecipe;
import mekanism.common.recipe.impl.GasConversionIRecipe;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType.Name;

@ZenRegister
@Name("mods.mekanism.recipe.manager.ItemStackToChemical.Gas")
public abstract class ItemStackToGasRecipeManager extends ItemStackToChemicalRecipeManager<Gas, GasStack, ICrTChemicalStack.ICrTGasStack, ItemStackToGasRecipe> {
   protected ItemStackToGasRecipeManager(IMekanismRecipeTypeProvider<ItemStackToGasRecipe, ?> recipeType) {
      super(recipeType);
   }

   @ZenRegister
   @Name("mods.mekanism.recipe.manager.ItemStackToChemical.Gas.Oxidizing")
   public static class ChemicalOxidizerRecipeManager extends ItemStackToGasRecipeManager {
      public static final ItemStackToGasRecipeManager.ChemicalOxidizerRecipeManager INSTANCE = new ItemStackToGasRecipeManager.ChemicalOxidizerRecipeManager();

      private ChemicalOxidizerRecipeManager() {
         super(MekanismRecipeType.OXIDIZING);
      }

      protected ItemStackToGasRecipe makeRecipe(ResourceLocation id, ItemStackIngredient input, GasStack output) {
         return new ChemicalOxidizerIRecipe(id, input, output);
      }
   }

   @ZenRegister
   @Name("mods.mekanism.recipe.manager.ItemStackToChemical.Gas.GasConversion")
   public static class GasConversionRecipeManager extends ItemStackToGasRecipeManager {
      public static final ItemStackToGasRecipeManager.GasConversionRecipeManager INSTANCE = new ItemStackToGasRecipeManager.GasConversionRecipeManager();

      private GasConversionRecipeManager() {
         super(MekanismRecipeType.GAS_CONVERSION);
      }

      protected ItemStackToGasRecipe makeRecipe(ResourceLocation id, ItemStackIngredient input, GasStack output) {
         return new GasConversionIRecipe(id, input, output);
      }
   }
}
