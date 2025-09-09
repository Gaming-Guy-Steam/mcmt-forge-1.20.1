package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import java.util.List;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.CrTUtils;
import org.openzen.zencode.java.ZenCodeType.Getter;
import org.openzen.zencode.java.ZenCodeType.Method;

@ZenRegister
@NativeTypeRegistration(
   value = SawmillRecipe.class,
   zenCodeName = "mods.mekanism.recipe.Sawing"
)
public class CrTSawmillRecipe {
   private CrTSawmillRecipe() {
   }

   @Method
   @Getter("input")
   public static ItemStackIngredient getInput(SawmillRecipe _this) {
      return _this.getInput();
   }

   @Method
   @Getter("mainOutputs")
   public static List<IItemStack> getMainOutputs(SawmillRecipe _this) {
      return CrTUtils.convertItems(_this.getMainOutputDefinition());
   }

   @Method
   @Getter("secondaryOutputs")
   public static List<IItemStack> getSecondaryOutputs(SawmillRecipe _this) {
      return CrTUtils.convertItems(_this.getSecondaryOutputDefinition());
   }

   @Method
   @Getter("secondaryChance")
   public static double getSecondaryChance(SawmillRecipe _this) {
      return _this.getSecondaryChance();
   }
}
