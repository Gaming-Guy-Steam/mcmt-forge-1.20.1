package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import java.util.List;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import org.openzen.zencode.java.ZenCodeType.Getter;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

@ZenRegister
@NativeTypeRegistration(
   value = PressurizedReactionRecipe.class,
   zenCodeName = "mods.mekanism.recipe.Reaction"
)
public class CrTPressurizedReactionRecipe {
   private CrTPressurizedReactionRecipe() {
   }

   @Method
   @Getter("inputSolid")
   public static ItemStackIngredient getInputSolid(PressurizedReactionRecipe _this) {
      return _this.getInputSolid();
   }

   @Method
   @Getter("inputFluid")
   public static FluidStackIngredient getInputFluid(PressurizedReactionRecipe _this) {
      return _this.getInputFluid();
   }

   @Method
   @Getter("inputGas")
   public static ChemicalStackIngredient.GasStackIngredient getInputGas(PressurizedReactionRecipe _this) {
      return _this.getInputGas();
   }

   @Method
   @Getter("energyRequired")
   public static FloatingLong getEnergyRequired(PressurizedReactionRecipe _this) {
      return _this.getEnergyRequired().copyAsConst();
   }

   @Method
   @Getter("duration")
   public static int getDuration(PressurizedReactionRecipe _this) {
      return _this.getDuration();
   }

   @Method
   @Getter("outputs")
   public static List<CrTPressurizedReactionRecipe.CrTPressurizedReactionRecipeOutput> getOutputs(PressurizedReactionRecipe _this) {
      return CrTUtils.convert(
         _this.getOutputDefinition(),
         output -> new CrTPressurizedReactionRecipe.CrTPressurizedReactionRecipeOutput(
            IItemStack.of(output.item()), new CrTChemicalStack.CrTGasStack(output.gas())
         )
      );
   }

   @ZenRegister
   @Name("mods.mekanism.recipe.Reaction.Output")
   public record CrTPressurizedReactionRecipeOutput(@Getter("item") IItemStack item, @Getter("gas") ICrTChemicalStack.ICrTGasStack gas) {
   }
}
