package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.ElectrolysisIRecipe;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

@ZenRegister
@Name("mods.mekanism.recipe.manager.Separating")
public class ElectrolysisRecipeManager extends MekanismRecipeManager<ElectrolysisRecipe> {
   public static final ElectrolysisRecipeManager INSTANCE = new ElectrolysisRecipeManager();

   private ElectrolysisRecipeManager() {
      super(MekanismRecipeType.SEPARATING);
   }

   @Method
   public void addRecipe(
      String name,
      FluidStackIngredient input,
      ICrTChemicalStack.ICrTGasStack leftGasOutput,
      ICrTChemicalStack.ICrTGasStack rightGasOutput,
      FloatingLong energyMultiplier
   ) {
      this.addRecipe(this.makeRecipe(this.getAndValidateName(name), input, leftGasOutput, rightGasOutput, energyMultiplier));
   }

   @Method
   public void addRecipe(String name, FluidStackIngredient input, ICrTChemicalStack.ICrTGasStack leftGasOutput, ICrTChemicalStack.ICrTGasStack rightGasOutput) {
      this.addRecipe(this.makeRecipe(this.getAndValidateName(name), input, leftGasOutput, rightGasOutput, FloatingLong.ONE));
   }

   public final ElectrolysisRecipe makeRecipe(
      ResourceLocation id,
      FluidStackIngredient input,
      ICrTChemicalStack.ICrTGasStack leftGasOutput,
      ICrTChemicalStack.ICrTGasStack rightGasOutput,
      FloatingLong energyMultiplier
   ) {
      if (energyMultiplier.smallerThan(FloatingLong.ONE)) {
         throw new IllegalArgumentException("Energy multiplier must be at least one! Multiplier: " + energyMultiplier);
      } else {
         return new ElectrolysisIRecipe(
            id, input, energyMultiplier.copyAsConst(), this.getAndValidateNotEmpty(leftGasOutput), this.getAndValidateNotEmpty(rightGasOutput)
         );
      }
   }

   protected String describeOutputs(ElectrolysisRecipe recipe) {
      return CrTUtils.describeOutputs(
         recipe.getOutputDefinition(), output -> new CrTChemicalStack.CrTGasStack(output.left()) + " and " + new CrTChemicalStack.CrTGasStack(output.right())
      );
   }
}
