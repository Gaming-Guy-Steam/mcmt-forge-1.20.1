package mekanism.common.capabilities.chemical.variable;

import java.util.function.BiPredicate;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.BasicChemicalTank;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public abstract class VariableCapacityChemicalTank<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>>
   extends BasicChemicalTank<CHEMICAL, STACK> {
   private final LongSupplier capacity;

   protected VariableCapacityChemicalTank(
      LongSupplier capacity,
      BiPredicate<CHEMICAL, AutomationType> canExtract,
      BiPredicate<CHEMICAL, AutomationType> canInsert,
      Predicate<CHEMICAL> validator,
      @Nullable ChemicalAttributeValidator attributeValidator,
      @Nullable IContentsListener listener
   ) {
      super(capacity.getAsLong(), canExtract, canInsert, validator, attributeValidator, listener);
      this.capacity = capacity;
   }

   @Override
   public long getCapacity() {
      return this.capacity.getAsLong();
   }

   @Override
   public long setStackSize(long amount, @NotNull Action action) {
      if (this.isEmpty()) {
         return 0L;
      } else if (amount <= 0L) {
         if (action.execute()) {
            this.setEmpty();
         }

         return 0L;
      } else {
         long maxStackSize = this.getCapacity();
         if (maxStackSize > 0L && amount > maxStackSize) {
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
}
