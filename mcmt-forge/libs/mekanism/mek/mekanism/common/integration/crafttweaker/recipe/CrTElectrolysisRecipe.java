package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import java.util.List;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import org.openzen.zencode.java.ZenCodeType.Getter;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

@ZenRegister
@NativeTypeRegistration(
   value = ElectrolysisRecipe.class,
   zenCodeName = "mods.mekanism.recipe.Separating"
)
public class CrTElectrolysisRecipe {
   private CrTElectrolysisRecipe() {
   }

   @Method
   @Getter("input")
   public static FluidStackIngredient getInput(ElectrolysisRecipe _this) {
      return _this.getInput();
   }

   @Method
   @Getter("energyMultiplier")
   public static FloatingLong getEnergyMultiplier(ElectrolysisRecipe _this) {
      return _this.getEnergyMultiplier().copyAsConst();
   }

   @Method
   @Getter("outputs")
   public static List<CrTElectrolysisRecipe.CrTElectrolysisRecipeOutput> getOutputs(ElectrolysisRecipe _this) {
      return CrTUtils.convert(
         _this.getOutputDefinition(),
         output -> new CrTElectrolysisRecipe.CrTElectrolysisRecipeOutput(
            new CrTChemicalStack.CrTGasStack(output.left()), new CrTChemicalStack.CrTGasStack(output.right())
         )
      );
   }

   @ZenRegister
   @Name("mods.mekanism.recipe.Separating.Output")
   public record CrTElectrolysisRecipeOutput(@Getter("left") ICrTChemicalStack.ICrTGasStack left, @Getter("right") ICrTChemicalStack.ICrTGasStack right) {
   }
}
