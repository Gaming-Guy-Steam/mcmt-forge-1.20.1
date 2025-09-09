package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.action.recipe.ActionAddRecipe;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import java.util.List;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fluids.FluidStack;
import org.openzen.zencode.java.ZenCodeType.Name;

@ZenRegister
@Name("mods.mekanism.recipe.manager.MekanismRecipe")
public abstract class MekanismRecipeManager<RECIPE extends MekanismRecipe> implements IRecipeManager<RECIPE> {
   private final IMekanismRecipeTypeProvider<RECIPE, ?> recipeType;

   protected MekanismRecipeManager(IMekanismRecipeTypeProvider<RECIPE, ?> recipeType) {
      this.recipeType = recipeType;
   }

   protected abstract String describeOutputs(RECIPE recipe);

   protected void addRecipe(RECIPE recipe) {
      CraftTweakerAPI.apply(new ActionAddRecipe(this, recipe).outputDescriber(this::describeOutputs));
   }

   public RecipeType<RECIPE> getRecipeType() {
      return this.recipeType.getRecipeType();
   }

   public ResourceLocation getBracketResourceLocation() {
      return this.recipeType.getRegistryName();
   }

   @Deprecated
   public List<RECIPE> getRecipesByOutput(IIngredient output) {
      throw new UnsupportedOperationException("Mekanism's recipe managers don't support reverse lookup by output, please lookup by recipe name.");
   }

   @Deprecated
   public void remove(IIngredient output) {
      throw new UnsupportedOperationException("Mekanism's recipe managers don't support removal by output, please remove by recipe name.");
   }

   protected ResourceLocation getAndValidateName(String path) {
      return CrTUtils.rl(this.fixRecipeName(path));
   }

   protected ItemStack getAndValidateNotEmpty(IItemStack stack) {
      if (stack.isEmpty()) {
         throw new IllegalArgumentException("Output stack cannot be empty.");
      } else {
         return stack.getImmutableInternal();
      }
   }

   protected FluidStack getAndValidateNotEmpty(IFluidStack stack) {
      if (stack.isEmpty()) {
         throw new IllegalArgumentException("Output stack cannot be empty.");
      } else {
         return (FluidStack)stack.getImmutableInternal();
      }
   }

   protected <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> STACK getAndValidateNotEmpty(
      ICrTChemicalStack<CHEMICAL, STACK, ?> stack
   ) {
      if (stack.isEmpty()) {
         throw new IllegalArgumentException("Output stack cannot be empty.");
      } else {
         return stack.getImmutableInternal();
      }
   }
}
