package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.chemical.ItemStackToChemicalRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

@ZenRegister
@Name("mods.mekanism.recipe.manager.ItemStackToChemical")
public abstract class ItemStackToChemicalRecipeManager<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, CRT_STACK extends ICrTChemicalStack<CHEMICAL, STACK, CRT_STACK>, RECIPE extends ItemStackToChemicalRecipe<CHEMICAL, STACK>>
   extends MekanismRecipeManager<RECIPE> {
   protected ItemStackToChemicalRecipeManager(IMekanismRecipeTypeProvider<RECIPE, ?> recipeType) {
      super(recipeType);
   }

   @Method
   public void addRecipe(String name, ItemStackIngredient input, CRT_STACK output) {
      this.addRecipe(this.makeRecipe(this.getAndValidateName(name), input, output));
   }

   public final RECIPE makeRecipe(ResourceLocation id, ItemStackIngredient input, CRT_STACK output) {
      return this.makeRecipe(id, input, this.getAndValidateNotEmpty(output));
   }

   protected abstract RECIPE makeRecipe(ResourceLocation id, ItemStackIngredient input, STACK output);

   protected String describeOutputs(RECIPE recipe) {
      return CrTUtils.describeOutputs(recipe.getOutputDefinition());
   }
}
