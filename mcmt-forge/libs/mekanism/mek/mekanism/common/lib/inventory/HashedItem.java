package mekanism.common.lib.inventory;

import java.util.Objects;
import java.util.UUID;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.inventory.IHashedItem;
import mekanism.common.util.StackUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class HashedItem implements IHashedItem {
   private final ItemStack itemStack;
   private final int hashCode;

   public static HashedItem create(ItemStack stack) {
      return new HashedItem(stack.m_255036_(1));
   }

   public static HashedItem raw(ItemStack stack) {
      return new HashedItem(stack);
   }

   protected HashedItem(ItemStack stack) {
      this.itemStack = stack;
      this.hashCode = this.initHashCode();
   }

   protected HashedItem(HashedItem other) {
      this(other.itemStack, other.hashCode);
   }

   protected HashedItem(ItemStack stack, int hashCode) {
      this.itemStack = stack;
      this.hashCode = hashCode;
   }

   @Deprecated(
      forRemoval = true,
      since = "10.3.6"
   )
   public ItemStack getStack() {
      return this.itemStack;
   }

   @Override
   public ItemStack getInternalStack() {
      return this.itemStack;
   }

   @Override
   public ItemStack createStack(int size) {
      return StackUtils.size(this.itemStack, size);
   }

   public HashedItem recreate() {
      return new HashedItem(this.createStack(1), this.hashCode);
   }

   @NotNull
   public CompoundTag internalToNBT() {
      return this.itemStack.serializeNBT();
   }

   @Override
   public boolean equals(Object obj) {
      return obj == this ? true : obj instanceof IHashedItem other && ItemHandlerHelper.canItemStacksStack(this.itemStack, other.getInternalStack());
   }

   @Override
   public int hashCode() {
      return this.hashCode;
   }

   private int initHashCode() {
      int code = this.itemStack.m_41720_().hashCode();
      if (this.itemStack.m_41782_()) {
         code = 31 * code + this.itemStack.m_41783_().hashCode();
      }

      return code;
   }

   public static class UUIDAwareHashedItem extends HashedItem {
      @Nullable
      private final UUID uuid;
      private final int uuidBasedHash;
      private final boolean overrideHash;

      public UUIDAwareHashedItem(ItemStack stack, @Nullable UUID uuid) {
         super(stack.m_255036_(1));
         this.uuid = uuid;
         if (this.uuid == null) {
            this.overrideHash = false;
            this.uuidBasedHash = super.hashCode();
         } else {
            this.overrideHash = true;
            this.uuidBasedHash = Objects.hash(super.hashCode(), this.uuid);
         }
      }

      public UUIDAwareHashedItem(HashedItem other, @NotNull UUID uuid) {
         super(other);
         this.uuid = uuid;
         this.uuidBasedHash = super.hashCode();
         this.overrideHash = false;
      }

      @Nullable
      public UUID getUUID() {
         return this.uuid;
      }

      @Override
      public boolean equals(Object obj) {
         if (obj == this) {
            return true;
         } else {
            return !this.overrideHash
               ? super.equals(obj)
               : obj instanceof HashedItem.UUIDAwareHashedItem uuidAware && this.uuid.equals(uuidAware.uuid) && super.equals(obj);
         }
      }

      @Override
      public int hashCode() {
         return this.uuidBasedHash;
      }

      public HashedItem asRawHashedItem() {
         return new HashedItem(this);
      }
   }
}
