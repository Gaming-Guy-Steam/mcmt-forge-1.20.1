package mekanism.common.inventory.container.sync;

import mekanism.common.network.to_client.container.property.PropertyData;

public interface ISyncableData {
   ISyncableData.DirtyType isDirty();

   PropertyData getPropertyData(short property, ISyncableData.DirtyType dirtyType);

   public static enum DirtyType {
      CLEAN,
      SIZE,
      DIRTY;

      public static ISyncableData.DirtyType get(boolean dirty) {
         return dirty ? DIRTY : CLEAN;
      }
   }
}
