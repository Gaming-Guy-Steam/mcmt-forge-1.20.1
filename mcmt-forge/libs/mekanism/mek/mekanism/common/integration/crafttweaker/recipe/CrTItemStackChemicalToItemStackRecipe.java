package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker_annotations.annotations.NativeMethod;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import com.blamejared.crafttweaker_annotations.annotations.NativeMethod.Holder;
import java.util.List;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
import mekanism.api.recipes.NucleosynthesizingRecipe;
import mekanism.api.recipes.PaintingRecipe;
import mekanism.api.recipes.chemical.ItemStackChemicalToItemStackRecipe;
import mekanism.common.integration.crafttweaker.CrTUtils;
import org.openzen.zencode.java.ZenCodeType.Getter;
import org.openzen.zencode.java.ZenCodeType.Method;

@ZenRegister
@Holder({@NativeMethod(
      name = "getItemInput",
      parameters = {},
      getterName = "itemInput"
   ), @NativeMethod(
      name = "getChemicalInput",
      parameters = {},
      getterName = "chemicalInput"
   )})
@NativeTypeRegistration(
   value = ItemStackChemicalToItemStackRecipe.class,
   zenCodeName = "mods.mekanism.recipe.ItemStackChemicalToItemStack"
)
public class CrTItemStackChemicalToItemStackRecipe {
   private CrTItemStackChemicalToItemStackRecipe() {
   }

   @ZenRegister
   @NativeTypeRegistration(
      value = ItemStackGasToItemStackRecipe.class,
      zenCodeName = "mods.mekanism.recipe.ItemStackChemicalToItemStack.Gas"
   )
   public static class CrTItemStackGasToItemStackRecipe {
      private CrTItemStackGasToItemStackRecipe() {
      }

      @Method
      @Getter("outputs")
      public static List<IItemStack> getOutputs(ItemStackGasToItemStackRecipe _this) {
         return CrTUtils.convertItems(_this.getOutputDefinition());
      }

      @ZenRegister
      @NativeTypeRegistration(
         value = NucleosynthesizingRecipe.class,
         zenCodeName = "mods.mekanism.recipe.ItemStackChemicalToItemStack.Gas.Nucleosynthesizing"
      )
      public static class CrTNucleosynthesizingRecipe {
         private CrTNucleosynthesizingRecipe() {
         }

         @Method
         @Getter("duration")
         public static int getDuration(NucleosynthesizingRecipe _this) {
            return _this.getDuration();
         }
      }
   }

   @ZenRegister
   @NativeTypeRegistration(
      value = MetallurgicInfuserRecipe.class,
      zenCodeName = "mods.mekanism.recipe.ItemStackChemicalToItemStack.MetallurgicInfusing"
   )
   public static class CrTMetallurgicInfuserRecipe {
      private CrTMetallurgicInfuserRecipe() {
      }

      @Method
      @Getter("outputs")
      public static List<IItemStack> getOutputs(MetallurgicInfuserRecipe _this) {
         return CrTUtils.convertItems(_this.getOutputDefinition());
      }
   }

   @ZenRegister
   @NativeTypeRegistration(
      value = PaintingRecipe.class,
      zenCodeName = "mods.mekanism.recipe.ItemStackChemicalToItemStack.Painting"
   )
   public static class CrTPaintingRecipe {
      private CrTPaintingRecipe() {
      }

      @Method
      @Getter("outputs")
      public static List<IItemStack> getOutputs(PaintingRecipe _this) {
         return CrTUtils.convertItems(_this.getOutputDefinition());
      }
   }
}
