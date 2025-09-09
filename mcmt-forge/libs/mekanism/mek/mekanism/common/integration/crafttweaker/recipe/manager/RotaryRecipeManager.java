package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.RotaryIRecipe;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

@ZenRegister
@Name("mods.mekanism.recipe.manager.Rotary")
public class RotaryRecipeManager extends MekanismRecipeManager<RotaryRecipe> {
   public static final RotaryRecipeManager INSTANCE = new RotaryRecipeManager();

   private RotaryRecipeManager() {
      super(MekanismRecipeType.ROTARY);
   }

   @Method
   public void addRecipe(String name, FluidStackIngredient fluidInput, ICrTChemicalStack.ICrTGasStack gasOutput) {
      this.addRecipe(this.makeRecipe(this.getAndValidateName(name), fluidInput, gasOutput));
   }

   @Method
   public void addRecipe(String name, ChemicalStackIngredient.GasStackIngredient gasInput, IFluidStack fluidOutput) {
      this.addRecipe(this.makeRecipe(this.getAndValidateName(name), gasInput, fluidOutput));
   }

   @Method
   public void addRecipe(
      String name,
      FluidStackIngredient fluidInput,
      ChemicalStackIngredient.GasStackIngredient gasInput,
      ICrTChemicalStack.ICrTGasStack gasOutput,
      IFluidStack fluidOutput
   ) {
      this.addRecipe(this.makeRecipe(this.getAndValidateName(name), fluidInput, gasInput, gasOutput, fluidOutput));
   }

   public final RotaryRecipe makeRecipe(ResourceLocation id, FluidStackIngredient fluidInput, ICrTChemicalStack.ICrTGasStack gasOutput) {
      return new RotaryIRecipe(id, fluidInput, this.getAndValidateNotEmpty(gasOutput));
   }

   public final RotaryRecipe makeRecipe(ResourceLocation id, ChemicalStackIngredient.GasStackIngredient gasInput, IFluidStack fluidOutput) {
      return new RotaryIRecipe(id, gasInput, this.getAndValidateNotEmpty(fluidOutput));
   }

   public final RotaryRecipe makeRecipe(
      ResourceLocation id,
      FluidStackIngredient fluidInput,
      ChemicalStackIngredient.GasStackIngredient gasInput,
      ICrTChemicalStack.ICrTGasStack gasOutput,
      IFluidStack fluidOutput
   ) {
      return new RotaryIRecipe(id, fluidInput, gasInput, this.getAndValidateNotEmpty(gasOutput), this.getAndValidateNotEmpty(fluidOutput));
   }

   protected String describeOutputs(RotaryRecipe recipe) {
      StringBuilder builder = new StringBuilder();
      if (recipe.hasFluidToGas()) {
         builder.append(CrTUtils.describeOutputs(recipe.getGasOutputDefinition())).append(" for fluid to gas");
      }

      if (recipe.hasGasToFluid()) {
         if (recipe.hasFluidToGas()) {
            builder.append(" and ");
         }

         builder.append(CrTUtils.describeOutputs(recipe.getFluidOutputDefinition(), IFluidStack::of)).append(" for gas to fluid");
      }

      return builder.toString();
   }
}
