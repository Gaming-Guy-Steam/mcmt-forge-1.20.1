package mekanism.common.recipe.lookup;

import java.util.function.Predicate;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.common.recipe.lookup.cache.InputRecipeCache;
import mekanism.common.recipe.lookup.cache.SingleInputRecipeCache;
import mekanism.common.util.ChemicalUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public interface ISingleRecipeLookupHandler<INPUT, RECIPE extends MekanismRecipe & Predicate<INPUT>, INPUT_CACHE extends SingleInputRecipeCache<INPUT, ?, RECIPE, ?>>
   extends IRecipeLookupHandler.IRecipeTypedLookupHandler<RECIPE, INPUT_CACHE> {
   default boolean containsRecipe(INPUT input) {
      return this.getRecipeType().getInputCache().containsInput(this.getHandlerWorld(), input);
   }

   @Nullable
   default RECIPE findFirstRecipe(INPUT input) {
      return this.getRecipeType().getInputCache().findFirstRecipe(this.getHandlerWorld(), input);
   }

   @Nullable
   default RECIPE findFirstRecipe(IInputHandler<INPUT> inputHandler) {
      return this.findFirstRecipe(inputHandler.getInput());
   }

   public interface ChemicalRecipeLookupHandler<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, RECIPE extends MekanismRecipe & Predicate<STACK>>
      extends ISingleRecipeLookupHandler<STACK, RECIPE, InputRecipeCache.SingleChemical<CHEMICAL, STACK, RECIPE>> {
      default boolean containsRecipe(CHEMICAL input) {
         return this.containsRecipe(ChemicalUtil.withAmount(input, 1L));
      }
   }

   public interface FluidRecipeLookupHandler<RECIPE extends MekanismRecipe & Predicate<FluidStack>>
      extends ISingleRecipeLookupHandler<FluidStack, RECIPE, InputRecipeCache.SingleFluid<RECIPE>> {
   }

   public interface ItemRecipeLookupHandler<RECIPE extends MekanismRecipe & Predicate<ItemStack>>
      extends ISingleRecipeLookupHandler<ItemStack, RECIPE, InputRecipeCache.SingleItem<RECIPE>> {
   }
}
