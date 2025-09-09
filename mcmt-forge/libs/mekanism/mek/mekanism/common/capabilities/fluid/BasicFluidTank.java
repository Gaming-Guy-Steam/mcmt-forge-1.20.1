package mekanism.common.capabilities.fluid;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.RegistryUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class BasicFluidTank implements IExtendedFluidTank {
   public static final Predicate<FluidStack> alwaysTrue = ConstantPredicates.alwaysTrue();
   public static final Predicate<FluidStack> alwaysFalse = ConstantPredicates.alwaysFalse();
   public static final BiPredicate<FluidStack, AutomationType> alwaysTrueBi = ConstantPredicates.alwaysTrueBi();
   public static final BiPredicate<FluidStack, AutomationType> internalOnly = ConstantPredicates.internalOnly();
   public static final BiPredicate<FluidStack, AutomationType> notExternal = ConstantPredicates.notExternal();
   protected FluidStack stored = FluidStack.EMPTY;
   private final Predicate<FluidStack> validator;
   protected final BiPredicate<FluidStack, AutomationType> canExtract;
   protected final BiPredicate<FluidStack, AutomationType> canInsert;
   private final int capacity;
   @Nullable
   private final IContentsListener listener;

   public static BasicFluidTank create(int capacity, @Nullable IContentsListener listener) {
      if (capacity < 0) {
         throw new IllegalArgumentException("Capacity must be at least zero");
      } else {
         return new BasicFluidTank(capacity, alwaysTrueBi, alwaysTrueBi, alwaysTrue, listener);
      }
   }

   public static BasicFluidTank create(int capacity, Predicate<FluidStack> validator, @Nullable IContentsListener listener) {
      if (capacity < 0) {
         throw new IllegalArgumentException("Capacity must be at least zero");
      } else {
         Objects.requireNonNull(validator, "Fluid validity check cannot be null");
         return new BasicFluidTank(capacity, alwaysTrueBi, alwaysTrueBi, validator, listener);
      }
   }

   public static BasicFluidTank create(int capacity, Predicate<FluidStack> canExtract, Predicate<FluidStack> canInsert, @Nullable IContentsListener listener) {
      return create(capacity, canExtract, canInsert, alwaysTrue, listener);
   }

   public static BasicFluidTank input(int capacity, Predicate<FluidStack> validator, @Nullable IContentsListener listener) {
      if (capacity < 0) {
         throw new IllegalArgumentException("Capacity must be at least zero");
      } else {
         Objects.requireNonNull(validator, "Fluid validity check cannot be null");
         return new BasicFluidTank(capacity, notExternal, alwaysTrueBi, validator, listener);
      }
   }

   public static BasicFluidTank input(int capacity, Predicate<FluidStack> canInsert, Predicate<FluidStack> validator, @Nullable IContentsListener listener) {
      if (capacity < 0) {
         throw new IllegalArgumentException("Capacity must be at least zero");
      } else {
         Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
         Objects.requireNonNull(validator, "Fluid validity check cannot be null");
         return new BasicFluidTank(capacity, notExternal, (stack, automationType) -> canInsert.test(stack), validator, listener);
      }
   }

   public static BasicFluidTank output(int capacity, @Nullable IContentsListener listener) {
      if (capacity < 0) {
         throw new IllegalArgumentException("Capacity must be at least zero");
      } else {
         return new BasicFluidTank(capacity, alwaysTrueBi, internalOnly, alwaysTrue, listener);
      }
   }

   public static BasicFluidTank create(
      int capacity, Predicate<FluidStack> canExtract, Predicate<FluidStack> canInsert, Predicate<FluidStack> validator, @Nullable IContentsListener listener
   ) {
      if (capacity < 0) {
         throw new IllegalArgumentException("Capacity must be at least zero");
      } else {
         Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
         Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
         Objects.requireNonNull(validator, "Fluid validity check cannot be null");
         return new BasicFluidTank(capacity, canExtract, canInsert, validator, listener);
      }
   }

   public static BasicFluidTank create(
      int capacity,
      BiPredicate<FluidStack, AutomationType> canExtract,
      BiPredicate<FluidStack, AutomationType> canInsert,
      Predicate<FluidStack> validator,
      @Nullable IContentsListener listener
   ) {
      if (capacity < 0) {
         throw new IllegalArgumentException("Capacity must be at least zero");
      } else {
         Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
         Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
         Objects.requireNonNull(validator, "Fluid validity check cannot be null");
         return new BasicFluidTank(capacity, canExtract, canInsert, validator, listener);
      }
   }

   protected BasicFluidTank(
      int capacity, Predicate<FluidStack> canExtract, Predicate<FluidStack> canInsert, Predicate<FluidStack> validator, @Nullable IContentsListener listener
   ) {
      this(
         capacity,
         (stack, automationType) -> automationType == AutomationType.MANUAL || canExtract.test(stack),
         (stack, automationType) -> canInsert.test(stack),
         validator,
         listener
      );
   }

   protected BasicFluidTank(
      int capacity,
      BiPredicate<FluidStack, AutomationType> canExtract,
      BiPredicate<FluidStack, AutomationType> canInsert,
      Predicate<FluidStack> validator,
      @Nullable IContentsListener listener
   ) {
      this.capacity = capacity;
      this.canExtract = canExtract;
      this.canInsert = canInsert;
      this.validator = validator;
      this.listener = listener;
   }

   @Override
   public void onContentsChanged() {
      if (this.listener != null) {
         this.listener.onContentsChanged();
      }
   }

   @NotNull
   public FluidStack getFluid() {
      return this.stored;
   }

   @Override
   public void setStack(FluidStack stack) {
      this.setStack(stack, true);
   }

   protected int getRate(@Nullable AutomationType automationType) {
      return Integer.MAX_VALUE;
   }

   @Override
   public void setStackUnchecked(FluidStack stack) {
      this.setStack(stack, false);
   }

   private void setStack(FluidStack stack, boolean validateStack) {
      if (stack.isEmpty()) {
         if (this.stored.isEmpty()) {
            return;
         }

         this.stored = FluidStack.EMPTY;
      } else {
         if (validateStack && !this.isFluidValid(stack)) {
            throw new RuntimeException("Invalid fluid for tank: " + RegistryUtils.getName(stack.getFluid()) + " " + stack.getAmount());
         }

         this.stored = new FluidStack(stack, stack.getAmount());
      }

      this.onContentsChanged();
   }

   @Override
   public FluidStack insert(@NotNull FluidStack stack, Action action, AutomationType automationType) {
      if (!stack.isEmpty() && this.isFluidValid(stack) && this.canInsert.test(stack, automationType)) {
         int needed = Math.min(this.getRate(automationType), this.getNeeded());
         if (needed <= 0) {
            return stack;
         } else {
            boolean sameType = false;
            if (!this.isEmpty() && !(sameType = this.stored.isFluidEqual(stack))) {
               return stack;
            } else {
               int toAdd = Math.min(stack.getAmount(), needed);
               if (action.execute()) {
                  if (sameType) {
                     this.stored.grow(toAdd);
                     this.onContentsChanged();
                  } else {
                     this.setStackUnchecked(new FluidStack(stack, toAdd));
                  }
               }

               return new FluidStack(stack, stack.getAmount() - toAdd);
            }
         }
      } else {
         return stack;
      }
   }

   @Override
   public FluidStack extract(int amount, Action action, AutomationType automationType) {
      if (!this.isEmpty() && amount >= 1 && this.canExtract.test(this.stored, automationType)) {
         int size = Math.min(Math.min(this.getRate(automationType), this.getFluidAmount()), amount);
         if (size == 0) {
            return FluidStack.EMPTY;
         } else {
            FluidStack ret = new FluidStack(this.stored, size);
            if (!ret.isEmpty() && action.execute()) {
               this.stored.shrink(ret.getAmount());
               this.onContentsChanged();
            }

            return ret;
         }
      } else {
         return FluidStack.EMPTY;
      }
   }

   public boolean isFluidValid(FluidStack stack) {
      return this.validator.test(stack);
   }

   @Override
   public int setStackSize(int amount, Action action) {
      if (this.isEmpty()) {
         return 0;
      } else if (amount <= 0) {
         if (action.execute()) {
            this.setEmpty();
         }

         return 0;
      } else {
         int maxStackSize = this.getCapacity();
         if (amount > maxStackSize) {
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

   @Override
   public int growStack(int amount, Action action) {
      int current = this.getFluidAmount();
      if (amount > 0) {
         amount = Math.min(Math.min(amount, this.getNeeded()), this.getRate(null));
      } else if (amount < 0) {
         amount = Math.max(amount, -this.getRate(null));
      }

      int newSize = this.setStackSize(current + amount, action);
      return newSize - current;
   }

   @Override
   public boolean isEmpty() {
      return this.stored.isEmpty();
   }

   @Override
   public boolean isFluidEqual(FluidStack other) {
      return this.stored.isFluidEqual(other);
   }

   public int getFluidAmount() {
      return this.stored.getAmount();
   }

   public int getCapacity() {
      return this.capacity;
   }

   @Override
   public CompoundTag serializeNBT() {
      CompoundTag nbt = new CompoundTag();
      if (!this.isEmpty()) {
         nbt.m_128365_("stored", this.stored.writeToNBT(new CompoundTag()));
      }

      return nbt;
   }

   public void deserializeNBT(CompoundTag nbt) {
      NBTUtils.setFluidStackIfPresent(nbt, "stored", this::setStackUnchecked);
   }
}
