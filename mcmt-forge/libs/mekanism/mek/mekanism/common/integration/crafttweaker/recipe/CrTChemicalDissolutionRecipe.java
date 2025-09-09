package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import java.util.List;
import java.util.Objects;
import mekanism.api.recipes.ChemicalDissolutionRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import org.openzen.zencode.java.ZenCodeType.Getter;
import org.openzen.zencode.java.ZenCodeType.Method;

@ZenRegister
@NativeTypeRegistration(
   value = ChemicalDissolutionRecipe.class,
   zenCodeName = "mods.mekanism.recipe.Dissolution"
)
public class CrTChemicalDissolutionRecipe {
   private CrTChemicalDissolutionRecipe() {
   }

   @Method
   @Getter("itemInput")
   public static ItemStackIngredient getItemInput(ChemicalDissolutionRecipe _this) {
      return _this.getItemInput();
   }

   @Method
   @Getter("gasInput")
   public static ChemicalStackIngredient.GasStackIngredient getGasInput(ChemicalDissolutionRecipe _this) {
      return _this.getGasInput();
   }

   @Method
   @Getter("outputs")
   public static List<ICrTChemicalStack<?, ?, ?>> getOutputs(ChemicalDissolutionRecipe _this) {
      return _this.getOutputDefinition().stream().map(CrTUtils::fromBoxedStack).filter(Objects::nonNull).toList();
   }
}
