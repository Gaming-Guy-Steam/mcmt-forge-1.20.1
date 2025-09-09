package mekanism.api.recipes.inputs;

import java.util.Objects;
import mekanism.api.Action;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.MathUtils;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.ingredients.InputIngredient;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public class InputHelper {
   private InputHelper() {
   }

   public static IInputHandler<ItemStack> getInputHandler(final IInventorySlot slot, final CachedRecipe.OperationTracker.RecipeError notEnoughError) {
      Objects.requireNonNull(slot, "Slot cannot be null.");
      Objects.requireNonNull(notEnoughError, "Not enough input error cannot be null.");
      return new IInputHandler<ItemStack>() {
         public ItemStack getInput() {
            return slot.getStack();
         }

         public ItemStack getRecipeInput(InputIngredient<ItemStack> recipeIngredient) {
            ItemStack input = this.getInput();
            return input.m_41619_() ? ItemStack.f_41583_ : recipeIngredient.getMatchingInstance(input);
         }

         public void use(ItemStack recipeInput, int operations) {
            if (operations != 0) {
               if (!recipeInput.m_41619_()) {
                  int amount = recipeInput.m_41613_() * operations;
                  InputHelper.logMismatchedStackSize((long)slot.shrinkStack(amount, Action.EXECUTE), (long)amount);
               }
            }
         }

         public void calculateOperationsCanSupport(CachedRecipe.OperationTracker tracker, ItemStack recipeInput, int usageMultiplier) {
            if (usageMultiplier > 0) {
               if (!recipeInput.m_41619_()) {
                  int operations = this.getInput().m_41613_() / (recipeInput.m_41613_() * usageMultiplier);
                  if (operations > 0) {
                     tracker.updateOperations(operations);
                     return;
                  }
               }

               tracker.resetProgress(notEnoughError);
            }
         }
      };
   }

   public static <STACK extends ChemicalStack<?>> ILongInputHandler<STACK> getInputHandler(
      IChemicalTank<?, STACK> tank, CachedRecipe.OperationTracker.RecipeError notEnoughError
   ) {
      Objects.requireNonNull(tank, "Tank cannot be null.");
      Objects.requireNonNull(notEnoughError, "Not enough input error cannot be null.");
      return new InputHelper.ChemicalInputHandler<>(tank, notEnoughError);
   }

   public static <STACK extends ChemicalStack<?>> ILongInputHandler<STACK> getConstantInputHandler(IChemicalTank<?, STACK> tank) {
      Objects.requireNonNull(tank, "Tank cannot be null.");
      return new InputHelper.ChemicalInputHandler<STACK>(tank, CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_SECONDARY_INPUT) {
         @Override
         protected void resetProgress(CachedRecipe.OperationTracker tracker) {
            tracker.updateOperations(0);
         }
      };
   }

   public static IInputHandler<FluidStack> getInputHandler(final IExtendedFluidTank tank, final CachedRecipe.OperationTracker.RecipeError notEnoughError) {
      Objects.requireNonNull(tank, "Tank cannot be null.");
      Objects.requireNonNull(notEnoughError, "Not enough input error cannot be null.");
      return new IInputHandler<FluidStack>() {
         @NotNull
         public FluidStack getInput() {
            return tank.getFluid();
         }

         @NotNull
         public FluidStack getRecipeInput(InputIngredient<FluidStack> recipeIngredient) {
            FluidStack input = this.getInput();
            return input.isEmpty() ? FluidStack.EMPTY : recipeIngredient.getMatchingInstance(input);
         }

         public void use(FluidStack recipeInput, int operations) {
            if (operations != 0 && !recipeInput.isEmpty()) {
               FluidStack inputFluid = this.getInput();
               if (!inputFluid.isEmpty()) {
                  int amount = recipeInput.getAmount() * operations;
                  InputHelper.logMismatchedStackSize((long)tank.shrinkStack(amount, Action.EXECUTE), (long)amount);
               }
            }
         }

         public void calculateOperationsCanSupport(CachedRecipe.OperationTracker tracker, FluidStack recipeInput, int usageMultiplier) {
            if (usageMultiplier > 0) {
               if (!recipeInput.isEmpty()) {
                  int operations = this.getInput().getAmount() / (recipeInput.getAmount() * usageMultiplier);
                  if (operations > 0) {
                     tracker.updateOperations(operations);
                     return;
                  }
               }

               tracker.resetProgress(notEnoughError);
            }
         }
      };
   }

   private static void logMismatchedStackSize(long actual, long expected) {
      if (expected != actual) {
         MekanismAPI.logger.error("Stack size changed by a different amount ({}) than requested ({}).", new Object[]{actual, expected, new Exception()});
      }
   }

   private static class ChemicalInputHandler<STACK extends ChemicalStack<?>> implements ILongInputHandler<STACK> {
      private final IChemicalTank<?, STACK> tank;
      private final CachedRecipe.OperationTracker.RecipeError notEnoughError;

      private ChemicalInputHandler(IChemicalTank<?, STACK> tank, CachedRecipe.OperationTracker.RecipeError notEnoughError) {
         this.tank = tank;
         this.notEnoughError = notEnoughError;
      }

      @NotNull
      public STACK getInput() {
         return this.tank.getStack();
      }

      @NotNull
      public STACK getRecipeInput(InputIngredient<STACK> recipeIngredient) {
         STACK input = this.getInput();
         return input.isEmpty() ? this.tank.getEmptyStack() : recipeIngredient.getMatchingInstance(input);
      }

      public void use(STACK recipeInput, long operations) {
         if (operations != 0L && !recipeInput.isEmpty()) {
            STACK inputGas = this.getInput();
            if (!inputGas.isEmpty()) {
               long amount = recipeInput.getAmount() * operations;
               InputHelper.logMismatchedStackSize(this.tank.shrinkStack(amount, Action.EXECUTE), amount);
            }
         }
      }

      public void calculateOperationsCanSupport(CachedRecipe.OperationTracker tracker, STACK recipeInput, long usageMultiplier) {
         if (usageMultiplier > 0L) {
            if (!recipeInput.isEmpty()) {
               int operations = MathUtils.clampToInt(this.getInput().getAmount() / (recipeInput.getAmount() * usageMultiplier));
               if (operations > 0) {
                  tracker.updateOperations(operations);
                  return;
               }
            }

            this.resetProgress(tracker);
         }
      }

      protected void resetProgress(CachedRecipe.OperationTracker tracker) {
         tracker.resetProgress(this.notEnoughError);
      }
   }
}
