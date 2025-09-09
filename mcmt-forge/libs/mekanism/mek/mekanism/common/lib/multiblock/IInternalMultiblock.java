package mekanism.common.lib.multiblock;

import java.util.UUID;
import org.jetbrains.annotations.Nullable;

public interface IInternalMultiblock {
   @Nullable
   UUID getMultiblockUUID();

   @Nullable
   MultiblockData getMultiblock();

   void setMultiblock(@Nullable MultiblockData multiblock);

   default boolean hasFormedMultiblock() {
      return this.getMultiblockUUID() != null;
   }
}
