package mekanism.api.recipes.cache;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.api.recipes.ItemStackToFluidRecipe;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.chemical.ChemicalToChemicalRecipe;
import mekanism.api.recipes.chemical.ItemStackToChemicalRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.InputIngredient;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.outputs.IOutputHandler;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class OneInputCachedRecipe<INPUT, OUTPUT, RECIPE extends MekanismRecipe & Predicate<INPUT>> extends CachedRecipe<RECIPE> {
   private final IInputHandler<INPUT> inputHandler;
   private final IOutputHandler<OUTPUT> outputHandler;
   private final Predicate<INPUT> inputEmptyCheck;
   private final Supplier<? extends InputIngredient<INPUT>> inputSupplier;
   private final Function<INPUT, OUTPUT> outputGetter;
   private final Predicate<OUTPUT> outputEmptyCheck;
   @Nullable
   private INPUT input;
   @Nullable
   private OUTPUT output;

   protected OneInputCachedRecipe(
      RECIPE recipe,
      BooleanSupplier recheckAllErrors,
      IInputHandler<INPUT> inputHandler,
      IOutputHandler<OUTPUT> outputHandler,
      Supplier<? extends InputIngredient<INPUT>> inputSupplier,
      Function<INPUT, OUTPUT> outputGetter,
      Predicate<INPUT> inputEmptyCheck,
      Predicate<OUTPUT> outputEmptyCheck
   ) {
      super(recipe, recheckAllErrors);
      this.inputHandler = Objects.requireNonNull(inputHandler, "Input handler cannot be null.");
      this.outputHandler = Objects.requireNonNull(outputHandler, "Output handler cannot be null.");
      this.inputSupplier = Objects.requireNonNull(inputSupplier, "Input ingredient supplier cannot be null.");
      this.outputGetter = Objects.requireNonNull(outputGetter, "Output getter cannot be null.");
      this.inputEmptyCheck = Objects.requireNonNull(inputEmptyCheck, "Input empty check cannot be null.");
      this.outputEmptyCheck = Objects.requireNonNull(outputEmptyCheck, "Output empty check cannot be null.");
   }

   @Override
   protected void calculateOperationsThisTick(CachedRecipe.OperationTracker tracker) {
      super.calculateOperationsThisTick(tracker);
      CachedRecipeHelper.oneInputCalculateOperationsThisTick(
         tracker,
         this.inputHandler,
         this.inputSupplier,
         input -> this.input = input,
         this.outputHandler,
         this.outputGetter,
         output -> this.output = output,
         this.inputEmptyCheck
      );
   }

   @Override
   public boolean isInputValid() {
      INPUT input = this.inputHandler.getInput();
      return !this.inputEmptyCheck.test(input) && this.recipe.test(input);
   }

   @Override
   protected void finishProcessing(int operations) {
      if (this.input != null && this.output != null && !this.inputEmptyCheck.test(this.input) && !this.outputEmptyCheck.test(this.output)) {
         this.inputHandler.use(this.input, operations);
         this.outputHandler.handleOutput(this.output, operations);
      }
   }

   public static OneInputCachedRecipe<FluidStack, ElectrolysisRecipe.ElectrolysisRecipeOutput, ElectrolysisRecipe> separating(
      ElectrolysisRecipe recipe,
      BooleanSupplier recheckAllErrors,
      IInputHandler<FluidStack> inputHandler,
      IOutputHandler<ElectrolysisRecipe.ElectrolysisRecipeOutput> outputHandler
   ) {
      return new OneInputCachedRecipe<>(
         recipe, recheckAllErrors, inputHandler, outputHandler, recipe::getInput, recipe::getOutput, FluidStack::isEmpty, ConstantPredicates.alwaysFalse()
      );
   }

   public static OneInputCachedRecipe<FluidStack, FluidStack, FluidToFluidRecipe> fluidToFluid(
      FluidToFluidRecipe recipe, BooleanSupplier recheckAllErrors, IInputHandler<FluidStack> inputHandler, IOutputHandler<FluidStack> outputHandler
   ) {
      return new OneInputCachedRecipe<>(
         recipe, recheckAllErrors, inputHandler, outputHandler, recipe::getInput, recipe::getOutput, FluidStack::isEmpty, FluidStack::isEmpty
      );
   }

   public static OneInputCachedRecipe<ItemStack, ItemStack, ItemStackToItemStackRecipe> itemToItem(
      ItemStackToItemStackRecipe recipe, BooleanSupplier recheckAllErrors, IInputHandler<ItemStack> inputHandler, IOutputHandler<ItemStack> outputHandler
   ) {
      return new OneInputCachedRecipe<>(
         recipe, recheckAllErrors, inputHandler, outputHandler, recipe::getInput, recipe::getOutput, ItemStack::m_41619_, ItemStack::m_41619_
      );
   }

   public static OneInputCachedRecipe<ItemStack, FluidStack, ItemStackToFluidRecipe> itemToFluid(
      ItemStackToFluidRecipe recipe, BooleanSupplier recheckAllErrors, IInputHandler<ItemStack> inputHandler, IOutputHandler<FluidStack> outputHandler
   ) {
      return new OneInputCachedRecipe<>(
         recipe, recheckAllErrors, inputHandler, outputHandler, recipe::getInput, recipe::getOutput, ItemStack::m_41619_, FluidStack::isEmpty
      );
   }

   public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, RECIPE extends ItemStackToChemicalRecipe<CHEMICAL, STACK>> OneInputCachedRecipe<ItemStack, STACK, RECIPE> itemToChemical(
      RECIPE recipe, BooleanSupplier recheckAllErrors, IInputHandler<ItemStack> inputHandler, IOutputHandler<STACK> outputHandler
   ) {
      return new OneInputCachedRecipe<>(
         recipe, recheckAllErrors, inputHandler, outputHandler, recipe::getInput, recipe::getOutput, ItemStack::m_41619_, ChemicalStack::isEmpty
      );
   }

   public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK>, RECIPE extends ChemicalToChemicalRecipe<CHEMICAL, STACK, INGREDIENT>> OneInputCachedRecipe<STACK, STACK, RECIPE> chemicalToChemical(
      RECIPE recipe, BooleanSupplier recheckAllErrors, IInputHandler<STACK> inputHandler, IOutputHandler<STACK> outputHandler
   ) {
      return new OneInputCachedRecipe<>(
         recipe, recheckAllErrors, inputHandler, outputHandler, recipe::getInput, recipe::getOutput, ChemicalStack::isEmpty, ChemicalStack::isEmpty
      );
   }

   public static OneInputCachedRecipe<ItemStack, SawmillRecipe.ChanceOutput, SawmillRecipe> sawing(
      SawmillRecipe recipe, BooleanSupplier recheckAllErrors, IInputHandler<ItemStack> inputHandler, IOutputHandler<SawmillRecipe.ChanceOutput> outputHandler
   ) {
      return new OneInputCachedRecipe<>(
         recipe, recheckAllErrors, inputHandler, outputHandler, recipe::getInput, recipe::getOutput, ItemStack::m_41619_, ConstantPredicates.alwaysFalse()
      );
   }
}
