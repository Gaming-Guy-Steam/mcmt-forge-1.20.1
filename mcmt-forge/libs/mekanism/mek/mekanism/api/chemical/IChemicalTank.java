package mekanism.api.chemical;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

@NothingNullByDefault
public interface IChemicalTank<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>>
   extends IEmptyStackProvider<CHEMICAL, STACK>,
   INBTSerializable<CompoundTag>,
   IContentsListener {
   STACK createStack(STACK var1, long var2);

   STACK getStack();

   void setStack(STACK var1);

   void setStackUnchecked(STACK var1);

   default STACK insert(STACK stack, Action action, AutomationType automationType) {
      if (!stack.isEmpty() && this.isValid(stack)) {
         long needed = this.getNeeded();
         if (needed <= 0L) {
            return stack;
         } else {
            boolean sameType = false;
            if (!this.isEmpty() && !(sameType = this.isTypeEqual(stack))) {
               return stack;
            } else {
               long toAdd = Math.min(stack.getAmount(), needed);
               if (action.execute()) {
                  if (sameType) {
                     this.growStack(toAdd, action);
                  } else {
                     this.setStack(this.createStack(stack, toAdd));
                  }
               }

               return this.createStack(stack, stack.getAmount() - toAdd);
            }
         }
      } else {
         return stack;
      }
   }

   default STACK extract(long amount, Action action, AutomationType automationType) {
      if (!this.isEmpty() && amount >= 1L) {
         STACK ret = this.createStack(this.getStack(), Math.min(this.getStored(), amount));
         if (!ret.isEmpty() && action.execute()) {
            this.shrinkStack(ret.getAmount(), action);
         }

         return ret;
      } else {
         return this.getEmptyStack();
      }
   }

   long getCapacity();

   boolean isValid(STACK var1);

   default long setStackSize(long amount, Action action) {
      if (this.isEmpty()) {
         return 0L;
      } else if (amount <= 0L) {
         if (action.execute()) {
            this.setEmpty();
         }

         return 0L;
      } else {
         long maxStackSize = this.getCapacity();
         if (amount > maxStackSize) {
            amount = maxStackSize;
         }

         if (this.getStored() != amount && !action.simulate()) {
            this.setStack(this.createStack(this.getStack(), amount));
            return amount;
         } else {
            return amount;
         }
      }
   }

   default long growStack(long amount, Action action) {
      long current = this.getStored();
      if (amount > 0L) {
         amount = Math.min(amount, this.getNeeded());
      }

      long newSize = this.setStackSize(current + amount, action);
      return newSize - current;
   }

   default long shrinkStack(long amount, Action action) {
      return -this.growStack(-amount, action);
   }

   default boolean isEmpty() {
      return this.getStack().isEmpty();
   }

   default void setEmpty() {
      this.setStack(this.getEmptyStack());
   }

   default long getStored() {
      return this.getStack().getAmount();
   }

   default long getNeeded() {
      return Math.max(0L, this.getCapacity() - this.getStored());
   }

   default CHEMICAL getType() {
      return this.getStack().getType();
   }

   default boolean isTypeEqual(STACK other) {
      return this.getStack().isTypeEqual(other);
   }

   default boolean isTypeEqual(CHEMICAL other) {
      return this.getStack().isTypeEqual(other);
   }

   default ChemicalAttributeValidator getAttributeValidator() {
      return ChemicalAttributeValidator.DEFAULT;
   }

   default CompoundTag serializeNBT() {
      CompoundTag nbt = new CompoundTag();
      if (!this.isEmpty()) {
         nbt.m_128365_("stored", this.getStack().write(new CompoundTag()));
      }

      return nbt;
   }
}
