package mekanism.api.radial.mode;

import mekanism.api.radial.RadialData;
import org.jetbrains.annotations.Nullable;

public interface INestedRadialMode extends IRadialMode {
   @Nullable
   RadialData<?> nestedData();

   default boolean hasNestedData() {
      return this.nestedData() != null;
   }
}
