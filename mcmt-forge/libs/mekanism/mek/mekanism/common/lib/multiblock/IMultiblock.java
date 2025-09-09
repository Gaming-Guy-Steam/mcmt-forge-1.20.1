package mekanism.common.lib.multiblock;

import java.util.UUID;
import org.jetbrains.annotations.Nullable;

public interface IMultiblock<T extends MultiblockData> extends IMultiblockBase {
   T createMultiblock();

   default T getMultiblock() {
      MultiblockData data = this.getStructure().getMultiblockData();
      return (T)(data != null && data.isFormed() ? data : this.getDefaultData());
   }

   @Override
   T getDefaultData();

   MultiblockManager<T> getManager();

   @Nullable
   UUID getCacheID();

   void resetCache();

   boolean isMaster();

   boolean canBeMaster();

   Structure getStructure();

   void setStructure(Structure structure);

   @Override
   default void setStructure(MultiblockManager<?> manager, Structure structure) {
      if (manager == this.getManager()) {
         this.setStructure(structure);
      }
   }

   @Override
   default Structure getStructure(MultiblockManager<?> manager) {
      return manager == this.getManager() ? this.getStructure() : null;
   }

   @Override
   default boolean hasStructure(Structure structure) {
      return this.getStructure() == structure;
   }

   default FormationProtocol<T> createFormationProtocol() {
      return new FormationProtocol<>(this, this.getStructure());
   }
}
