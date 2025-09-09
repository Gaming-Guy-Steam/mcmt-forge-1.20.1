package mekanism.api;

import java.util.function.Predicate;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;

@NothingNullByDefault
public interface IDisableableEnum<TYPE extends Enum<TYPE> & IDisableableEnum<TYPE>> extends IIncrementalEnum<TYPE> {
   boolean isEnabled();

   @Override
   default TYPE getNext(Predicate<TYPE> isValid) {
      return IIncrementalEnum.super.getNext(element -> element.isEnabled() && isValid.test(element));
   }

   @Override
   default TYPE getPrevious(Predicate<TYPE> isValid) {
      return IIncrementalEnum.super.getPrevious(element -> element.isEnabled() && isValid.test(element));
   }

   @Override
   default TYPE adjust(int shift) {
      return this.adjust(shift, ConstantPredicates.alwaysTrue());
   }
}
