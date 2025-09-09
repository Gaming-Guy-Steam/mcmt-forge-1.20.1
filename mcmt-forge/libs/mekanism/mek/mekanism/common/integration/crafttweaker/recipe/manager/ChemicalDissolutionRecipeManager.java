package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ChemicalDissolutionRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.ChemicalDissolutionIRecipe;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

@ZenRegister
@Name("mods.mekanism.recipe.manager.Dissolution")
public class ChemicalDissolutionRecipeManager extends MekanismRecipeManager<ChemicalDissolutionRecipe> {
   public static final ChemicalDissolutionRecipeManager INSTANCE = new ChemicalDissolutionRecipeManager();

   private ChemicalDissolutionRecipeManager() {
      super(MekanismRecipeType.DISSOLUTION);
   }

   @Method
   public void addRecipe(String name, ItemStackIngredient itemInput, ChemicalStackIngredient.GasStackIngredient gasInput, ICrTChemicalStack<?, ?, ?> output) {
      this.addRecipe(this.makeRecipe(this.getAndValidateName(name), itemInput, gasInput, output));
   }

   public final ChemicalDissolutionRecipe makeRecipe(
      ResourceLocation id, ItemStackIngredient itemInput, ChemicalStackIngredient.GasStackIngredient gasInput, ICrTChemicalStack<?, ?, ?> output
   ) {
      return new ChemicalDissolutionIRecipe(id, itemInput, gasInput, this.getAndValidateNotEmpty((ICrTChemicalStack<?, ChemicalStack<?>, ?>)output));
   }

   protected String describeOutputs(ChemicalDissolutionRecipe recipe) {
      return CrTUtils.describeOutputs(recipe.getOutputDefinition(), stack -> {
         ICrTChemicalStack<?, ?, ?> output = CrTUtils.fromBoxedStack(stack);
         return output == null ? "unknown chemical output" : output.toString();
      });
   }
}
