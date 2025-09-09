package mekanism.common.tile.interfaces;

import java.util.function.IntSupplier;

public interface ITileActive {
   IntSupplier NO_DELAY = () -> 0;

   default boolean isActivatable() {
      return true;
   }

   boolean getActive();

   void setActive(boolean active);
}
