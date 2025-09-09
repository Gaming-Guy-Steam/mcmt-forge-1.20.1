package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeMethod;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import com.blamejared.crafttweaker_annotations.annotations.NativeMethod.Holder;
import java.util.List;
import mekanism.api.recipes.FluidSlurryToSlurryRecipe;
import mekanism.api.recipes.chemical.FluidChemicalToChemicalRecipe;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import org.openzen.zencode.java.ZenCodeType.Getter;
import org.openzen.zencode.java.ZenCodeType.Method;

@ZenRegister
@Holder({@NativeMethod(
      name = "getFluidInput",
      parameters = {},
      getterName = "fluidInput"
   ), @NativeMethod(
      name = "getChemicalInput",
      parameters = {},
      getterName = "chemicalInput"
   )})
@NativeTypeRegistration(
   value = FluidChemicalToChemicalRecipe.class,
   zenCodeName = "mods.mekanism.recipe.FluidChemicalToChemical"
)
public class CrTFluidChemicalToChemicalRecipe {
   private CrTFluidChemicalToChemicalRecipe() {
   }

   @ZenRegister
   @NativeTypeRegistration(
      value = FluidSlurryToSlurryRecipe.class,
      zenCodeName = "mods.mekanism.recipe.FluidChemicalToChemical.Slurry"
   )
   public static class CrTFluidSlurryToSlurryRecipe {
      private CrTFluidSlurryToSlurryRecipe() {
      }

      @Method
      @Getter("outputs")
      public static List<ICrTChemicalStack.ICrTSlurryStack> getOutputs(FluidSlurryToSlurryRecipe _this) {
         return CrTUtils.convertSlurry(_this.getOutputDefinition());
      }
   }
}
