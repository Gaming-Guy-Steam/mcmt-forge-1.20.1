package mekanism.api.energy;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.FloatingLong;
import mekanism.api.text.IHasTranslationKey;

@NothingNullByDefault
public interface IEnergyConversion extends IHasTranslationKey {
   boolean isEnabled();

   default FloatingLong convertFrom(long energy) {
      return energy <= 0L ? FloatingLong.ZERO : this.convertInPlaceFrom(FloatingLong.create(energy));
   }

   FloatingLong convertFrom(FloatingLong var1);

   FloatingLong convertInPlaceFrom(FloatingLong var1);

   default int convertToAsInt(FloatingLong joules) {
      return this.convertTo(joules).intValue();
   }

   default long convertToAsLong(FloatingLong joules) {
      return this.convertTo(joules).longValue();
   }

   FloatingLong convertTo(FloatingLong var1);

   FloatingLong convertInPlaceTo(FloatingLong var1);
}
