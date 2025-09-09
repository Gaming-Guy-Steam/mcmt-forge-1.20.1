package mekanism.api.fluid;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

@NothingNullByDefault
public interface IExtendedFluidTank extends IFluidTank, INBTSerializable<CompoundTag>, IContentsListener {
   void setStack(FluidStack var1);

   void setStackUnchecked(FluidStack var1);

   default FluidStack insert(FluidStack stack, Action action, AutomationType automationType) {
      if (!stack.isEmpty() && this.isFluidValid(stack)) {
         int needed = this.getNeeded();
         if (needed <= 0) {
            return stack;
         } else {
            boolean sameType = false;
            if (!this.isEmpty() && !(sameType = stack.isFluidEqual(this.getFluid()))) {
               return stack;
            } else {
               int toAdd = Math.min(stack.getAmount(), needed);
               if (action.execute()) {
                  if (sameType) {
                     this.growStack(toAdd, action);
                  } else {
                     this.setStack(new FluidStack(stack, toAdd));
                  }
               }

               return new FluidStack(stack, stack.getAmount() - toAdd);
            }
         }
      } else {
         return stack;
      }
   }

   default FluidStack extract(int amount, Action action, AutomationType automationType) {
      if (!this.isEmpty() && amount >= 1) {
         FluidStack ret = new FluidStack(this.getFluid(), Math.min(this.getFluidAmount(), amount));
         if (!ret.isEmpty() && action.execute()) {
            this.shrinkStack(ret.getAmount(), action);
         }

         return ret;
      } else {
         return FluidStack.EMPTY;
      }
   }

   default int setStackSize(int amount, Action action) {
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
            this.setStack(new FluidStack(this.getFluid(), amount));
            return amount;
         } else {
            return amount;
         }
      }
   }

   default int growStack(int amount, Action action) {
      int current = this.getFluidAmount();
      if (amount > 0) {
         amount = Math.min(amount, this.getNeeded());
      }

      int newSize = this.setStackSize(current + amount, action);
      return newSize - current;
   }

   default int shrinkStack(int amount, Action action) {
      return -this.growStack(-amount, action);
   }

   default boolean isEmpty() {
      return this.getFluid().isEmpty();
   }

   default void setEmpty() {
      this.setStack(FluidStack.EMPTY);
   }

   default boolean isFluidEqual(FluidStack other) {
      return this.getFluid().isFluidEqual(other);
   }

   default int getNeeded() {
      return Math.max(0, this.getCapacity() - this.getFluidAmount());
   }

   default CompoundTag serializeNBT() {
      CompoundTag nbt = new CompoundTag();
      if (!this.isEmpty()) {
         nbt.m_128365_("stored", this.getFluid().writeToNBT(new CompoundTag()));
      }

      return nbt;
   }

   @Deprecated
   default int fill(FluidStack stack, FluidAction action) {
      return stack.getAmount() - this.insert(stack, Action.fromFluidAction(action), AutomationType.EXTERNAL).getAmount();
   }

   @Deprecated
   default FluidStack drain(FluidStack stack, FluidAction action) {
      return !this.isEmpty() && this.getFluid().isFluidEqual(stack)
         ? this.extract(stack.getAmount(), Action.fromFluidAction(action), AutomationType.EXTERNAL)
         : FluidStack.EMPTY;
   }

   @Deprecated
   default FluidStack drain(int amount, FluidAction action) {
      return this.extract(amount, Action.fromFluidAction(action), AutomationType.EXTERNAL);
   }
}
