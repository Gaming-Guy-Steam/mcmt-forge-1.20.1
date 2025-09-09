package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.util.ItemStackUtil;
import mekanism.api.recipes.NucleosynthesizingRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.NucleosynthesizingIRecipe;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

@ZenRegister
@Name("mods.mekanism.recipe.manager.Nucleosynthesizing")
public class NucleosynthesizingRecipeManager extends MekanismRecipeManager<NucleosynthesizingRecipe> {
   public static final NucleosynthesizingRecipeManager INSTANCE = new NucleosynthesizingRecipeManager();

   private NucleosynthesizingRecipeManager() {
      super(MekanismRecipeType.NUCLEOSYNTHESIZING);
   }

   @Method
   public void addRecipe(String name, ItemStackIngredient itemInput, ChemicalStackIngredient.GasStackIngredient gasInput, IItemStack output, int duration) {
      this.addRecipe(this.makeRecipe(this.getAndValidateName(name), itemInput, gasInput, output, duration));
   }

   public final NucleosynthesizingRecipe makeRecipe(
      ResourceLocation id, ItemStackIngredient itemInput, ChemicalStackIngredient.GasStackIngredient gasInput, IItemStack output, int duration
   ) {
      if (duration <= 0) {
         throw new IllegalArgumentException("Duration must be a number greater than zero! Duration: " + duration);
      } else {
         return new NucleosynthesizingIRecipe(id, itemInput, gasInput, this.getAndValidateNotEmpty(output), duration);
      }
   }

   protected String describeOutputs(NucleosynthesizingRecipe recipe) {
      return CrTUtils.describeOutputs(recipe.getOutputDefinition(), ItemStackUtil::getCommandString);
   }
}
