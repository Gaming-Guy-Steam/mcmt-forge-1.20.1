package mekanism.api;

import java.util.function.Predicate;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;

@NothingNullByDefault
public interface IIncrementalEnum<TYPE extends Enum<TYPE> & IIncrementalEnum<TYPE>> {
   default TYPE getNext(Predicate<TYPE> isValid) {
      TYPE next;
      for (next = this.byIndex(this.ordinal() + 1); !isValid.test(next); next = this.byIndex(next.ordinal() + 1)) {
         if (next == this) {
            return next;
         }
      }

      return next;
   }

   default TYPE getPrevious(Predicate<TYPE> isValid) {
      TYPE previous;
      for (previous = this.byIndex(this.ordinal() - 1); !isValid.test(previous); previous = this.byIndex(previous.ordinal() - 1)) {
         if (previous == this) {
            return previous;
         }
      }

      return previous;
   }

   TYPE byIndex(int var1);

   int ordinal();

   default TYPE getNext() {
      return this.getNext(ConstantPredicates.alwaysTrue());
   }

   default TYPE getPrevious() {
      return this.getPrevious(ConstantPredicates.alwaysTrue());
   }

   default TYPE adjust(int shift) {
      return (TYPE)(shift == 0 ? this : this.byIndex(this.ordinal() + shift));
   }

   default TYPE adjust(int shift, Predicate<TYPE> isValid) {
      var result = (TYPE)this;

      while (shift < 0) {
         shift++;
         result = result.getPrevious(isValid);
      }

      while (shift > 0) {
         shift--;
         result = result.getNext(isValid);
      }

      return result;
   }
}
