package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import java.util.List;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.common.integration.crafttweaker.CrTUtils;
import org.openzen.zencode.java.ZenCodeType.Getter;
import org.openzen.zencode.java.ZenCodeType.Method;

@ZenRegister
@NativeTypeRegistration(
   value = FluidToFluidRecipe.class,
   zenCodeName = "mods.mekanism.recipe.FluidToFluid"
)
public class CrTFluidToFluidRecipe {
   private CrTFluidToFluidRecipe() {
   }

   @Method
   @Getter("input")
   public static FluidStackIngredient getInput(FluidToFluidRecipe _this) {
      return _this.getInput();
   }

   @Method
   @Getter("outputs")
   public static List<IFluidStack> getOutputs(FluidToFluidRecipe _this) {
      return CrTUtils.convertFluids(_this.getOutputDefinition());
   }
}
