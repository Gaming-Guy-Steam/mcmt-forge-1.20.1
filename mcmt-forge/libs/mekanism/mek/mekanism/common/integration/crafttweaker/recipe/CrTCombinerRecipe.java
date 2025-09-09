package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import java.util.List;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.CrTUtils;
import org.openzen.zencode.java.ZenCodeType.Getter;
import org.openzen.zencode.java.ZenCodeType.Method;

@ZenRegister
@NativeTypeRegistration(
   value = CombinerRecipe.class,
   zenCodeName = "mods.mekanism.recipe.Combining"
)
public class CrTCombinerRecipe {
   private CrTCombinerRecipe() {
   }

   @Method
   @Getter("mainInput")
   public static ItemStackIngredient getMainInput(CombinerRecipe _this) {
      return _this.getMainInput();
   }

   @Method
   @Getter("extraInput")
   public static ItemStackIngredient getExtraInput(CombinerRecipe _this) {
      return _this.getExtraInput();
   }

   @Method
   @Getter("outputs")
   public static List<IItemStack> getOutputs(CombinerRecipe _this) {
      return CrTUtils.convertItems(_this.getOutputDefinition());
   }
}
