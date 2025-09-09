package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import java.util.List;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.CrTUtils;
import org.openzen.zencode.java.ZenCodeType.Getter;
import org.openzen.zencode.java.ZenCodeType.Method;

@ZenRegister
@NativeTypeRegistration(
   value = ItemStackToItemStackRecipe.class,
   zenCodeName = "mods.mekanism.recipe.ItemStackToItemStack"
)
public class CrTItemStackToItemStackRecipe {
   private CrTItemStackToItemStackRecipe() {
   }

   @Method
   @Getter("input")
   public static ItemStackIngredient getInput(ItemStackToItemStackRecipe _this) {
      return _this.getInput();
   }

   @Method
   @Getter("outputs")
   public static List<IItemStack> getOutputs(ItemStackToItemStackRecipe _this) {
      return CrTUtils.convertItems(_this.getOutputDefinition());
   }
}
