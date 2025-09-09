package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.ActivatingIRecipe;
import mekanism.common.recipe.impl.CentrifugingIRecipe;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

@ZenRegister
@Name("mods.mekanism.recipe.manager.GasToGas")
public abstract class GasToGasRecipeManager extends MekanismRecipeManager<GasToGasRecipe> {
   protected GasToGasRecipeManager(IMekanismRecipeTypeProvider<GasToGasRecipe, ?> recipeType) {
      super(recipeType);
   }

   @Method
   public void addRecipe(String name, ChemicalStackIngredient.GasStackIngredient input, ICrTChemicalStack.ICrTGasStack output) {
      this.addRecipe(this.makeRecipe(this.getAndValidateName(name), input, output));
   }

   public final GasToGasRecipe makeRecipe(ResourceLocation id, ChemicalStackIngredient.GasStackIngredient input, ICrTChemicalStack.ICrTGasStack output) {
      return this.makeRecipe(id, input, this.getAndValidateNotEmpty(output));
   }

   protected abstract GasToGasRecipe makeRecipe(ResourceLocation id, ChemicalStackIngredient.GasStackIngredient ingredient, GasStack output);

   protected String describeOutputs(GasToGasRecipe recipe) {
      return CrTUtils.describeOutputs(recipe.getOutputDefinition());
   }

   @ZenRegister
   @Name("mods.mekanism.recipe.manager.GasToGas.Centrifuging")
   public static class IsotopicCentrifugeRecipeManager extends GasToGasRecipeManager {
      public static final GasToGasRecipeManager.IsotopicCentrifugeRecipeManager INSTANCE = new GasToGasRecipeManager.IsotopicCentrifugeRecipeManager();

      private IsotopicCentrifugeRecipeManager() {
         super(MekanismRecipeType.CENTRIFUGING);
      }

      @Override
      protected GasToGasRecipe makeRecipe(ResourceLocation id, ChemicalStackIngredient.GasStackIngredient ingredient, GasStack output) {
         return new CentrifugingIRecipe(id, ingredient, output);
      }
   }

   @ZenRegister
   @Name("mods.mekanism.recipe.manager.GasToGas.Activating")
   public static class SolarNeutronActivatorRecipeManager extends GasToGasRecipeManager {
      public static final GasToGasRecipeManager.SolarNeutronActivatorRecipeManager INSTANCE = new GasToGasRecipeManager.SolarNeutronActivatorRecipeManager();

      private SolarNeutronActivatorRecipeManager() {
         super(MekanismRecipeType.ACTIVATING);
      }

      @Override
      protected GasToGasRecipe makeRecipe(ResourceLocation id, ChemicalStackIngredient.GasStackIngredient ingredient, GasStack output) {
         return new ActivatingIRecipe(id, ingredient, output);
      }
   }
}
