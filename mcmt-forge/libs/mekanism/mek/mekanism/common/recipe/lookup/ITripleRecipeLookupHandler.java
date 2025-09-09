package mekanism.common.recipe.lookup;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.common.recipe.lookup.cache.InputRecipeCache;
import mekanism.common.recipe.lookup.cache.TripleInputRecipeCache;
import mekanism.common.util.ChemicalUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.TriPredicate;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public interface ITripleRecipeLookupHandler<INPUT_A, INPUT_B, INPUT_C, RECIPE extends MekanismRecipe & TriPredicate<INPUT_A, INPUT_B, INPUT_C>, INPUT_CACHE extends TripleInputRecipeCache<INPUT_A, ?, INPUT_B, ?, INPUT_C, ?, RECIPE, ?, ?, ?>>
   extends IRecipeLookupHandler.IRecipeTypedLookupHandler<RECIPE, INPUT_CACHE> {
   default boolean containsRecipeABC(INPUT_A inputA, INPUT_B inputB, INPUT_C inputC) {
      return this.getRecipeType().getInputCache().containsInputABC(this.getHandlerWorld(), inputA, inputB, inputC);
   }

   default boolean containsRecipeBAC(INPUT_A inputA, INPUT_B inputB, INPUT_C inputC) {
      return this.getRecipeType().getInputCache().containsInputBAC(this.getHandlerWorld(), inputA, inputB, inputC);
   }

   default boolean containsRecipeCAB(INPUT_A inputA, INPUT_B inputB, INPUT_C inputC) {
      return this.getRecipeType().getInputCache().containsInputCAB(this.getHandlerWorld(), inputA, inputB, inputC);
   }

   default boolean containsRecipeA(INPUT_A input) {
      return this.getRecipeType().getInputCache().containsInputA(this.getHandlerWorld(), input);
   }

   default boolean containsRecipeB(INPUT_B input) {
      return this.getRecipeType().getInputCache().containsInputB(this.getHandlerWorld(), input);
   }

   default boolean containsRecipeC(INPUT_C input) {
      return this.getRecipeType().getInputCache().containsInputC(this.getHandlerWorld(), input);
   }

   @Nullable
   default RECIPE findFirstRecipe(INPUT_A inputA, INPUT_B inputB, INPUT_C inputC) {
      return this.getRecipeType().getInputCache().findFirstRecipe(this.getHandlerWorld(), inputA, inputB, inputC);
   }

   @Nullable
   default RECIPE findFirstRecipe(IInputHandler<INPUT_A> inputAHandler, IInputHandler<INPUT_B> inputBHandler, IInputHandler<INPUT_C> inputCHandler) {
      return this.findFirstRecipe(inputAHandler.getInput(), inputBHandler.getInput(), inputCHandler.getInput());
   }

   public interface ItemFluidChemicalRecipeLookupHandler<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, RECIPE extends MekanismRecipe & TriPredicate<ItemStack, FluidStack, STACK>>
      extends ITripleRecipeLookupHandler.ObjectObjectChemicalRecipeLookupHandler<ItemStack, FluidStack, CHEMICAL, STACK, RECIPE, InputRecipeCache.ItemFluidChemical<CHEMICAL, STACK, RECIPE>> {
   }

   public interface ObjectObjectChemicalRecipeLookupHandler<INPUT_A, INPUT_B, CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, RECIPE extends MekanismRecipe & TriPredicate<INPUT_A, INPUT_B, STACK>, INPUT_CACHE extends TripleInputRecipeCache<INPUT_A, ?, INPUT_B, ?, STACK, ?, RECIPE, ?, ?, ?>>
      extends ITripleRecipeLookupHandler<INPUT_A, INPUT_B, STACK, RECIPE, INPUT_CACHE> {
      default boolean containsRecipeCAB(INPUT_A inputA, INPUT_B inputB, CHEMICAL inputC) {
         return this.containsRecipeCAB(inputA, inputB, ChemicalUtil.withAmount(inputC, 1L));
      }

      default boolean containsRecipeC(CHEMICAL input) {
         return this.containsRecipeC(ChemicalUtil.withAmount(input, 1L));
      }
   }
}
