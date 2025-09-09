package mekanism.api.chemical;

import java.util.function.BiPredicate;
import java.util.function.Predicate;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public abstract class BasicChemicalTank<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>>
   implements IChemicalTank<CHEMICAL, STACK>,
   IChemicalHandler<CHEMICAL, STACK> {
   private final Predicate<CHEMICAL> validator;
   protected final BiPredicate<CHEMICAL, AutomationType> canExtract;
   protected final BiPredicate<CHEMICAL, AutomationType> canInsert;
   @Nullable
   private final ChemicalAttributeValidator attributeValidator;
   private final long capacity;
   protected STACK stored;
   @Nullable
   private final IContentsListener listener;

   protected BasicChemicalTank(
      long capacity,
      BiPredicate<CHEMICAL, AutomationType> canExtract,
      BiPredicate<CHEMICAL, AutomationType> canInsert,
      Predicate<CHEMICAL> validator,
      @Nullable ChemicalAttributeValidator attributeValidator,
      @Nullable IContentsListener listener
   ) {
      this.capacity = capacity;
      this.canExtract = canExtract;
      this.canInsert = canInsert;
      this.validator = validator;
      this.attributeValidator = attributeValidator;
      this.listener = listener;
      this.stored = this.getEmptyStack();
   }

   @Override
   public STACK getStack() {
      return this.stored;
   }

   @Override
   public void setStack(STACK stack) {
      this.setStack(stack, true);
   }

   protected long getRate(@Nullable AutomationType automationType) {
      return Long.MAX_VALUE;
   }

   @Override
   public void setStackUnchecked(STACK stack) {
      this.setStack(stack, false);
   }

   private void setStack(STACK stack, boolean validateStack) {
      if (stack.isEmpty()) {
         if (this.stored.isEmpty()) {
            return;
         }

         this.stored = this.getEmptyStack();
      } else {
         if (validateStack && !this.isValid(stack)) {
            throw new RuntimeException("Invalid chemical for tank: " + stack.getTypeRegistryName() + " " + stack.getAmount());
         }

         this.stored = this.createStack(stack, stack.getAmount());
      }

      this.onContentsChanged();
   }

   @Override
   public STACK insert(@NotNull STACK stack, Action action, AutomationType automationType) {
      if (!stack.isEmpty() && this.isValid(stack) && this.canInsert.test(stack.getType(), automationType)) {
         long needed = Math.min(this.getRate(automationType), this.getNeeded());
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
                     this.stored.grow(toAdd);
                     this.onContentsChanged();
                  } else {
                     this.setStackUnchecked(this.createStack(stack, toAdd));
                  }
               }

               return this.createStack(stack, stack.getAmount() - toAdd);
            }
         }
      } else {
         return stack;
      }
   }

   @Override
   public STACK extract(long amount, Action action, AutomationType automationType) {
      if (!this.isEmpty() && amount >= 1L && this.canExtract.test(this.stored.getType(), automationType)) {
         long size = Math.min(Math.min(this.getRate(automationType), this.getStored()), amount);
         if (size == 0L) {
            return this.getEmptyStack();
         } else {
            STACK ret = this.createStack(this.stored, size);
            if (!ret.isEmpty() && action.execute()) {
               this.stored.shrink(ret.getAmount());
               this.onContentsChanged();
            }

            return ret;
         }
      } else {
         return this.getEmptyStack();
      }
   }

   @Override
   public boolean isValid(STACK stack) {
      return this.getAttributeValidator().process(stack) && this.validator.test(stack.getType());
   }

   @Override
   public long setStackSize(long amount, Action action) {
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
            this.stored.setAmount(amount);
            this.onContentsChanged();
            return amount;
         } else {
            return amount;
         }
      }
   }

   @Override
   public long growStack(long amount, Action action) {
      long current = this.getStored();
      if (amount > 0L) {
         amount = Math.min(Math.min(amount, this.getNeeded()), this.getRate(null));
      } else if (amount < 0L) {
         amount = Math.max(amount, -this.getRate(null));
      }

      long newSize = this.setStackSize(current + amount, action);
      return newSize - current;
   }

   @Override
   public boolean isEmpty() {
      return this.stored.isEmpty();
   }

   @Override
   public long getStored() {
      return this.stored.getAmount();
   }

   @Override
   public CHEMICAL getType() {
      return this.stored.getType();
   }

   @Override
   public boolean isTypeEqual(STACK other) {
      return this.stored.isTypeEqual(other);
   }

   @Override
   public boolean isTypeEqual(CHEMICAL other) {
      return this.stored.isTypeEqual(other);
   }

   @Override
   public long getCapacity() {
      return this.capacity;
   }

   @Override
   public void onContentsChanged() {
      if (this.listener != null) {
         this.listener.onContentsChanged();
      }
   }

   @Override
   public ChemicalAttributeValidator getAttributeValidator() {
      return this.attributeValidator == null ? IChemicalTank.super.getAttributeValidator() : this.attributeValidator;
   }

   @Override
   public CompoundTag serializeNBT() {
      CompoundTag nbt = new CompoundTag();
      if (!this.isEmpty()) {
         nbt.m_128365_("stored", this.stored.write(new CompoundTag()));
      }

      return nbt;
   }

   @Override
   public int getTanks() {
      return 1;
   }

   @Override
   public STACK getChemicalInTank(int tank) {
      return tank == 0 ? this.getStack() : this.getEmptyStack();
   }

   @Override
   public void setChemicalInTank(int tank, STACK stack) {
      if (tank == 0) {
         this.setStack(stack);
      }
   }

   @Override
   public long getTankCapacity(int tank) {
      return tank == 0 ? this.getCapacity() : 0L;
   }

   @Override
   public boolean isValid(int tank, STACK stack) {
      return tank == 0 && this.isValid(stack);
   }

   @Override
   public STACK insertChemical(int tank, STACK stack, Action action) {
      return tank == 0 ? this.insert(stack, action, AutomationType.EXTERNAL) : stack;
   }

   @Override
   public STACK extractChemical(int tank, long amount, Action action) {
      return tank == 0 ? this.extract(amount, action, AutomationType.EXTERNAL) : this.getEmptyStack();
   }
}
