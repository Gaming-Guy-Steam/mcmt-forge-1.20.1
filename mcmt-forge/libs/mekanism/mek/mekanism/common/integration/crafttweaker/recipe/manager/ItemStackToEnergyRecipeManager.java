package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ItemStackToEnergyRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.EnergyConversionIRecipe;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

@ZenRegister
@Name("mods.mekanism.recipe.manager.ItemStackToEnergy")
public abstract class ItemStackToEnergyRecipeManager extends MekanismRecipeManager<ItemStackToEnergyRecipe> {
   protected ItemStackToEnergyRecipeManager(IMekanismRecipeTypeProvider<ItemStackToEnergyRecipe, ?> recipeType) {
      super(recipeType);
   }

   @Method
   public void addRecipe(String name, ItemStackIngredient input, FloatingLong output) {
      this.addRecipe(this.makeRecipe(this.getAndValidateName(name), input, output));
   }

   public final ItemStackToEnergyRecipe makeRecipe(ResourceLocation id, ItemStackIngredient input, FloatingLong output) {
      if (output.isZero()) {
         throw new IllegalArgumentException("Output must be greater than zero.");
      } else {
         return this.makeRecipeInternal(id, input, output.copyAsConst());
      }
   }

   protected abstract ItemStackToEnergyRecipe makeRecipeInternal(ResourceLocation id, ItemStackIngredient input, FloatingLong output);

   protected String describeOutputs(ItemStackToEnergyRecipe recipe) {
      return CrTUtils.describeOutputs(recipe.getOutputDefinition(), fl -> fl);
   }

   @ZenRegister
   @Name("mods.mekanism.recipe.manager.ItemStackToEnergy.EnergyConversion")
   public static class EnergyConversionRecipeManager extends ItemStackToEnergyRecipeManager {
      public static final ItemStackToEnergyRecipeManager.EnergyConversionRecipeManager INSTANCE = new ItemStackToEnergyRecipeManager.EnergyConversionRecipeManager();

      private EnergyConversionRecipeManager() {
         super(MekanismRecipeType.ENERGY_CONVERSION);
      }

      @Override
      protected ItemStackToEnergyRecipe makeRecipeInternal(ResourceLocation id, ItemStackIngredient input, FloatingLong output) {
         return new EnergyConversionIRecipe(id, input, output);
      }
   }
}
