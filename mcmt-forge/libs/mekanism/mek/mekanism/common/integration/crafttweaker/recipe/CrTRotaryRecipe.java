package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import java.util.List;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import org.openzen.zencode.java.ZenCodeType.Getter;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;
import org.openzen.zencode.java.ZenCodeType.Nullable;

@ZenRegister
@NativeTypeRegistration(
   value = RotaryRecipe.class,
   zenCodeName = "mods.mekanism.recipe.Rotary"
)
public class CrTRotaryRecipe {
   private CrTRotaryRecipe() {
   }

   @Method
   @Getter("gasToFluid")
   @Nullable
   public static CrTRotaryRecipe.GasToFluid getGasToFluid(RotaryRecipe _this) {
      return _this.hasGasToFluid() ? new CrTRotaryRecipe.GasToFluid(_this.getGasInput(), CrTUtils.convertFluids(_this.getFluidOutputDefinition())) : null;
   }

   @Method
   @Getter("fluidToGas")
   @Nullable
   public static CrTRotaryRecipe.FluidToGas getFluidToGas(RotaryRecipe _this) {
      return _this.hasFluidToGas() ? new CrTRotaryRecipe.FluidToGas(_this.getFluidInput(), CrTUtils.convertGas(_this.getGasOutputDefinition())) : null;
   }

   @ZenRegister
   @Name("mods.mekanism.recipe.Rotary.FluidToGas")
   public record FluidToGas(@Getter("input") FluidStackIngredient input, @Getter("outputs") List<ICrTChemicalStack.ICrTGasStack> outputs) {
   }

   @ZenRegister
   @Name("mods.mekanism.recipe.Rotary.GasToFluid")
   public record GasToFluid(@Getter("input") ChemicalStackIngredient.GasStackIngredient input, @Getter("outputs") List<IFluidStack> outputs) {
   }
}
