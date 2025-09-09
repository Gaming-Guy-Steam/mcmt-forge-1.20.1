package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.recipes.ItemStackToInfuseTypeRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.InfusionConversionIRecipe;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType.Name;

@ZenRegister
@Name("mods.mekanism.recipe.manager.ItemStackToChemical.InfuseType")
public abstract class ItemStackToInfuseTypeRecipeManager
   extends ItemStackToChemicalRecipeManager<InfuseType, InfusionStack, ICrTChemicalStack.ICrTInfusionStack, ItemStackToInfuseTypeRecipe> {
   protected ItemStackToInfuseTypeRecipeManager(IMekanismRecipeTypeProvider<ItemStackToInfuseTypeRecipe, ?> recipeType) {
      super(recipeType);
   }

   @ZenRegister
   @Name("mods.mekanism.recipe.manager.ItemStackToChemical.InfuseType.InfusionConversion")
   public static class InfusionConversionRecipeManager extends ItemStackToInfuseTypeRecipeManager {
      public static final ItemStackToInfuseTypeRecipeManager.InfusionConversionRecipeManager INSTANCE = new ItemStackToInfuseTypeRecipeManager.InfusionConversionRecipeManager();

      private InfusionConversionRecipeManager() {
         super(MekanismRecipeType.INFUSION_CONVERSION);
      }

      protected ItemStackToInfuseTypeRecipe makeRecipe(ResourceLocation id, ItemStackIngredient input, InfusionStack output) {
         return new InfusionConversionIRecipe(id, input, output);
      }
   }
}
