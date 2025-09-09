package mekanism.common.capabilities.fluid;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.lib.multiblock.MultiblockData;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class VariableCapacityFluidTank extends BasicFluidTank {
   private final IntSupplier capacity;

   public static VariableCapacityFluidTank create(
      MultiblockData multiblock, IntSupplier capacity, Predicate<FluidStack> validator, @Nullable IContentsListener listener
   ) {
      Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
      Objects.requireNonNull(validator, "Fluid validity check cannot be null");
      return new VariableCapacityFluidTank(capacity, multiblock.formedBiPred(), multiblock.formedBiPred(), validator, listener);
   }

   public static VariableCapacityFluidTank input(
      MultiblockData multiblock, IntSupplier capacity, Predicate<FluidStack> validator, @Nullable IContentsListener listener
   ) {
      return create(capacity, multiblock.notExternalFormedBiPred(), multiblock.formedBiPred(), validator, listener);
   }

   public static VariableCapacityFluidTank output(
      MultiblockData multiblock, IntSupplier capacity, Predicate<FluidStack> validator, @Nullable IContentsListener listener
   ) {
      return create(capacity, multiblock.formedBiPred(), multiblock.notExternalFormedBiPred(), validator, listener);
   }

   public static VariableCapacityFluidTank input(IntSupplier capacity, Predicate<FluidStack> validator, @Nullable IContentsListener listener) {
      Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
      Objects.requireNonNull(validator, "Fluid validity check cannot be null");
      return new VariableCapacityFluidTank(capacity, notExternal, alwaysTrueBi, validator, listener);
   }

   public static VariableCapacityFluidTank output(IntSupplier capacity, Predicate<FluidStack> validator, @Nullable IContentsListener listener) {
      Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
      Objects.requireNonNull(validator, "Fluid validity check cannot be null");
      return new VariableCapacityFluidTank(capacity, alwaysTrueBi, internalOnly, validator, listener);
   }

   public static VariableCapacityFluidTank create(
      IntSupplier capacity,
      BiPredicate<FluidStack, AutomationType> canExtract,
      BiPredicate<FluidStack, AutomationType> canInsert,
      Predicate<FluidStack> validator,
      @Nullable IContentsListener listener
   ) {
      Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
      Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
      Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
      Objects.requireNonNull(validator, "Fluid validity check cannot be null");
      return new VariableCapacityFluidTank(capacity, canExtract, canInsert, validator, listener);
   }

   protected VariableCapacityFluidTank(
      IntSupplier capacity,
      BiPredicate<FluidStack, AutomationType> canExtract,
      BiPredicate<FluidStack, AutomationType> canInsert,
      Predicate<FluidStack> validator,
      @Nullable IContentsListener listener
   ) {
      super(capacity.getAsInt(), canExtract, canInsert, validator, listener);
      this.capacity = capacity;
   }

   @Override
   public int getCapacity() {
      return this.capacity.getAsInt();
   }

   @Override
   public int setStackSize(int amount, @NotNull Action action) {
      if (this.isEmpty()) {
         return 0;
      } else if (amount <= 0) {
         if (action.execute()) {
            this.setEmpty();
         }

         return 0;
      } else {
         int maxStackSize = this.getCapacity();
         if (maxStackSize > 0 && amount > maxStackSize) {
            amount = maxStackSize;
         }

         if (this.getFluidAmount() != amount && !action.simulate()) {
            this.stored.setAmount(amount);
            this.onContentsChanged();
            return amount;
         } else {
            return amount;
         }
      }
   }
}
