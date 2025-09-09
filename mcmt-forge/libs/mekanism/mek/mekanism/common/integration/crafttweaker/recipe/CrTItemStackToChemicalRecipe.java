package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeMethod;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import java.util.List;
import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.api.recipes.ItemStackToInfuseTypeRecipe;
import mekanism.api.recipes.ItemStackToPigmentRecipe;
import mekanism.api.recipes.chemical.ItemStackToChemicalRecipe;
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
   value = ItemStackToChemicalRecipe.class,
   zenCodeName = "mods.mekanism.recipe.ItemStackToChemical"
)
public class CrTItemStackToChemicalRecipe {
   private CrTItemStackToChemicalRecipe() {
   }

   @ZenRegister
   @NativeTypeRegistration(
      value = ItemStackToGasRecipe.class,
      zenCodeName = "mods.mekanism.recipe.ItemStackToChemical.ItemStackToGas"
   )
   public static class CrTItemStackToGasRecipe {
      private CrTItemStackToGasRecipe() {
      }

      @Method
      @Getter("outputs")
      public static List<ICrTChemicalStack.ICrTGasStack> getOutputs(ItemStackToGasRecipe _this) {
         return CrTUtils.convertGas(_this.getOutputDefinition());
      }
   }

   @ZenRegister
   @NativeTypeRegistration(
      value = ItemStackToInfuseTypeRecipe.class,
      zenCodeName = "mods.mekanism.recipe.ItemStackToChemical.ItemStackToInfuseType"
   )
   public static class CrTItemStackToInfuseTypeRecipe {
      private CrTItemStackToInfuseTypeRecipe() {
      }

      @Method
      @Getter("outputs")
      public static List<ICrTChemicalStack.ICrTInfusionStack> getOutputs(ItemStackToInfuseTypeRecipe _this) {
         return CrTUtils.convertInfusion(_this.getOutputDefinition());
      }
   }

   @ZenRegister
   @NativeTypeRegistration(
      value = ItemStackToPigmentRecipe.class,
      zenCodeName = "mods.mekanism.recipe.ItemStackToChemical.ItemStackToPigment"
   )
   public static class CrTItemStackToPigmentRecipe {
      private CrTItemStackToPigmentRecipe() {
      }

      @Method
      @Getter("outputs")
      public static List<ICrTChemicalStack.ICrTPigmentStack> getOutputs(ItemStackToPigmentRecipe _this) {
         return CrTUtils.convertPigment(_this.getOutputDefinition());
      }
   }
}
