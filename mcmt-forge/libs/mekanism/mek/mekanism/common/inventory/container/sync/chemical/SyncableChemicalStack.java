package mekanism.common.inventory.container.sync.chemical;

import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IEmptyStackProvider;
import mekanism.common.inventory.container.sync.ISyncableData;
import org.jetbrains.annotations.NotNull;

public abstract class SyncableChemicalStack<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>>
   implements ISyncableData,
   IEmptyStackProvider<CHEMICAL, STACK> {
   @NotNull
   private ChemicalStack<CHEMICAL> lastKnownValue;
   private final Supplier<STACK> getter;
   private final Consumer<STACK> setter;

   protected SyncableChemicalStack(Supplier<STACK> getter, Consumer<STACK> setter) {
      this.getter = getter;
      this.setter = setter;
      this.lastKnownValue = this.getEmptyStack();
   }

   @NotNull
   protected abstract STACK createStack(STACK stored, long size);

   @NotNull
   public STACK get() {
      return this.getter.get();
   }

   public void set(@NotNull STACK value) {
      this.setter.accept(value);
   }

   public void set(long amount) {
      STACK stack = this.get();
      if (!stack.isEmpty()) {
         this.set(this.createStack(stack, amount));
      }
   }

   @Override
   public ISyncableData.DirtyType isDirty() {
      STACK value = this.get();
      boolean sameType = value.isTypeEqual(this.lastKnownValue);
      if (sameType && value.getAmount() == this.lastKnownValue.getAmount()) {
         return ISyncableData.DirtyType.CLEAN;
      } else {
         this.lastKnownValue = value.copy();
         return sameType ? ISyncableData.DirtyType.SIZE : ISyncableData.DirtyType.DIRTY;
      }
   }
}
