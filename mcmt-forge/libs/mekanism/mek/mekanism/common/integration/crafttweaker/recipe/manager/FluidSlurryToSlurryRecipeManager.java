package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.recipes.FluidSlurryToSlurryRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.FluidSlurryToSlurryIRecipe;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

@ZenRegister
@Name("mods.mekanism.recipe.manager.FluidSlurryToSlurry")
public abstract class FluidSlurryToSlurryRecipeManager extends MekanismRecipeManager<FluidSlurryToSlurryRecipe> {
   protected FluidSlurryToSlurryRecipeManager(IMekanismRecipeTypeProvider<FluidSlurryToSlurryRecipe, ?> recipeType) {
      super(recipeType);
   }

   @Method
   public void addRecipe(
      String name, FluidStackIngredient fluidInput, ChemicalStackIngredient.SlurryStackIngredient slurryInput, ICrTChemicalStack.ICrTSlurryStack output
   ) {
      this.addRecipe(this.makeRecipe(this.getAndValidateName(name), fluidInput, slurryInput, output));
   }

   public final FluidSlurryToSlurryRecipe makeRecipe(
      ResourceLocation id, FluidStackIngredient fluidInput, ChemicalStackIngredient.SlurryStackIngredient slurryInput, ICrTChemicalStack.ICrTSlurryStack output
   ) {
      return this.makeRecipe(id, fluidInput, slurryInput, this.getAndValidateNotEmpty(output));
   }

   protected abstract FluidSlurryToSlurryRecipe makeRecipe(
      ResourceLocation id, FluidStackIngredient fluidInput, ChemicalStackIngredient.SlurryStackIngredient slurryInput, SlurryStack output
   );

   protected String describeOutputs(FluidSlurryToSlurryRecipe recipe) {
      return CrTUtils.describeOutputs(recipe.getOutputDefinition());
   }

   @ZenRegister
   @Name("mods.mekanism.recipe.manager.FluidSlurryToSlurry.Washing")
   public static class ChemicalWasherRecipeManager extends FluidSlurryToSlurryRecipeManager {
      public static final FluidSlurryToSlurryRecipeManager.ChemicalWasherRecipeManager INSTANCE = new FluidSlurryToSlurryRecipeManager.ChemicalWasherRecipeManager();

      private ChemicalWasherRecipeManager() {
         super(MekanismRecipeType.WASHING);
      }

      @Override
      protected FluidSlurryToSlurryRecipe makeRecipe(
         ResourceLocation id, FluidStackIngredient fluidInput, ChemicalStackIngredient.SlurryStackIngredient slurryInput, SlurryStack output
      ) {
         return new FluidSlurryToSlurryIRecipe(id, fluidInput, slurryInput, output);
      }
   }
}
