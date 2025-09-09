package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.ItemStackToPigmentRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.PigmentExtractingIRecipe;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType.Name;

@ZenRegister
@Name("mods.mekanism.recipe.manager.ItemStackToChemical.Pigment")
public abstract class ItemStackToPigmentRecipeManager
   extends ItemStackToChemicalRecipeManager<Pigment, PigmentStack, ICrTChemicalStack.ICrTPigmentStack, ItemStackToPigmentRecipe> {
   protected ItemStackToPigmentRecipeManager(IMekanismRecipeTypeProvider<ItemStackToPigmentRecipe, ?> recipeType) {
      super(recipeType);
   }

   @ZenRegister
   @Name("mods.mekanism.recipe.manager.ItemStackToChemical.Pigment.PigmentExtracting")
   public static class PigmentExtractingRecipeManager extends ItemStackToPigmentRecipeManager {
      public static final ItemStackToPigmentRecipeManager.PigmentExtractingRecipeManager INSTANCE = new ItemStackToPigmentRecipeManager.PigmentExtractingRecipeManager();

      private PigmentExtractingRecipeManager() {
         super(MekanismRecipeType.PIGMENT_EXTRACTING);
      }

      protected ItemStackToPigmentRecipe makeRecipe(ResourceLocation id, ItemStackIngredient input, PigmentStack output) {
         return new PigmentExtractingIRecipe(id, input, output);
      }
   }
}
