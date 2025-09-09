package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeMethod;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import com.blamejared.crafttweaker_annotations.annotations.NativeMethod.Holder;
import java.util.List;
import mekanism.api.recipes.ChemicalInfuserRecipe;
import mekanism.api.recipes.PigmentMixingRecipe;
import mekanism.api.recipes.chemical.ChemicalChemicalToChemicalRecipe;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import org.openzen.zencode.java.ZenCodeType.Getter;
import org.openzen.zencode.java.ZenCodeType.Method;

@ZenRegister
@Holder({@NativeMethod(
      name = "getLeftInput",
      parameters = {},
      getterName = "leftInput"
   ), @NativeMethod(
      name = "getRightInput",
      parameters = {},
      getterName = "rightInput"
   )})
@NativeTypeRegistration(
   value = ChemicalChemicalToChemicalRecipe.class,
   zenCodeName = "mods.mekanism.recipe.ChemicalChemicalToChemical"
)
public class CrTChemicalChemicalToChemicalRecipe {
   private CrTChemicalChemicalToChemicalRecipe() {
   }

   @ZenRegister
   @NativeTypeRegistration(
      value = ChemicalInfuserRecipe.class,
      zenCodeName = "mods.mekanism.recipe.ChemicalChemicalToChemical.ChemicalInfusing"
   )
   public static class CrTChemicalInfuserRecipe {
      private CrTChemicalInfuserRecipe() {
      }

      @Method
      @Getter("outputs")
      public static List<ICrTChemicalStack.ICrTGasStack> getOutputs(ChemicalInfuserRecipe _this) {
         return CrTUtils.convertGas(_this.getOutputDefinition());
      }
   }

   @ZenRegister
   @NativeTypeRegistration(
      value = PigmentMixingRecipe.class,
      zenCodeName = "mods.mekanism.recipe.PigmentMixing"
   )
   public static class CrTPigmentMixingRecipe {
      private CrTPigmentMixingRecipe() {
      }

      @Method
      @Getter("outputs")
      public static List<ICrTChemicalStack.ICrTPigmentStack> getOutputs(PigmentMixingRecipe _this) {
         return CrTUtils.convertPigment(_this.getOutputDefinition());
      }
   }
}
