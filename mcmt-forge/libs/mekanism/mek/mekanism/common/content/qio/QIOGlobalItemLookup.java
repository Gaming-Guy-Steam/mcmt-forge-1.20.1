package mekanism.common.content.qio;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.UUID;
import java.util.Map.Entry;
import mekanism.common.Mekanism;
import mekanism.common.lib.MekanismSavedData;
import mekanism.common.lib.inventory.HashedItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QIOGlobalItemLookup {
   public static final QIOGlobalItemLookup INSTANCE = new QIOGlobalItemLookup();
   private static final String DATA_HANDLER_NAME = "qio_type_cache";
   @Nullable
   private QIOGlobalItemLookup.QIOGlobalItemLookupDataHandler dataHandler;
   private BiMap<UUID, HashedItem> itemCache = HashBiMap.create();

   private QIOGlobalItemLookup() {
   }

   @Nullable
   public UUID getUUIDForType(HashedItem item) {
      return (UUID)this.itemCache.inverse().get(item);
   }

   public UUID getOrTrackUUID(HashedItem item) {
      QIOGlobalItemLookup.SerializedHashedItem serializable = new QIOGlobalItemLookup.SerializedHashedItem(item);
      return (UUID)this.itemCache.inverse().computeIfAbsent(serializable, s -> {
         this.markDirty();
         return UUID.randomUUID();
      });
   }

   @Nullable
   public HashedItem getTypeByUUID(@Nullable UUID uuid) {
      return uuid == null ? null : (HashedItem)this.itemCache.get(uuid);
   }

   private void markDirty() {
      if (this.dataHandler != null) {
         this.dataHandler.m_77762_();
      }
   }

   public void createOrLoad() {
      if (this.dataHandler == null) {
         this.dataHandler = MekanismSavedData.createSavedData(QIOGlobalItemLookup.QIOGlobalItemLookupDataHandler::new, "qio_type_cache");
      }
   }

   public void reset() {
      this.itemCache = HashBiMap.create();
      this.dataHandler = null;
   }

   private static class QIOGlobalItemLookupDataHandler extends MekanismSavedData {
      @Override
      public void load(@NotNull CompoundTag nbt) {
         for (String key : nbt.m_128431_()) {
            UUID uuid;
            try {
               uuid = UUID.fromString(key);
            } catch (IllegalArgumentException var6) {
               Mekanism.logger.warn("Invalid UUID ({}) stored in {} saved data.", key, "qio_type_cache");
               continue;
            }

            ItemStack stack = ItemStack.m_41712_(nbt.m_128469_(key));
            if (stack.m_41619_()) {
               Mekanism.logger
                  .debug(
                     "Failed to read corresponding item for UUID ({}) stored in {} saved data. This most likely means the mod adding the item was removed.",
                     uuid,
                     "qio_type_cache"
                  );
            } else {
               QIOGlobalItemLookup.INSTANCE.itemCache.put(uuid, new QIOGlobalItemLookup.SerializedHashedItem(stack));
            }
         }
      }

      @NotNull
      public CompoundTag m_7176_(@NotNull CompoundTag nbt) {
         for (Entry<UUID, HashedItem> entry : QIOGlobalItemLookup.INSTANCE.itemCache.entrySet()) {
            nbt.m_128365_(entry.getKey().toString(), ((QIOGlobalItemLookup.SerializedHashedItem)entry.getValue()).getNbtRepresentation());
         }

         return nbt;
      }
   }

   private static class SerializedHashedItem extends HashedItem {
      private CompoundTag nbtRepresentation;

      @Nullable
      protected static QIOGlobalItemLookup.SerializedHashedItem read(CompoundTag nbtRepresentation) {
         ItemStack stack = ItemStack.m_41712_(nbtRepresentation);
         return stack.m_41619_() ? null : new QIOGlobalItemLookup.SerializedHashedItem(stack);
      }

      private SerializedHashedItem(ItemStack stack) {
         super(stack);
      }

      protected SerializedHashedItem(HashedItem other) {
         super(other);
      }

      public CompoundTag getNbtRepresentation() {
         if (this.nbtRepresentation == null) {
            this.nbtRepresentation = this.internalToNBT();
            this.nbtRepresentation.m_128344_("Count", (byte)1);
         }

         return this.nbtRepresentation;
      }
   }
}
