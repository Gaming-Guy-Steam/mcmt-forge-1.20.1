package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.util.ItemStackUtil;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.PressurizedReactionIRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

@ZenRegister
@Name("mods.mekanism.recipe.manager.Reaction")
public class PressurizedReactionRecipeManager extends MekanismRecipeManager<PressurizedReactionRecipe> {
   public static final PressurizedReactionRecipeManager INSTANCE = new PressurizedReactionRecipeManager();

   private PressurizedReactionRecipeManager() {
      super(MekanismRecipeType.REACTION);
   }

   @Method
   public void addRecipe(
      String name,
      ItemStackIngredient inputSolid,
      FluidStackIngredient inputFluid,
      ChemicalStackIngredient.GasStackIngredient inputGas,
      int duration,
      IItemStack outputItem,
      FloatingLong energyRequired
   ) {
      this.addRecipe(name, inputSolid, inputFluid, inputGas, duration, this.getAndValidateNotEmpty(outputItem), GasStack.EMPTY, energyRequired);
   }

   @Method
   public void addRecipe(
      String name,
      ItemStackIngredient inputSolid,
      FluidStackIngredient inputFluid,
      ChemicalStackIngredient.GasStackIngredient inputGas,
      int duration,
      IItemStack outputItem
   ) {
      this.addRecipe(name, inputSolid, inputFluid, inputGas, duration, this.getAndValidateNotEmpty(outputItem), GasStack.EMPTY, FloatingLong.ZERO);
   }

   @Method
   public void addRecipe(
      String name,
      ItemStackIngredient inputSolid,
      FluidStackIngredient inputFluid,
      ChemicalStackIngredient.GasStackIngredient inputGas,
      int duration,
      ICrTChemicalStack.ICrTGasStack outputGas,
      FloatingLong energyRequired
   ) {
      this.addRecipe(name, inputSolid, inputFluid, inputGas, duration, ItemStack.f_41583_, this.getAndValidateNotEmpty(outputGas), energyRequired);
   }

   @Method
   public void addRecipe(
      String name,
      ItemStackIngredient inputSolid,
      FluidStackIngredient inputFluid,
      ChemicalStackIngredient.GasStackIngredient inputGas,
      int duration,
      ICrTChemicalStack.ICrTGasStack outputGas
   ) {
      this.addRecipe(name, inputSolid, inputFluid, inputGas, duration, ItemStack.f_41583_, this.getAndValidateNotEmpty(outputGas), FloatingLong.ZERO);
   }

   @Method
   public void addRecipe(
      String name,
      ItemStackIngredient inputSolid,
      FluidStackIngredient inputFluid,
      ChemicalStackIngredient.GasStackIngredient inputGas,
      int duration,
      IItemStack outputItem,
      ICrTChemicalStack.ICrTGasStack outputGas,
      FloatingLong energyRequired
   ) {
      this.addRecipe(
         name, inputSolid, inputFluid, inputGas, duration, this.getAndValidateNotEmpty(outputItem), this.getAndValidateNotEmpty(outputGas), energyRequired
      );
   }

   @Method
   public void addRecipe(
      String name,
      ItemStackIngredient inputSolid,
      FluidStackIngredient inputFluid,
      ChemicalStackIngredient.GasStackIngredient inputGas,
      int duration,
      IItemStack outputItem,
      ICrTChemicalStack.ICrTGasStack outputGas
   ) {
      this.addRecipe(
         name, inputSolid, inputFluid, inputGas, duration, this.getAndValidateNotEmpty(outputItem), this.getAndValidateNotEmpty(outputGas), FloatingLong.ZERO
      );
   }

   private void addRecipe(
      String name,
      ItemStackIngredient inputSolid,
      FluidStackIngredient inputFluid,
      ChemicalStackIngredient.GasStackIngredient inputGas,
      int duration,
      ItemStack outputItem,
      GasStack outputGas,
      FloatingLong energyRequired
   ) {
      this.addRecipe(this.makeRecipe(this.getAndValidateName(name), inputSolid, inputFluid, inputGas, duration, outputItem, outputGas, energyRequired));
   }

   public PressurizedReactionRecipe makeRecipe(
      ResourceLocation id,
      ItemStackIngredient inputSolid,
      FluidStackIngredient inputFluid,
      ChemicalStackIngredient.GasStackIngredient inputGas,
      int duration,
      ItemStack outputItem,
      GasStack outputGas,
      FloatingLong energyRequired
   ) {
      if (duration <= 0) {
         throw new IllegalArgumentException("Duration must be positive! Duration: " + duration);
      } else {
         return new PressurizedReactionIRecipe(id, inputSolid, inputFluid, inputGas, energyRequired.copyAsConst(), duration, outputItem, outputGas);
      }
   }

   protected String describeOutputs(PressurizedReactionRecipe recipe) {
      return CrTUtils.describeOutputs(recipe.getOutputDefinition(), output -> {
         StringBuilder builder = new StringBuilder();
         ItemStack itemOutput = output.item();
         if (!itemOutput.m_41619_()) {
            builder.append(ItemStackUtil.getCommandString(itemOutput));
         }

         GasStack gasOutput = output.gas();
         if (!gasOutput.isEmpty()) {
            if (!itemOutput.m_41619_()) {
               builder.append(" and ");
            }

            builder.append(new CrTChemicalStack.CrTGasStack(gasOutput));
         }

         return builder.toString();
      });
   }
}
