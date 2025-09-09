package mekanism.common.recipe.lookup;

import java.util.function.BiPredicate;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.common.recipe.lookup.cache.DoubleInputRecipeCache;
import mekanism.common.recipe.lookup.cache.InputRecipeCache;
import mekanism.common.util.ChemicalUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public interface IDoubleRecipeLookupHandler<INPUT_A, INPUT_B, RECIPE extends MekanismRecipe & BiPredicate<INPUT_A, INPUT_B>, INPUT_CACHE extends DoubleInputRecipeCache<INPUT_A, ?, INPUT_B, ?, RECIPE, ?, ?>>
   extends IRecipeLookupHandler.IRecipeTypedLookupHandler<RECIPE, INPUT_CACHE> {
   default boolean containsRecipeAB(INPUT_A inputA, INPUT_B inputB) {
      return this.getRecipeType().getInputCache().containsInputAB(this.getHandlerWorld(), inputA, inputB);
   }

   default boolean containsRecipeBA(INPUT_A inputA, INPUT_B inputB) {
      return this.getRecipeType().getInputCache().containsInputBA(this.getHandlerWorld(), inputA, inputB);
   }

   default boolean containsRecipeA(INPUT_A input) {
      return this.getRecipeType().getInputCache().containsInputA(this.getHandlerWorld(), input);
   }

   default boolean containsRecipeB(INPUT_B input) {
      return this.getRecipeType().getInputCache().containsInputB(this.getHandlerWorld(), input);
   }

   @Nullable
   default RECIPE findFirstRecipe(INPUT_A inputA, INPUT_B inputB) {
      return this.getRecipeType().getInputCache().findFirstRecipe(this.getHandlerWorld(), inputA, inputB);
   }

   @Nullable
   default RECIPE findFirstRecipe(IInputHandler<INPUT_A> inputAHandler, IInputHandler<INPUT_B> inputBHandler) {
      return this.findFirstRecipe(inputAHandler.getInput(), inputBHandler.getInput());
   }

   public interface DoubleItemRecipeLookupHandler<RECIPE extends MekanismRecipe & BiPredicate<ItemStack, ItemStack>>
      extends IDoubleRecipeLookupHandler<ItemStack, ItemStack, RECIPE, InputRecipeCache.DoubleItem<RECIPE>> {
   }

   public interface FluidChemicalRecipeLookupHandler<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, RECIPE extends MekanismRecipe & BiPredicate<FluidStack, STACK>>
      extends IDoubleRecipeLookupHandler.ObjectChemicalRecipeLookupHandler<FluidStack, CHEMICAL, STACK, RECIPE, InputRecipeCache.FluidChemical<CHEMICAL, STACK, RECIPE>> {
   }

   public interface ItemChemicalRecipeLookupHandler<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, RECIPE extends MekanismRecipe & BiPredicate<ItemStack, STACK>>
      extends IDoubleRecipeLookupHandler.ObjectChemicalRecipeLookupHandler<ItemStack, CHEMICAL, STACK, RECIPE, InputRecipeCache.ItemChemical<CHEMICAL, STACK, RECIPE>> {
   }

   public interface ObjectChemicalRecipeLookupHandler<INPUT, CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, RECIPE extends MekanismRecipe & BiPredicate<INPUT, STACK>, INPUT_CACHE extends DoubleInputRecipeCache<INPUT, ?, STACK, ?, RECIPE, ?, ?>>
      extends IDoubleRecipeLookupHandler<INPUT, STACK, RECIPE, INPUT_CACHE> {
      default boolean containsRecipeBA(INPUT inputA, CHEMICAL inputB) {
         return this.containsRecipeBA(inputA, ChemicalUtil.withAmount(inputB, 1L));
      }

      default boolean containsRecipeB(CHEMICAL input) {
         return this.containsRecipeB(ChemicalUtil.withAmount(input, 1L));
      }
   }
}
