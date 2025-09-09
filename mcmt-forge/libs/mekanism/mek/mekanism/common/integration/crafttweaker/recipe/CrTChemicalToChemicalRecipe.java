package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeMethod;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import java.util.List;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.api.recipes.chemical.ChemicalToChemicalRecipe;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import org.openzen.zencode.java.ZenCodeType.Getter;
import org.openzen.zencode.java.ZenCodeType.Method;

@ZenRegister
@NativeMethod(
   name = "getInput",
   parameters = {},
   getterName = "input"
)
@NativeTypeRegistration(
   value = ChemicalToChemicalRecipe.class,
   zenCodeName = "mods.mekanism.recipe.ChemicalToChemical"
)
public class CrTChemicalToChemicalRecipe {
   private CrTChemicalToChemicalRecipe() {
   }

   @ZenRegister
   @NativeTypeRegistration(
      value = GasToGasRecipe.class,
      zenCodeName = "mods.mekanism.recipe.ChemicalToChemical.GasToGas"
   )
   public static class CrTGasToGasRecipe {
      private CrTGasToGasRecipe() {
      }

      @Method
      @Getter("outputs")
      public static List<ICrTChemicalStack.ICrTGasStack> getOutputs(GasToGasRecipe _this) {
         return CrTUtils.convertGas(_this.getOutputDefinition());
      }
   }
}
