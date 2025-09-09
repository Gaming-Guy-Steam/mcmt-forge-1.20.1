package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.FluidToFluidIRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

@ZenRegister
@Name("mods.mekanism.recipe.manager.FluidToFluid")
public abstract class FluidToFluidRecipeManager extends MekanismRecipeManager<FluidToFluidRecipe> {
   protected FluidToFluidRecipeManager(IMekanismRecipeTypeProvider<FluidToFluidRecipe, ?> recipeType) {
      super(recipeType);
   }

   @Method
   public void addRecipe(String name, FluidStackIngredient input, IFluidStack output) {
      this.addRecipe(this.makeRecipe(this.getAndValidateName(name), input, output));
   }

   public final FluidToFluidIRecipe makeRecipe(ResourceLocation id, FluidStackIngredient input, IFluidStack output) {
      return this.makeRecipe(id, input, this.getAndValidateNotEmpty(output));
   }

   protected abstract FluidToFluidIRecipe makeRecipe(ResourceLocation id, FluidStackIngredient input, FluidStack output);

   protected String describeOutputs(FluidToFluidRecipe recipe) {
      return CrTUtils.describeOutputs(recipe.getOutputDefinition(), IFluidStack::of);
   }

   @ZenRegister
   @Name("mods.mekanism.recipe.manager.FluidToFluid.Evaporating")
   public static class EvaporatingRecipeManager extends FluidToFluidRecipeManager {
      public static final FluidToFluidRecipeManager.EvaporatingRecipeManager INSTANCE = new FluidToFluidRecipeManager.EvaporatingRecipeManager();

      private EvaporatingRecipeManager() {
         super(MekanismRecipeType.EVAPORATING);
      }

      @Override
      protected FluidToFluidIRecipe makeRecipe(ResourceLocation id, FluidStackIngredient input, FluidStack output) {
         return new FluidToFluidIRecipe(id, input, output);
      }
   }
}
