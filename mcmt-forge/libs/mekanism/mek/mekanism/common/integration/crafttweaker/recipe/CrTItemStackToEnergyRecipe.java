package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import java.util.List;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ItemStackToEnergyRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.CrTUtils;
import org.openzen.zencode.java.ZenCodeType.Getter;
import org.openzen.zencode.java.ZenCodeType.Method;

@ZenRegister
@NativeTypeRegistration(
   value = ItemStackToEnergyRecipe.class,
   zenCodeName = "mods.mekanism.recipe.ItemStackToEnergy"
)
public class CrTItemStackToEnergyRecipe {
   private CrTItemStackToEnergyRecipe() {
   }

   @Method
   @Getter("input")
   public static ItemStackIngredient getInput(ItemStackToEnergyRecipe _this) {
      return _this.getInput();
   }

   @Method
   @Getter("outputs")
   public static List<FloatingLong> getOutput(ItemStackToEnergyRecipe _this) {
      return CrTUtils.convert(_this.getOutputDefinition(), FloatingLong::copyAsConst);
   }
}
