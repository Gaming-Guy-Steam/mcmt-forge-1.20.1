package mekanism.common.inventory.container.sync;

import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.common.network.to_client.container.property.IntPropertyData;
import mekanism.common.network.to_client.container.property.ItemStackPropertyData;
import mekanism.common.network.to_client.container.property.PropertyData;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SyncableItemStack implements ISyncableData {
   private final Supplier<ItemStack> getter;
   private final Consumer<ItemStack> setter;
   @NotNull
   private ItemStack lastKnownValue = ItemStack.f_41583_;

   public static SyncableItemStack create(Supplier<ItemStack> getter, Consumer<ItemStack> setter) {
      return new SyncableItemStack(getter, setter);
   }

   private SyncableItemStack(Supplier<ItemStack> getter, Consumer<ItemStack> setter) {
      this.getter = getter;
      this.setter = setter;
   }

   @NotNull
   public ItemStack get() {
      return this.getter.get();
   }

   public void set(@NotNull ItemStack value) {
      this.setter.accept(value);
   }

   public void set(int amount) {
      ItemStack stack = this.get();
      if (!stack.m_41619_()) {
         stack.m_41764_(amount);
      }
   }

   @Override
   public ISyncableData.DirtyType isDirty() {
      ItemStack value = this.get();
      if (value.m_41619_() && this.lastKnownValue.m_41619_()) {
         return ISyncableData.DirtyType.CLEAN;
      } else {
         boolean sameItem = ItemStack.m_150942_(value, this.lastKnownValue);
         if (sameItem && value.m_41613_() == this.lastKnownValue.m_41613_()) {
            return ISyncableData.DirtyType.CLEAN;
         } else {
            this.lastKnownValue = value.m_41777_();
            return sameItem ? ISyncableData.DirtyType.SIZE : ISyncableData.DirtyType.DIRTY;
         }
      }
   }

   @Override
   public PropertyData getPropertyData(short property, ISyncableData.DirtyType dirtyType) {
      return (PropertyData)(dirtyType == ISyncableData.DirtyType.SIZE
         ? new IntPropertyData(property, this.get().m_41613_())
         : new ItemStackPropertyData(property, this.get()));
   }
}
