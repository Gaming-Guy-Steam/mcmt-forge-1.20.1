package mekanism.api.recipes.cache;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongConsumer;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.api.recipes.MekanismRecipe;

@NothingNullByDefault
public abstract class CachedRecipe<RECIPE extends MekanismRecipe> {
   protected final RECIPE recipe;
   private Set<CachedRecipe.OperationTracker.RecipeError> errors = Collections.emptySet();
   private final BooleanSupplier recheckAllErrors;
   private BooleanSupplier canHolderFunction = () -> true;
   private BooleanConsumer setActive = active -> {};
   private IntSupplier requiredTicks = () -> 1;
   private Runnable onFinish = () -> {};
   private FloatingLongSupplier perTickEnergy = () -> FloatingLong.ZERO;
   private FloatingLongSupplier storedEnergy = () -> FloatingLong.ZERO;
   private FloatingLongConsumer useEnergy = energy -> {};
   private IntSupplier baselineMaxOperations = () -> 1;
   private Consumer<CachedRecipe.OperationTracker> postProcessOperations = tracker -> {};
   private Consumer<Set<CachedRecipe.OperationTracker.RecipeError>> onErrorsChange = errors -> {};
   private int operatingTicks;
   private IntConsumer operatingTicksChanged = ticks -> {};

   protected CachedRecipe(RECIPE recipe, BooleanSupplier recheckAllErrors) {
      this.recipe = Objects.requireNonNull(recipe, "Recipe cannot be null.");
      this.recheckAllErrors = Objects.requireNonNull(recheckAllErrors, "Recheck all errors supplier cannot be null.");
   }

   public CachedRecipe<RECIPE> setCanHolderFunction(BooleanSupplier canHolderFunction) {
      this.canHolderFunction = Objects.requireNonNull(canHolderFunction, "Can holder function cannot be null.");
      return this;
   }

   public CachedRecipe<RECIPE> setActive(BooleanConsumer setActive) {
      this.setActive = Objects.requireNonNull(setActive, "Set active consumer cannot be null.");
      return this;
   }

   public CachedRecipe<RECIPE> setEnergyRequirements(FloatingLongSupplier perTickEnergy, IEnergyContainer energyContainer) {
      this.perTickEnergy = Objects.requireNonNull(perTickEnergy, "The per tick energy cannot be null.");
      Objects.requireNonNull(energyContainer, "Energy container cannot be null.");
      this.storedEnergy = energyContainer::getEnergy;
      this.useEnergy = energy -> energyContainer.extract(energy, Action.EXECUTE, AutomationType.INTERNAL);
      return this;
   }

   public CachedRecipe<RECIPE> setRequiredTicks(IntSupplier requiredTicks) {
      this.requiredTicks = Objects.requireNonNull(requiredTicks, "Required ticks cannot be null.");
      return this;
   }

   public CachedRecipe<RECIPE> setOperatingTicksChanged(IntConsumer operatingTicksChanged) {
      this.operatingTicksChanged = Objects.requireNonNull(operatingTicksChanged, "Operating ticks changed handler cannot be null.");
      return this;
   }

   public CachedRecipe<RECIPE> setOnFinish(Runnable onFinish) {
      this.onFinish = Objects.requireNonNull(onFinish, "On finish handling cannot be null.");
      return this;
   }

   public CachedRecipe<RECIPE> setBaselineMaxOperations(IntSupplier baselineMaxOperations) {
      this.baselineMaxOperations = Objects.requireNonNull(baselineMaxOperations, "Baseline max operations cannot be null.");
      return this;
   }

   public CachedRecipe<RECIPE> setPostProcessOperations(Consumer<CachedRecipe.OperationTracker> postProcessOperations) {
      this.postProcessOperations = Objects.requireNonNull(postProcessOperations, "Post processing of the operation count cannot be null.");
      return this;
   }

   public CachedRecipe<RECIPE> setErrorsChanged(Consumer<Set<CachedRecipe.OperationTracker.RecipeError>> onErrorsChange) {
      this.onErrorsChange = Objects.requireNonNull(onErrorsChange, "On errors change consumer cannot be null.");
      return this;
   }

   private void updateErrors(Set<CachedRecipe.OperationTracker.RecipeError> errors) {
      if (!this.errors.equals(errors)) {
         this.errors = errors;
         this.onErrorsChange.accept(errors);
      }
   }

   public void loadSavedOperatingTicks(int operatingTicks) {
      if (operatingTicks > 0 && operatingTicks < this.requiredTicks.getAsInt()) {
         this.operatingTicks = operatingTicks;
      }
   }

   public void process() {
      int operations;
      if (this.canHolderFunction.getAsBoolean()) {
         this.setupVariableValues();
         CachedRecipe.OperationTracker tracker = new CachedRecipe.OperationTracker(
            this.errors, this.recheckAllErrors.getAsBoolean(), this.baselineMaxOperations.getAsInt()
         );
         this.calculateOperationsThisTick(tracker);
         if (tracker.shouldContinueChecking()) {
            this.postProcessOperations.accept(tracker);
            if (tracker.shouldContinueChecking() && tracker.capAtMaxForEnergy()) {
               tracker.addError(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY_REDUCED_RATE);
            }
         }

         operations = tracker.currentMax;
         if (tracker.hasErrorsToCopy()) {
            this.updateErrors(tracker.errors);
         }
      } else {
         operations = 0;
         if (!this.errors.isEmpty()) {
            this.updateErrors(Collections.emptySet());
         }
      }

      if (operations > 0) {
         this.setActive.accept(true);
         this.useEnergy(operations);
         this.operatingTicks++;
         int ticksRequired = this.requiredTicks.getAsInt();
         if (this.operatingTicks >= ticksRequired) {
            this.operatingTicks = 0;
            this.finishProcessing(operations);
            this.onFinish.run();
            this.resetCache();
         } else {
            this.useResources(operations);
         }

         if (ticksRequired > 1) {
            this.operatingTicksChanged.accept(this.operatingTicks);
         }
      } else {
         this.setActive.accept(false);
         if (operations < 0) {
            this.operatingTicks = 0;
            this.operatingTicksChanged.accept(this.operatingTicks);
            this.resetCache();
         }
      }
   }

   protected void setupVariableValues() {
   }

   protected int getOperatingTicks() {
      return this.operatingTicks;
   }

   protected void useResources(int operations) {
   }

   protected void resetCache() {
   }

   protected void useEnergy(int operations) {
      FloatingLong energy = this.perTickEnergy.get();
      if (operations == 1) {
         this.useEnergy.accept(energy);
      } else {
         this.useEnergy.accept(energy.multiply((long)operations));
      }
   }

   protected void calculateOperationsThisTick(CachedRecipe.OperationTracker tracker) {
      if (tracker.shouldContinueChecking()) {
         FloatingLong energyPerTick = this.perTickEnergy.get();
         if (!energyPerTick.isZero()) {
            int operations = this.storedEnergy.get().divideToInt(energyPerTick);
            tracker.maxForEnergy = operations;
            if (operations == 0) {
               tracker.updateOperations(operations);
               tracker.addError(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY);
            }
         }
      }
   }

   public abstract boolean isInputValid();

   protected abstract void finishProcessing(int var1);

   public RECIPE getRecipe() {
      return this.recipe;
   }

   public static final class OperationTracker {
      private static final int RESET_PROGRESS = -1;
      private static final int MISMATCHED_RECIPE = -2;
      private final Set<CachedRecipe.OperationTracker.RecipeError> lastErrors;
      private Set<CachedRecipe.OperationTracker.RecipeError> errors = Collections.emptySet();
      private boolean checkAll;
      private boolean checkedErrors = true;
      private int currentMax;
      private int maxForEnergy;

      private OperationTracker(Set<CachedRecipe.OperationTracker.RecipeError> lastErrors, boolean checkAll, int startingMax) {
         this.lastErrors = lastErrors;
         this.checkAll = checkAll;
         this.currentMax = startingMax;
         this.maxForEnergy = this.currentMax;
      }

      private boolean hasErrorsToCopy() {
         if (this.currentMax == -2) {
            this.errors = Collections.emptySet();
            return true;
         } else {
            return !this.checkAll && this.currentMax <= 0 ? !this.checkedErrors && !this.lastErrors.containsAll(this.errors) : true;
         }
      }

      public boolean shouldContinueChecking() {
         if (this.currentMax > 0) {
            return true;
         } else {
            if (this.currentMax == 0) {
               if (this.checkAll) {
                  return true;
               }

               if (!this.checkedErrors) {
                  if (!this.lastErrors.containsAll(this.errors)) {
                     this.checkAll = true;
                     return true;
                  }

                  this.checkedErrors = true;
               }
            }

            return false;
         }
      }

      public boolean updateOperations(int max) {
         if (max < this.currentMax) {
            this.currentMax = max;
            return true;
         } else {
            return false;
         }
      }

      private boolean capAtMaxForEnergy() {
         return this.updateOperations(this.maxForEnergy);
      }

      public void mismatchedRecipe() {
         this.updateOperations(-2);
      }

      public void resetProgress(CachedRecipe.OperationTracker.RecipeError error) {
         this.updateOperations(-1);
         this.addError(error);
      }

      public void addError(CachedRecipe.OperationTracker.RecipeError error) {
         Objects.requireNonNull(error, "Error cannot be null.");
         if (this.errors.isEmpty()) {
            this.errors = new ObjectArraySet();
         }

         if (this.errors.add(error)) {
            this.checkedErrors = false;
         }
      }

      public static final class RecipeError {
         public static final CachedRecipe.OperationTracker.RecipeError INPUT_DOESNT_PRODUCE_OUTPUT = create();
         public static final CachedRecipe.OperationTracker.RecipeError NOT_ENOUGH_ENERGY = create();
         public static final CachedRecipe.OperationTracker.RecipeError NOT_ENOUGH_ENERGY_REDUCED_RATE = create();
         public static final CachedRecipe.OperationTracker.RecipeError NOT_ENOUGH_INPUT = create();
         public static final CachedRecipe.OperationTracker.RecipeError NOT_ENOUGH_SECONDARY_INPUT = create();
         public static final CachedRecipe.OperationTracker.RecipeError NOT_ENOUGH_LEFT_INPUT = create();
         public static final CachedRecipe.OperationTracker.RecipeError NOT_ENOUGH_RIGHT_INPUT = create();
         public static final CachedRecipe.OperationTracker.RecipeError NOT_ENOUGH_OUTPUT_SPACE = create();

         public static CachedRecipe.OperationTracker.RecipeError create() {
            return new CachedRecipe.OperationTracker.RecipeError();
         }

         private RecipeError() {
         }
      }
   }
}
