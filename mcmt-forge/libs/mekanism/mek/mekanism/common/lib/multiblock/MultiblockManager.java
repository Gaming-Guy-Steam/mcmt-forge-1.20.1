package mekanism.common.lib.multiblock;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Supplier;
import mekanism.common.lib.MekanismSavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MultiblockManager<T extends MultiblockData> {
   private static final Set<MultiblockManager<?>> managers = new HashSet<>();
   private final String name;
   private final String nameLower;
   private final Supplier<MultiblockCache<T>> cacheSupplier;
   private final Supplier<IStructureValidator<T>> validatorSupplier;
   private final Map<UUID, MultiblockCache<T>> caches = new HashMap<>();
   @Nullable
   private MultiblockManager<T>.MultiblockCacheDataHandler dataHandler;

   public MultiblockManager(String name, Supplier<MultiblockCache<T>> cacheSupplier, Supplier<IStructureValidator<T>> validatorSupplier) {
      this.name = name;
      this.nameLower = name.toLowerCase(Locale.ROOT);
      this.cacheSupplier = cacheSupplier;
      this.validatorSupplier = validatorSupplier;
      managers.add(this);
   }

   public MultiblockCache<T> createCache() {
      return this.cacheSupplier.get();
   }

   public void trackCache(UUID id, MultiblockCache<T> cache) {
      this.caches.put(id, cache);
      this.markDirty();
   }

   @Nullable
   public MultiblockCache<T> getCache(UUID multiblockID) {
      return this.caches.get(multiblockID);
   }

   public IStructureValidator<T> createValidator() {
      return this.validatorSupplier.get();
   }

   public String getName() {
      return this.name;
   }

   public String getNameLower() {
      return this.nameLower;
   }

   public boolean isCompatible(BlockEntity tile) {
      return tile instanceof IMultiblock<?> multiblock ? multiblock.getManager() == this : false;
   }

   public static void reset() {
      for (MultiblockManager<?> manager : managers) {
         manager.caches.clear();
         manager.dataHandler = null;
      }
   }

   public void replaceCaches(Set<UUID> staleIds, UUID id, MultiblockCache<T> cache) {
      for (UUID staleId : staleIds) {
         this.caches.remove(staleId);
      }

      this.trackCache(id, cache);
   }

   public void handleDirtyMultiblock(T multiblock) {
      if (multiblock.isDirty()) {
         MultiblockCache<T> cache = this.getCache(multiblock.inventoryID);
         if (cache != null) {
            cache.sync(multiblock);
            this.markDirty();
            multiblock.resetDirty();
         }
      }
   }

   public UUID getUniqueInventoryID() {
      return UUID.randomUUID();
   }

   private void markDirty() {
      if (this.dataHandler != null) {
         this.dataHandler.m_77762_();
      }
   }

   public static void createOrLoadAll() {
      for (MultiblockManager<?> manager : managers) {
         manager.createOrLoad();
      }
   }

   private void createOrLoad() {
      if (this.dataHandler == null) {
         this.dataHandler = MekanismSavedData.createSavedData(() -> new MultiblockManager.MultiblockCacheDataHandler(), this.getNameLower());
      }
   }

   private class MultiblockCacheDataHandler extends MekanismSavedData {
      @Override
      public void load(@NotNull CompoundTag nbt) {
         if (nbt.m_128425_("cache", 9)) {
            ListTag cachesNbt = nbt.m_128437_("cache", 10);

            for (int i = 0; i < cachesNbt.size(); i++) {
               CompoundTag cacheTags = cachesNbt.m_128728_(i);
               if (cacheTags.m_128403_("inventoryID")) {
                  UUID id = cacheTags.m_128342_("inventoryID");
                  MultiblockCache<T> cachedData = MultiblockManager.this.cacheSupplier.get();
                  cachedData.load(cacheTags);
                  MultiblockManager.this.caches.put(id, cachedData);
               }
            }
         }
      }

      @NotNull
      public CompoundTag m_7176_(@NotNull CompoundTag nbt) {
         ListTag cachesNbt = new ListTag();

         for (Entry<UUID, MultiblockCache<T>> entry : MultiblockManager.this.caches.entrySet()) {
            CompoundTag cacheTags = new CompoundTag();
            cacheTags.m_128362_("inventoryID", entry.getKey());
            entry.getValue().save(cacheTags);
            cachesNbt.add(cacheTags);
         }

         nbt.m_128365_("cache", cachesNbt);
         return nbt;
      }
   }
}
