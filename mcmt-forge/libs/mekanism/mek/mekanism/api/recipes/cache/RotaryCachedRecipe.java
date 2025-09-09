package mekanism.api.recipes.cache;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.outputs.IOutputHandler;
import net.minecraftforge.fluids.FluidStack;

@NothingNullByDefault
public class RotaryCachedRecipe extends CachedRecipe<RotaryRecipe> {
   private final IOutputHandler<GasStack> gasOutputHandler;
   private final IOutputHandler<FluidStack> fluidOutputHandler;
   private final IInputHandler<FluidStack> fluidInputHandler;
   private final IInputHandler<GasStack> gasInputHandler;
   private final BooleanSupplier modeSupplier;
   private FluidStack recipeFluid = FluidStack.EMPTY;
   private GasStack recipeGas = GasStack.EMPTY;
   private FluidStack fluidOutput = FluidStack.EMPTY;
   private GasStack gasOutput = GasStack.EMPTY;

   public RotaryCachedRecipe(
      RotaryRecipe recipe,
      BooleanSupplier recheckAllErrors,
      IInputHandler<FluidStack> fluidInputHandler,
      IInputHandler<GasStack> gasInputHandler,
      IOutputHandler<GasStack> gasOutputHandler,
      IOutputHandler<FluidStack> fluidOutputHandler,
      BooleanSupplier modeSupplier
   ) {
      super(recipe, recheckAllErrors);
      this.fluidInputHandler = Objects.requireNonNull(fluidInputHandler, "Fluid input handler cannot be null.");
      this.gasInputHandler = Objects.requireNonNull(gasInputHandler, "Gas input handler cannot be null.");
      this.gasOutputHandler = Objects.requireNonNull(gasOutputHandler, "Gas output handler cannot be null.");
      this.fluidOutputHandler = Objects.requireNonNull(fluidOutputHandler, "Fluid output handler cannot be null.");
      this.modeSupplier = Objects.requireNonNull(modeSupplier, "Mode supplier cannot be null.");
   }

   @Override
   protected void calculateOperationsThisTick(CachedRecipe.OperationTracker tracker) {
      super.calculateOperationsThisTick(tracker);
      if (tracker.shouldContinueChecking()) {
         if (this.modeSupplier.getAsBoolean()) {
            if (!this.recipe.hasFluidToGas()) {
               tracker.mismatchedRecipe();
            } else {
               CachedRecipeHelper.oneInputCalculateOperationsThisTick(
                  tracker,
                  this.fluidInputHandler,
                  this.recipe::getFluidInput,
                  input -> this.recipeFluid = input,
                  this.gasOutputHandler,
                  this.recipe::getGasOutput,
                  output -> this.gasOutput = output,
                  FluidStack::isEmpty
               );
            }
         } else if (!this.recipe.hasGasToFluid()) {
            tracker.mismatchedRecipe();
         } else {
            CachedRecipeHelper.oneInputCalculateOperationsThisTick(
               tracker,
               this.gasInputHandler,
               this.recipe::getGasInput,
               input -> this.recipeGas = input,
               this.fluidOutputHandler,
               this.recipe::getFluidOutput,
               output -> this.fluidOutput = output,
               ChemicalStack::isEmpty
            );
         }
      }
   }

   @Override
   public boolean isInputValid() {
      if (this.modeSupplier.getAsBoolean()) {
         if (!this.recipe.hasFluidToGas()) {
            return false;
         } else {
            FluidStack fluidStack = this.fluidInputHandler.getInput();
            return !fluidStack.isEmpty() && this.recipe.test(fluidStack);
         }
      } else if (!this.recipe.hasGasToFluid()) {
         return false;
      } else {
         GasStack gasStack = this.gasInputHandler.getInput();
         return !gasStack.isEmpty() && this.recipe.test(gasStack);
      }
   }

   @Override
   protected void finishProcessing(int operations) {
      if (this.modeSupplier.getAsBoolean()) {
         if (this.recipe.hasFluidToGas() && !this.recipeFluid.isEmpty() && !this.gasOutput.isEmpty()) {
            this.fluidInputHandler.use(this.recipeFluid, operations);
            this.gasOutputHandler.handleOutput(this.gasOutput, operations);
         }
      } else if (this.recipe.hasGasToFluid() && !this.recipeGas.isEmpty() && !this.fluidOutput.isEmpty()) {
         this.gasInputHandler.use(this.recipeGas, operations);
         this.fluidOutputHandler.handleOutput(this.fluidOutput, operations);
      }
   }
}
