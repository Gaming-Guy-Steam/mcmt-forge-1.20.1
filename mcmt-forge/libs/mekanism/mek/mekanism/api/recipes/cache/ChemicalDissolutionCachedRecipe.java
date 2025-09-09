package mekanism.api.recipes.cache;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.LongSupplier;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.recipes.ChemicalDissolutionRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.ILongInputHandler;
import mekanism.api.recipes.outputs.BoxedChemicalOutputHandler;
import net.minecraft.world.item.ItemStack;

@NothingNullByDefault
public class ChemicalDissolutionCachedRecipe extends CachedRecipe<ChemicalDissolutionRecipe> {
   private final BoxedChemicalOutputHandler outputHandler;
   private final IInputHandler<ItemStack> itemInputHandler;
   private final ILongInputHandler<GasStack> gasInputHandler;
   private final LongSupplier gasUsage;
   private long gasUsageMultiplier;
   private ItemStack recipeItem = ItemStack.f_41583_;
   private GasStack recipeGas = GasStack.EMPTY;
   private BoxedChemicalStack output = BoxedChemicalStack.EMPTY;

   public ChemicalDissolutionCachedRecipe(
      ChemicalDissolutionRecipe recipe,
      BooleanSupplier recheckAllErrors,
      IInputHandler<ItemStack> itemInputHandler,
      ILongInputHandler<GasStack> gasInputHandler,
      LongSupplier gasUsage,
      BoxedChemicalOutputHandler outputHandler
   ) {
      super(recipe, recheckAllErrors);
      this.itemInputHandler = Objects.requireNonNull(itemInputHandler, "Item input handler cannot be null.");
      this.gasInputHandler = Objects.requireNonNull(gasInputHandler, "Gas input handler cannot be null.");
      this.gasUsage = Objects.requireNonNull(gasUsage, "Gas usage cannot be null.");
      this.outputHandler = Objects.requireNonNull(outputHandler, "Input handler cannot be null.");
   }

   @Override
   protected void setupVariableValues() {
      this.gasUsageMultiplier = Math.max(this.gasUsage.getAsLong(), 0L);
   }

   @Override
   protected void calculateOperationsThisTick(CachedRecipe.OperationTracker tracker) {
      super.calculateOperationsThisTick(tracker);
      if (tracker.shouldContinueChecking()) {
         this.recipeItem = this.itemInputHandler.getRecipeInput(this.recipe.getItemInput());
         if (this.recipeItem.m_41619_()) {
            tracker.mismatchedRecipe();
         } else {
            this.recipeGas = this.gasInputHandler.getRecipeInput(this.recipe.getGasInput());
            if (this.recipeGas.isEmpty()) {
               tracker.updateOperations(0);
               if (!tracker.shouldContinueChecking()) {
                  return;
               }
            }

            this.itemInputHandler.calculateOperationsCanSupport(tracker, this.recipeItem);
            if (!this.recipeGas.isEmpty() && tracker.shouldContinueChecking()) {
               this.gasInputHandler.calculateOperationsCanSupport(tracker, this.recipeGas, this.gasUsageMultiplier);
               if (tracker.shouldContinueChecking()) {
                  this.output = this.recipe.getOutput(this.recipeItem, this.recipeGas);
                  this.outputHandler.calculateOperationsRoomFor(tracker, this.output);
               }
            }
         }
      }
   }

   @Override
   public boolean isInputValid() {
      ItemStack itemInput = this.itemInputHandler.getInput();
      if (!itemInput.m_41619_()) {
         GasStack gasStack = this.gasInputHandler.getInput();
         if (!gasStack.isEmpty() && this.recipe.test(itemInput, gasStack)) {
            GasStack recipeGas = this.gasInputHandler.getRecipeInput(this.recipe.getGasInput());
            return !recipeGas.isEmpty() && gasStack.getAmount() >= recipeGas.getAmount();
         }
      }

      return false;
   }

   @Override
   protected void useResources(int operations) {
      super.useResources(operations);
      if (this.gasUsageMultiplier > 0L) {
         if (!this.recipeGas.isEmpty()) {
            this.gasInputHandler.use(this.recipeGas, operations * this.gasUsageMultiplier);
         }
      }
   }

   @Override
   protected void finishProcessing(int operations) {
      if (!this.recipeItem.m_41619_() && !this.recipeGas.isEmpty() && !this.output.isEmpty()) {
         this.itemInputHandler.use(this.recipeItem, operations);
         if (this.gasUsageMultiplier > 0L) {
            this.gasInputHandler.use(this.recipeGas, operations * this.gasUsageMultiplier);
         }

         this.outputHandler.handleOutput(this.output, operations);
      }
   }
}
