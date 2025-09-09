package mekanism.common.lib.frequency;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableFrequency;
import mekanism.common.inventory.container.sync.list.SyncableFrequencyList;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.ITileComponent;
import mekanism.common.util.WorldUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileComponentFrequency implements ITileComponent {
   private final TileEntityMekanism tile;
   private final Map<FrequencyType<?>, TileComponentFrequency.FrequencyData> supportedFrequencies = new LinkedHashMap<>();
   private final Map<FrequencyType<?>, List<? extends Frequency>> publicCache = new LinkedHashMap<>();
   private final Map<FrequencyType<?>, List<? extends Frequency>> privateCache = new LinkedHashMap<>();
   private boolean needsSave;
   private boolean needsNotify;

   public TileComponentFrequency(TileEntityMekanism tile) {
      this.tile = tile;
      tile.addComponent(this);
   }

   public boolean hasCustomFrequencies() {
      return this.supportedFrequencies.containsKey(FrequencyType.SECURITY) ? this.supportedFrequencies.size() > 1 : !this.supportedFrequencies.isEmpty();
   }

   public void tickServer() {
      for (Entry<FrequencyType<?>, TileComponentFrequency.FrequencyData> entry : this.supportedFrequencies.entrySet()) {
         this.updateFrequency(entry.getKey(), entry.getValue());
      }

      if (this.needsNotify) {
         this.tile.invalidateCachedCapabilities();
         WorldUtils.notifyLoadedNeighborsOfTileChange(this.tile.m_58904_(), this.tile.m_58899_());
         this.needsNotify = false;
      }

      if (this.needsSave) {
         this.tile.m_6596_();
         this.needsSave = false;
      }
   }

   public void track(FrequencyType<?> type, boolean needsSync, boolean needsListCache, boolean notifyNeighbors) {
      this.supportedFrequencies.put(type, new TileComponentFrequency.FrequencyData(needsSync, needsListCache, notifyNeighbors));
   }

   @Nullable
   public <FREQ extends Frequency> FREQ getFrequency(FrequencyType<FREQ> type) {
      TileComponentFrequency.FrequencyData frequencyData = this.supportedFrequencies.get(type);
      return (FREQ)(frequencyData == null ? null : frequencyData.selectedFrequency);
   }

   public <FREQ extends Frequency> void unsetFrequency(FrequencyType<FREQ> type) {
      this.unsetFrequency(type, this.supportedFrequencies.get(type));
   }

   private <FREQ extends Frequency> void unsetFrequency(FrequencyType<FREQ> type, TileComponentFrequency.FrequencyData frequencyData) {
      if (frequencyData != null && frequencyData.selectedFrequency != null) {
         this.deactivate(type, frequencyData);
         frequencyData.clearFrequency();
         this.setNeedsNotify(frequencyData);
      }
   }

   public <FREQ extends Frequency> List<FREQ> getPublicCache(FrequencyType<FREQ> type) {
      return this.getCache(this.publicCache, type);
   }

   public <FREQ extends Frequency> List<FREQ> getPrivateCache(FrequencyType<FREQ> type) {
      return this.getCache(this.privateCache, type);
   }

   private <FREQ extends Frequency> List<FREQ> getCache(Map<FrequencyType<?>, List<? extends Frequency>> cache, FrequencyType<FREQ> type) {
      return (List<FREQ>)cache.computeIfAbsent(type, t -> new ArrayList<>());
   }

   public <FREQ extends Frequency> void setFrequencyFromData(FrequencyType<FREQ> type, Frequency.FrequencyIdentity data, UUID player) {
      if (player != null) {
         TileComponentFrequency.FrequencyData frequencyData = this.supportedFrequencies.get(type);
         if (frequencyData != null) {
            this.setFrequencyFromData(type, data, player, frequencyData);
         }
      }
   }

   private <FREQ extends Frequency> void setFrequencyFromData(
      FrequencyType<FREQ> type, Frequency.FrequencyIdentity data, @NotNull UUID player, TileComponentFrequency.FrequencyData frequencyData
   ) {
      Frequency oldFrequency = frequencyData.selectedFrequency;
      FrequencyManager<FREQ> manager = type.getManager(data, player);
      FREQ freq = manager.getOrCreateFrequency(data, player);
      if (!freq.equals(oldFrequency)) {
         manager.deactivate(oldFrequency, this.tile);
         freq.update(this.tile);
         frequencyData.setFrequency(freq);
         this.setNeedsNotify(frequencyData);
      }
   }

   public void removeFrequencyFromData(FrequencyType<?> type, Frequency.FrequencyIdentity data, UUID player) {
      FrequencyManager<?> manager = type.getManager(data, player);
      if (manager != null && manager.remove(data.key(), player)) {
         this.setNeedsNotify(this.supportedFrequencies.get(type));
      }
   }

   private <FREQ extends Frequency> void updateFrequency(FrequencyType<FREQ> type, TileComponentFrequency.FrequencyData frequencyData) {
      if (frequencyData.selectedFrequency != null) {
         if (frequencyData.selectedFrequency.isValid()) {
            if (frequencyData.selectedFrequency.isRemoved()) {
               FrequencyManager<FREQ> manager = type.getFrequencyManager((FREQ)frequencyData.selectedFrequency);
               if (manager != null) {
                  manager.deactivate(frequencyData.selectedFrequency, this.tile);
               }

               frequencyData.clearFrequency();
               this.setNeedsNotify(frequencyData);
            }
         } else {
            FREQ frequency = (FREQ)frequencyData.selectedFrequency;
            FrequencyManager<FREQ> manager = type.getFrequencyManager(frequency);
            if (manager == null) {
               frequencyData.clearFrequency();
            } else {
               frequencyData.setFrequency(manager.validateAndUpdate(this.tile, frequency));
            }

            this.setNeedsNotify(frequencyData);
         }
      }
   }

   private void setNeedsNotify(TileComponentFrequency.FrequencyData data) {
      if (data.notifyNeighbors) {
         this.needsNotify = true;
      }

      this.needsSave = true;
   }

   private <FREQ extends Frequency> void deactivate(FrequencyType<FREQ> type, TileComponentFrequency.FrequencyData frequencyData) {
      if (frequencyData.selectedFrequency != null) {
         FrequencyManager<FREQ> manager = type.getFrequencyManager((FREQ)frequencyData.selectedFrequency);
         if (manager != null) {
            manager.deactivate(frequencyData.selectedFrequency, this.tile);
         }
      }
   }

   @Override
   public void read(CompoundTag nbtTags) {
      if (nbtTags.m_128425_("componentFrequency", 10)) {
         CompoundTag frequencyNBT = nbtTags.m_128469_("componentFrequency");

         for (Entry<FrequencyType<?>, TileComponentFrequency.FrequencyData> entry : this.supportedFrequencies.entrySet()) {
            FrequencyType<?> type = entry.getKey();
            if (frequencyNBT.m_128425_(type.getName(), 10)) {
               Frequency frequency = type.create(frequencyNBT.m_128469_(type.getName()));
               frequency.setValid(false);
               entry.getValue().setFrequency(frequency);
            }
         }
      }
   }

   @Override
   public void write(CompoundTag nbtTags) {
      CompoundTag frequencyNBT = new CompoundTag();

      for (TileComponentFrequency.FrequencyData frequencyData : this.supportedFrequencies.values()) {
         Frequency frequency = frequencyData.selectedFrequency;
         if (frequency != null) {
            CompoundTag frequencyTag = new CompoundTag();
            frequency.writeComponentData(frequencyTag);
            frequencyNBT.m_128365_(frequency.getType().getName(), frequencyTag);
         }
      }

      nbtTags.m_128365_("componentFrequency", frequencyNBT);
   }

   public void readConfiguredFrequencies(Player player, CompoundTag data) {
      if (this.hasCustomFrequencies() && data.m_128425_("componentFrequency", 10)) {
         CompoundTag frequencyNBT = data.m_128469_("componentFrequency");

         for (Entry<FrequencyType<?>, TileComponentFrequency.FrequencyData> entry : this.supportedFrequencies.entrySet()) {
            FrequencyType<?> type = entry.getKey();
            if (type != FrequencyType.SECURITY) {
               if (frequencyNBT.m_128425_(type.getName(), 10)) {
                  CompoundTag frequencyData = frequencyNBT.m_128469_(type.getName());
                  if (frequencyData.m_128403_("owner")) {
                     Frequency.FrequencyIdentity identity = Frequency.FrequencyIdentity.load(type, frequencyData);
                     if (identity != null) {
                        UUID owner = frequencyData.m_128342_("owner");
                        if (identity.isPublic() || owner.equals(player.m_20148_())) {
                           this.setFrequencyFromData(type, identity, owner, entry.getValue());
                        }
                        continue;
                     }
                  }
               }

               this.unsetFrequency(type, entry.getValue());
            }
         }
      }
   }

   public void writeConfiguredFrequencies(CompoundTag data) {
      CompoundTag frequencyNBT = new CompoundTag();

      for (Entry<FrequencyType<?>, TileComponentFrequency.FrequencyData> entry : this.supportedFrequencies.entrySet()) {
         Frequency frequency = entry.getValue().selectedFrequency;
         if (frequency != null && entry.getKey() != FrequencyType.SECURITY) {
            frequencyNBT.m_128365_(entry.getKey().getName(), frequency.serializeIdentityWithOwner());
         }
      }

      if (!frequencyNBT.m_128456_()) {
         data.m_128365_("componentFrequency", frequencyNBT);
      }
   }

   @Override
   public void invalidate() {
      if (!this.tile.isRemote()) {
         this.supportedFrequencies.forEach(this::deactivate);
      }
   }

   @Override
   public void trackForMainContainer(MekanismContainer container) {
      for (Entry<FrequencyType<?>, TileComponentFrequency.FrequencyData> entry : this.supportedFrequencies.entrySet()) {
         TileComponentFrequency.FrequencyData data = entry.getValue();
         if (data.needsContainerSync) {
            container.track(SyncableFrequency.create(() -> data.selectedFrequency, data::setFrequency));
         }

         if (data.needsListCache) {
            this.track(container, entry.getKey());
         }
      }
   }

   private <FREQ extends Frequency> void track(MekanismContainer container, FrequencyType<FREQ> type) {
      if (container.isRemote()) {
         container.track(SyncableFrequencyList.create(() -> this.getPublicCache(type), value -> this.publicCache.put(type, value)));
         container.track(SyncableFrequencyList.create(() -> this.getPrivateCache(type), value -> this.privateCache.put(type, value)));
      } else {
         container.track(
            SyncableFrequencyList.create(() -> type.getManagerWrapper().getPublicManager().getFrequencies(), value -> this.publicCache.put(type, value))
         );
         container.track(
            SyncableFrequencyList.create(
               () -> type.getManagerWrapper().getPrivateManager(container.getPlayerUUID()).getFrequencies(), value -> this.privateCache.put(type, value)
            )
         );
      }
   }

   private static final class FrequencyData {
      private final boolean needsContainerSync;
      private final boolean needsListCache;
      private final boolean notifyNeighbors;
      @Nullable
      private Frequency selectedFrequency;

      private FrequencyData(boolean needsContainerSync, boolean needsListCache, boolean notifyNeighbors) {
         this.needsContainerSync = needsContainerSync;
         this.needsListCache = needsListCache;
         this.notifyNeighbors = notifyNeighbors;
      }

      public void setFrequency(@Nullable Frequency frequency) {
         this.selectedFrequency = frequency;
      }

      public void clearFrequency() {
         this.setFrequency(null);
      }
   }
}
