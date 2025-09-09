package mekanism.common.inventory.container.sync;

import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.common.network.to_client.container.property.BlockPosPropertyData;
import mekanism.common.network.to_client.container.property.PropertyData;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

public class SyncableBlockPos implements ISyncableData {
   private final Supplier<BlockPos> getter;
   private final Consumer<BlockPos> setter;
   private int lastKnownHashCode;

   public static SyncableBlockPos create(Supplier<BlockPos> getter, Consumer<BlockPos> setter) {
      return new SyncableBlockPos(getter, setter);
   }

   private SyncableBlockPos(Supplier<BlockPos> getter, Consumer<BlockPos> setter) {
      this.getter = getter;
      this.setter = setter;
   }

   @Nullable
   public BlockPos get() {
      return this.getter.get();
   }

   public void set(@Nullable BlockPos value) {
      this.setter.accept(value);
   }

   @Override
   public ISyncableData.DirtyType isDirty() {
      BlockPos value = this.get();
      int valueHashCode = value == null ? 0 : value.hashCode();
      if (this.lastKnownHashCode == valueHashCode) {
         return ISyncableData.DirtyType.CLEAN;
      } else {
         this.lastKnownHashCode = valueHashCode;
         return ISyncableData.DirtyType.DIRTY;
      }
   }

   @Override
   public PropertyData getPropertyData(short property, ISyncableData.DirtyType dirtyType) {
      return new BlockPosPropertyData(property, this.get());
   }
}
