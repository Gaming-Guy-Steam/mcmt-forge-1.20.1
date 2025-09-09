package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.ChemicalInfuserRecipe;
import mekanism.api.recipes.PigmentMixingRecipe;
import mekanism.api.recipes.chemical.ChemicalChemicalToChemicalRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.ChemicalInfuserIRecipe;
import mekanism.common.recipe.impl.PigmentMixingIRecipe;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

@ZenRegister
@Name("mods.mekanism.recipe.manager.ChemicalChemicalToChemical")
public abstract class ChemicalChemicalToChemicalRecipeManager<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK>, CRT_STACK extends ICrTChemicalStack<CHEMICAL, STACK, CRT_STACK>, RECIPE extends ChemicalChemicalToChemicalRecipe<CHEMICAL, STACK, INGREDIENT>>
   extends MekanismRecipeManager<RECIPE> {
   protected ChemicalChemicalToChemicalRecipeManager(IMekanismRecipeTypeProvider<RECIPE, ?> recipeType) {
      super(recipeType);
   }

   @Method
   public void addRecipe(String name, INGREDIENT leftInput, INGREDIENT rightInput, CRT_STACK output) {
      this.addRecipe(this.makeRecipe(this.getAndValidateName(name), leftInput, rightInput, output));
   }

   public final RECIPE makeRecipe(ResourceLocation id, INGREDIENT leftInput, INGREDIENT rightInput, CRT_STACK output) {
      return this.makeRecipe(id, leftInput, rightInput, this.getAndValidateNotEmpty(output));
   }

   protected abstract RECIPE makeRecipe(ResourceLocation id, INGREDIENT leftInput, INGREDIENT rightInput, STACK output);

   protected String describeOutputs(RECIPE recipe) {
      return CrTUtils.describeOutputs(recipe.getOutputDefinition());
   }

   @ZenRegister
   @Name("mods.mekanism.recipe.manager.ChemicalChemicalToChemical.ChemicalInfusing")
   public static class ChemicalInfuserRecipeManager
      extends ChemicalChemicalToChemicalRecipeManager<Gas, GasStack, ChemicalStackIngredient.GasStackIngredient, ICrTChemicalStack.ICrTGasStack, ChemicalInfuserRecipe> {
      public static final ChemicalChemicalToChemicalRecipeManager.ChemicalInfuserRecipeManager INSTANCE = new ChemicalChemicalToChemicalRecipeManager.ChemicalInfuserRecipeManager();

      private ChemicalInfuserRecipeManager() {
         super(MekanismRecipeType.CHEMICAL_INFUSING);
      }

      protected ChemicalInfuserRecipe makeRecipe(
         ResourceLocation id, ChemicalStackIngredient.GasStackIngredient left, ChemicalStackIngredient.GasStackIngredient right, GasStack output
      ) {
         return new ChemicalInfuserIRecipe(id, left, right, output);
      }
   }

   @ZenRegister
   @Name("mods.mekanism.recipe.manager.ChemicalChemicalToChemical.PigmentMixing")
   public static class PigmentMixingRecipeManager
      extends ChemicalChemicalToChemicalRecipeManager<Pigment, PigmentStack, ChemicalStackIngredient.PigmentStackIngredient, ICrTChemicalStack.ICrTPigmentStack, PigmentMixingRecipe> {
      public static final ChemicalChemicalToChemicalRecipeManager.PigmentMixingRecipeManager INSTANCE = new ChemicalChemicalToChemicalRecipeManager.PigmentMixingRecipeManager();

      private PigmentMixingRecipeManager() {
         super(MekanismRecipeType.PIGMENT_MIXING);
      }

      protected PigmentMixingRecipe makeRecipe(
         ResourceLocation id, ChemicalStackIngredient.PigmentStackIngredient left, ChemicalStackIngredient.PigmentStackIngredient right, PigmentStack output
      ) {
         return new PigmentMixingIRecipe(id, left, right, output);
      }
   }
}
