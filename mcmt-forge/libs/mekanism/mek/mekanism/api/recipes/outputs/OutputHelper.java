package mekanism.api.recipes.outputs;

import java.util.Objects;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.MathUtils;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

@NothingNullByDefault
public class OutputHelper {
   private OutputHelper() {
   }

   public static <STACK extends ChemicalStack<?>> IOutputHandler<STACK> getOutputHandler(
      final IChemicalTank<?, STACK> tank, final CachedRecipe.OperationTracker.RecipeError notEnoughSpaceError
   ) {
      Objects.requireNonNull(tank, "Tank cannot be null.");
      Objects.requireNonNull(notEnoughSpaceError, "Not enough space error cannot be null.");
      return new IOutputHandler<STACK>() {
         public void handleOutput(STACK toOutput, int operations) {
            OutputHelper.handleOutput(tank, toOutput, operations);
         }

         public void calculateOperationsCanSupport(CachedRecipe.OperationTracker tracker, STACK toOutput) {
            OutputHelper.calculateOperationsCanSupport(tracker, notEnoughSpaceError, tank, toOutput);
         }
      };
   }

   public static IOutputHandler<FluidStack> getOutputHandler(final IExtendedFluidTank tank, final CachedRecipe.OperationTracker.RecipeError notEnoughSpaceError) {
      Objects.requireNonNull(tank, "Tank cannot be null.");
      Objects.requireNonNull(notEnoughSpaceError, "Not enough space error cannot be null.");
      return new IOutputHandler<FluidStack>() {
         public void handleOutput(FluidStack toOutput, int operations) {
            OutputHelper.handleOutput(tank, toOutput, operations);
         }

         public void calculateOperationsCanSupport(CachedRecipe.OperationTracker tracker, FluidStack toOutput) {
            OutputHelper.calculateOperationsCanSupport(tracker, notEnoughSpaceError, tank, toOutput);
         }
      };
   }

   public static IOutputHandler<ItemStack> getOutputHandler(final IInventorySlot slot, final CachedRecipe.OperationTracker.RecipeError notEnoughSpaceError) {
      Objects.requireNonNull(slot, "Slot cannot be null.");
      Objects.requireNonNull(notEnoughSpaceError, "Not enough space error cannot be null.");
      return new IOutputHandler<ItemStack>() {
         public void handleOutput(ItemStack toOutput, int operations) {
            OutputHelper.handleOutput(slot, toOutput, operations);
         }

         public void calculateOperationsCanSupport(CachedRecipe.OperationTracker tracker, ItemStack toOutput) {
            OutputHelper.calculateOperationsCanSupport(tracker, notEnoughSpaceError, slot, toOutput);
         }
      };
   }

   public static IOutputHandler<SawmillRecipe.ChanceOutput> getOutputHandler(
      final IInventorySlot mainSlot,
      final CachedRecipe.OperationTracker.RecipeError mainSlotNotEnoughSpaceError,
      final IInventorySlot secondarySlot,
      final CachedRecipe.OperationTracker.RecipeError secondarySlotNotEnoughSpaceError
   ) {
      Objects.requireNonNull(mainSlot, "Main slot cannot be null.");
      Objects.requireNonNull(secondarySlot, "Secondary/Extra slot cannot be null.");
      Objects.requireNonNull(mainSlotNotEnoughSpaceError, "Main slot not enough space error cannot be null.");
      Objects.requireNonNull(secondarySlotNotEnoughSpaceError, "Secondary/Extra slot not enough space error cannot be null.");
      return new IOutputHandler<SawmillRecipe.ChanceOutput>() {
         public void handleOutput(SawmillRecipe.ChanceOutput toOutput, int operations) {
            OutputHelper.handleOutput(mainSlot, toOutput.getMainOutput(), operations);
            ItemStack secondaryOutput = toOutput.getSecondaryOutput();

            for (int i = 0; i < operations; i++) {
               OutputHelper.handleOutput(secondarySlot, secondaryOutput, operations);
               if (i < operations - 1) {
                  secondaryOutput = toOutput.nextSecondaryOutput();
               }
            }
         }

         public void calculateOperationsCanSupport(CachedRecipe.OperationTracker tracker, SawmillRecipe.ChanceOutput toOutput) {
            OutputHelper.calculateOperationsCanSupport(tracker, mainSlotNotEnoughSpaceError, mainSlot, toOutput.getMainOutput());
            if (tracker.shouldContinueChecking()) {
               OutputHelper.calculateOperationsCanSupport(tracker, secondarySlotNotEnoughSpaceError, secondarySlot, toOutput.getMaxSecondaryOutput());
            }
         }
      };
   }

   public static IOutputHandler<PressurizedReactionRecipe.PressurizedReactionRecipeOutput> getOutputHandler(
      final IInventorySlot slot,
      final CachedRecipe.OperationTracker.RecipeError slotNotEnoughSpaceError,
      final IGasTank tank,
      final CachedRecipe.OperationTracker.RecipeError tankNotEnoughSpaceError
   ) {
      Objects.requireNonNull(slot, "Slot cannot be null.");
      Objects.requireNonNull(tank, "Tank cannot be null.");
      Objects.requireNonNull(slotNotEnoughSpaceError, "Slot not enough space error cannot be null.");
      Objects.requireNonNull(tankNotEnoughSpaceError, "Tank not enough space error cannot be null.");
      return new IOutputHandler<PressurizedReactionRecipe.PressurizedReactionRecipeOutput>() {
         public void handleOutput(PressurizedReactionRecipe.PressurizedReactionRecipeOutput toOutput, int operations) {
            OutputHelper.handleOutput(slot, toOutput.item(), operations);
            OutputHelper.handleOutput(tank, toOutput.gas(), operations);
         }

         public void calculateOperationsCanSupport(CachedRecipe.OperationTracker tracker, PressurizedReactionRecipe.PressurizedReactionRecipeOutput toOutput) {
            OutputHelper.calculateOperationsCanSupport(tracker, slotNotEnoughSpaceError, slot, toOutput.item());
            if (tracker.shouldContinueChecking()) {
               OutputHelper.calculateOperationsCanSupport(tracker, tankNotEnoughSpaceError, tank, toOutput.gas());
            }
         }
      };
   }

   public static IOutputHandler<ElectrolysisRecipe.ElectrolysisRecipeOutput> getOutputHandler(
      final IGasTank leftTank,
      final CachedRecipe.OperationTracker.RecipeError leftNotEnoughSpaceError,
      final IGasTank rightTank,
      final CachedRecipe.OperationTracker.RecipeError rightNotEnoughSpaceError
   ) {
      Objects.requireNonNull(leftTank, "Left tank cannot be null.");
      Objects.requireNonNull(rightTank, "Right tank cannot be null.");
      Objects.requireNonNull(leftNotEnoughSpaceError, "Left not enough space error cannot be null.");
      Objects.requireNonNull(rightNotEnoughSpaceError, "Right not enough space error cannot be null.");
      return new IOutputHandler<ElectrolysisRecipe.ElectrolysisRecipeOutput>() {
         public void handleOutput(ElectrolysisRecipe.ElectrolysisRecipeOutput toOutput, int operations) {
            OutputHelper.handleOutput(leftTank, toOutput.left(), operations);
            OutputHelper.handleOutput(rightTank, toOutput.right(), operations);
         }

         public void calculateOperationsCanSupport(CachedRecipe.OperationTracker tracker, ElectrolysisRecipe.ElectrolysisRecipeOutput toOutput) {
            OutputHelper.calculateOperationsCanSupport(tracker, leftNotEnoughSpaceError, leftTank, toOutput.left());
            if (tracker.shouldContinueChecking()) {
               OutputHelper.calculateOperationsCanSupport(tracker, rightNotEnoughSpaceError, rightTank, toOutput.right());
            }
         }
      };
   }

   static <STACK extends ChemicalStack<?>> void handleOutput(IChemicalTank<?, STACK> tank, STACK toOutput, int operations) {
      if (operations != 0) {
         STACK output = tank.createStack(toOutput, toOutput.getAmount() * operations);
         tank.insert(output, Action.EXECUTE, AutomationType.INTERNAL);
      }
   }

   private static void handleOutput(IExtendedFluidTank fluidTank, FluidStack toOutput, int operations) {
      if (operations != 0) {
         fluidTank.insert(new FluidStack(toOutput, toOutput.getAmount() * operations), Action.EXECUTE, AutomationType.INTERNAL);
      }
   }

   private static void handleOutput(IInventorySlot inventorySlot, ItemStack toOutput, int operations) {
      if (operations != 0 && !toOutput.m_41619_()) {
         ItemStack output = toOutput.m_41777_();
         if (operations > 1) {
            output.m_41764_(output.m_41613_() * operations);
         }

         inventorySlot.insertItem(output, Action.EXECUTE, AutomationType.INTERNAL);
      }
   }

   static <STACK extends ChemicalStack<?>> void calculateOperationsCanSupport(
      CachedRecipe.OperationTracker tracker, CachedRecipe.OperationTracker.RecipeError notEnoughSpace, IChemicalTank<?, STACK> tank, STACK toOutput
   ) {
      if (!toOutput.isEmpty()) {
         STACK maxOutput = tank.createStack(toOutput, Long.MAX_VALUE);
         STACK remainder = tank.insert(maxOutput, Action.SIMULATE, AutomationType.INTERNAL);
         long amountUsed = maxOutput.getAmount() - remainder.getAmount();
         int operations = MathUtils.clampToInt(amountUsed / toOutput.getAmount());
         tracker.updateOperations(operations);
         if (operations == 0) {
            if (amountUsed == 0L && tank.getNeeded() > 0L) {
               tracker.addError(CachedRecipe.OperationTracker.RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT);
            } else {
               tracker.addError(notEnoughSpace);
            }
         }
      }
   }

   private static void calculateOperationsCanSupport(
      CachedRecipe.OperationTracker tracker, CachedRecipe.OperationTracker.RecipeError notEnoughSpace, IExtendedFluidTank tank, FluidStack toOutput
   ) {
      if (!toOutput.isEmpty()) {
         FluidStack maxOutput = new FluidStack(toOutput, Integer.MAX_VALUE);
         FluidStack remainder = tank.insert(maxOutput, Action.SIMULATE, AutomationType.INTERNAL);
         int amountUsed = maxOutput.getAmount() - remainder.getAmount();
         int operations = amountUsed / toOutput.getAmount();
         tracker.updateOperations(operations);
         if (operations == 0) {
            if (amountUsed == 0 && tank.getNeeded() > 0) {
               tracker.addError(CachedRecipe.OperationTracker.RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT);
            } else {
               tracker.addError(notEnoughSpace);
            }
         }
      }
   }

   private static void calculateOperationsCanSupport(
      CachedRecipe.OperationTracker tracker, CachedRecipe.OperationTracker.RecipeError notEnoughSpace, IInventorySlot slot, ItemStack toOutput
   ) {
      if (!toOutput.m_41619_()) {
         ItemStack output = toOutput.m_255036_(toOutput.m_41741_());
         ItemStack remainder = slot.insertItem(output, Action.SIMULATE, AutomationType.INTERNAL);
         int amountUsed = output.m_41613_() - remainder.m_41613_();
         int operations = amountUsed / toOutput.m_41613_();
         tracker.updateOperations(operations);
         if (operations == 0) {
            if (amountUsed == 0 && slot.getLimit(slot.getStack()) - slot.getCount() > 0) {
               tracker.addError(CachedRecipe.OperationTracker.RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT);
            } else {
               tracker.addError(notEnoughSpace);
            }
         }
      }
   }
}
