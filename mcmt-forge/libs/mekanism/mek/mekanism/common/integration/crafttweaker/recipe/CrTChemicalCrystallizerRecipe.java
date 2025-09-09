package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import java.util.List;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.integration.crafttweaker.CrTUtils;
import org.openzen.zencode.java.ZenCodeType.Getter;
import org.openzen.zencode.java.ZenCodeType.Method;

@ZenRegister
@NativeTypeRegistration(
   value = ChemicalCrystallizerRecipe.class,
   zenCodeName = "mods.mekanism.recipe.Crystallizing"
)
public class CrTChemicalCrystallizerRecipe {
   private CrTChemicalCrystallizerRecipe() {
   }

   @Method
   @Getter("input")
   public static ChemicalStackIngredient<?, ?> getInput(ChemicalCrystallizerRecipe _this) {
      return _this.getInput();
   }

   @Method
   @Getter("outputs")
   public static List<IItemStack> getOutputs(ChemicalCrystallizerRecipe _this) {
      return CrTUtils.convertItems(_this.getOutputDefinition());
   }
}
