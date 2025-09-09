package mekanism.common.lib.frequency;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import mekanism.common.lib.MekanismSavedData;
import mekanism.common.lib.collection.HashList;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FrequencyManager<FREQ extends Frequency> {
   public static final int MAX_FREQ_LENGTH = 16;
   private static boolean loaded;
   private static final Set<FrequencyManager<?>> managers = new ObjectOpenHashSet();
   private final Map<Object, FREQ> frequencies = new LinkedHashMap<>();
   @Nullable
   private FrequencyManager<FREQ>.FrequencyDataHandler dataHandler;
   private UUID ownerUUID;
   private final FrequencyType<FREQ> frequencyType;

   public FrequencyManager(FrequencyType<FREQ> frequencyType) {
      this.frequencyType = frequencyType;
      managers.add(this);
   }

   public FrequencyManager(FrequencyType<FREQ> frequencyType, UUID uuid) {
      this(frequencyType);
      this.ownerUUID = uuid;
   }

   public static void load() {
      if (!loaded) {
         loaded = true;
         FrequencyType.init();
         managers.forEach(FrequencyManager::createOrLoad);
      }
   }

   public static void tick() {
      if (!loaded) {
         load();
      }

      managers.forEach(FrequencyManager::tickSelf);
   }

   public static void reset() {
      for (FrequencyManager<?> manager : managers) {
         manager.frequencies.clear();
         manager.dataHandler = null;
      }

      loaded = false;
   }

   public boolean remove(Object key, UUID ownerUUID) {
      FREQ freq = this.getFrequency(key);
      if (freq != null && freq.ownerMatches(ownerUUID)) {
         freq.onRemove();
         this.frequencies.remove(key);
         this.markDirty();
         return true;
      } else {
         return false;
      }
   }

   public void deactivate(@Nullable Frequency freq, BlockEntity tile) {
      if (freq != null && freq.onDeactivate(tile)) {
         this.markDirty();
      }
   }

   public FREQ validateAndUpdate(BlockEntity tile, FREQ freq) {
      FREQ storedFreq = this.frequencies.computeIfAbsent(freq.getKey(), key -> {
         freq.setValid(true);
         this.markDirty();
         return freq;
      });
      if (storedFreq.update(tile)) {
         this.markDirty();
      }

      return storedFreq;
   }

   public void createOrLoad() {
      if (this.dataHandler == null) {
         String name = this.getName();
         this.dataHandler = MekanismSavedData.createSavedData(() -> new FrequencyManager.FrequencyDataHandler(), name);
         this.dataHandler.syncManager();
      }
   }

   public Collection<FREQ> getFrequencies() {
      return this.frequencies.values();
   }

   public FREQ getFrequency(Object key) {
      return this.frequencies.get(key);
   }

   public FREQ getOrCreateFrequency(Frequency.FrequencyIdentity identity, @Nullable UUID ownerUUID) {
      return this.frequencies.computeIfAbsent(identity.key(), key -> {
         FREQ freq = this.frequencyType.create(key, ownerUUID);
         freq.setPublic(identity.isPublic());
         this.markDirty();
         return freq;
      });
   }

   public void addFrequency(FREQ freq) {
      this.frequencies.put(freq.getKey(), freq);
      this.markDirty();
   }

   protected void markDirty() {
      if (this.dataHandler != null) {
         this.dataHandler.m_77762_();
      }
   }

   public FrequencyType<FREQ> getType() {
      return this.frequencyType;
   }

   private void tickSelf() {
      boolean dirty = false;

      for (FREQ freq : this.getFrequencies()) {
         dirty |= freq.tick();
      }

      if (dirty) {
         this.markDirty();
      }
   }

   public String getName() {
      String owner = this.ownerUUID == null ? "" : this.ownerUUID + "_";
      return owner + this.frequencyType.getName() + "FrequencyHandler";
   }

   public class FrequencyDataHandler extends MekanismSavedData {
      public List<FREQ> loadedFrequencies;
      public UUID loadedOwner;

      public void syncManager() {
         if (this.loadedFrequencies != null) {
            this.loadedFrequencies.forEach(freq -> FrequencyManager.this.frequencies.put(freq.getKey(), (FREQ)freq));
            FrequencyManager.this.ownerUUID = this.loadedOwner;
         }
      }

      @Override
      public void load(@NotNull CompoundTag nbtTags) {
         NBTUtils.setUUIDIfPresent(nbtTags, "owner", uuid -> this.loadedOwner = uuid);
         ListTag list = nbtTags.m_128437_("freqList", 10);
         this.loadedFrequencies = new HashList<>();

         for (int i = 0; i < list.size(); i++) {
            this.loadedFrequencies.add(FrequencyManager.this.frequencyType.create(list.m_128728_(i)));
         }
      }

      @NotNull
      public CompoundTag m_7176_(@NotNull CompoundTag nbtTags) {
         if (FrequencyManager.this.ownerUUID != null) {
            nbtTags.m_128362_("owner", FrequencyManager.this.ownerUUID);
         }

         ListTag list = new ListTag();

         for (FREQ freq : FrequencyManager.this.getFrequencies()) {
            CompoundTag compound = new CompoundTag();
            freq.write(compound);
            list.add(compound);
         }

         nbtTags.m_128365_("freqList", list);
         return nbtTags;
      }
   }
}
