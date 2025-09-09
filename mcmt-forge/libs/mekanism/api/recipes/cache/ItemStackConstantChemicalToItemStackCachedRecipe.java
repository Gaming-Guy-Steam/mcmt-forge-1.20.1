package mekanism.api.recipes.cache;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.LongConsumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.chemical.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.ILongInputHandler;
import mekanism.api.recipes.outputs.IOutputHandler;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ItemStackConstantChemicalToItemStackCachedRecipe<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK>, RECIPE extends ItemStackChemicalToItemStackRecipe<CHEMICAL, STACK, INGREDIENT>>
   extends CachedRecipe<RECIPE> {
   private final IOutputHandler<ItemStack> outputHandler;
   private final IInputHandler<ItemStack> itemInputHandler;
   private final ILongInputHandler<STACK> chemicalInputHandler;
   private final ItemStackConstantChemicalToItemStackCachedRecipe.ChemicalUsageMultiplier chemicalUsage;
   private final LongConsumer chemicalUsedSoFarChanged;
   private long chemicalUsageMultiplier;
   private long chemicalUsedSoFar;
   private ItemStack recipeItem = ItemStack.f_41583_;
   @Nullable
   private STACK recipeChemical;
   private ItemStack output = ItemStack.f_41583_;

   public ItemStackConstantChemicalToItemStackCachedRecipe(
      RECIPE recipe,
      BooleanSupplier recheckAllErrors,
      IInputHandler<ItemStack> itemInputHandler,
      ILongInputHandler<STACK> chemicalInputHandler,
      ItemStackConstantChemicalToItemStackCachedRecipe.ChemicalUsageMultiplier chemicalUsage,
      LongConsumer chemicalUsedSoFarChanged,
      IOutputHandler<ItemStack> outputHandler
   ) {
      super(recipe, recheckAllErrors);
      this.itemInputHandler = Objects.requireNonNull(itemInputHandler, "Item input handler cannot be null.");
      this.chemicalInputHandler = Objects.requireNonNull(chemicalInputHandler, "Chemical input handler cannot be null.");
      this.chemicalUsage = Objects.requireNonNull(chemicalUsage, "Chemical usage cannot be null.");
      this.chemicalUsedSoFarChanged = Objects.requireNonNull(chemicalUsedSoFarChanged, "Chemical used so far changed handler cannot be null.");
      this.outputHandler = Objects.requireNonNull(outputHandler, "Output handler cannot be null.");
   }

   public void loadSavedUsageSoFar(long chemicalUsedSoFar) {
      if (chemicalUsedSoFar > 0L) {
         this.chemicalUsedSoFar = chemicalUsedSoFar;
      }
   }

   @Override
   protected void setupVariableValues() {
      this.chemicalUsageMultiplier = Math.max(this.chemicalUsage.getToUse(this.chemicalUsedSoFar, this.getOperatingTicks()), 0L);
   }

   @Override
   protected void calculateOperationsThisTick(CachedRecipe.OperationTracker tracker) {
      super.calculateOperationsThisTick(tracker);
      if (tracker.shouldContinueChecking()) {
         this.recipeItem = this.itemInputHandler.getRecipeInput(this.recipe.getItemInput());
         if (this.recipeItem.m_41619_()) {
            tracker.mismatchedRecipe();
         } else {
            this.recipeChemical = this.chemicalInputHandler.getRecipeInput(this.recipe.getChemicalInput());
            if (this.recipeChemical.isEmpty()) {
               tracker.updateOperations(0);
               if (!tracker.shouldContinueChecking()) {
                  return;
               }
            }

            this.itemInputHandler.calculateOperationsCanSupport(tracker, this.recipeItem);
            if (!this.recipeChemical.isEmpty() && tracker.shouldContinueChecking()) {
               this.chemicalInputHandler.calculateOperationsCanSupport(tracker, this.recipeChemical, this.chemicalUsageMultiplier);
               if (tracker.shouldContinueChecking()) {
                  this.output = this.recipe.getOutput(this.recipeItem, this.recipeChemical);
                  this.outputHandler.calculateOperationsCanSupport(tracker, this.output);
               }
            }
         }
      }
   }

   @Override
   public boolean isInputValid() {
      ItemStack itemInput = this.itemInputHandler.getInput();
      if (!itemInput.m_41619_()) {
         STACK chemicalStack = this.chemicalInputHandler.getInput();
         if (!chemicalStack.isEmpty() && this.recipe.test(itemInput, chemicalStack)) {
            STACK recipeChemical = this.chemicalInputHandler.getRecipeInput(this.recipe.getChemicalInput());
            return !recipeChemical.isEmpty() && chemicalStack.getAmount() >= recipeChemical.getAmount();
         }
      }

      return false;
   }

   @Override
   protected void useResources(int operations) {
      super.useResources(operations);
      if (this.chemicalUsageMultiplier > 0L) {
         if (this.recipeChemical != null && !this.recipeChemical.isEmpty()) {
            long toUse = operations * this.chemicalUsageMultiplier;
            this.chemicalInputHandler.use(this.recipeChemical, toUse);
            this.chemicalUsedSoFar += toUse;
            this.chemicalUsedSoFarChanged.accept(this.chemicalUsedSoFar);
         }
      }
   }

   @Override
   protected void resetCache() {
      super.resetCache();
      this.chemicalUsedSoFar = 0L;
      this.chemicalUsedSoFarChanged.accept(this.chemicalUsedSoFar);
   }

   @Override
   protected void finishProcessing(int operations) {
      if (this.recipeChemical != null && !this.recipeItem.m_41619_() && !this.recipeChemical.isEmpty() && !this.output.m_41619_()) {
         this.itemInputHandler.use(this.recipeItem, operations);
         if (this.chemicalUsageMultiplier > 0L) {
            this.chemicalInputHandler.use(this.recipeChemical, operations * this.chemicalUsageMultiplier);
         }

         this.outputHandler.handleOutput(this.output, operations);
      }
   }

   @FunctionalInterface
   public interface ChemicalUsageMultiplier {
      long getToUse(long var1, int var3);
   }
}
