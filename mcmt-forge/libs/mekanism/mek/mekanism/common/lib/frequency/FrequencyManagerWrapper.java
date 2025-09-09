package mekanism.common.lib.frequency;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.UUID;
import mekanism.common.Mekanism;

public class FrequencyManagerWrapper<FREQ extends Frequency> {
   private final FrequencyManagerWrapper.Type type;
   private final FrequencyType<FREQ> frequencyType;
   private FrequencyManager<FREQ> publicManager;
   private Map<UUID, FrequencyManager<FREQ>> privateManagers;

   private FrequencyManagerWrapper(FrequencyManagerWrapper.Type type, FrequencyType<FREQ> frequencyType) {
      this.type = type;
      this.frequencyType = frequencyType;
      if (type.supportsPublic()) {
         this.publicManager = new FrequencyManager<>(frequencyType);
      }

      if (type.supportsPrivate()) {
         this.privateManagers = new Object2ObjectOpenHashMap();
      }
   }

   public static <FREQ extends Frequency> FrequencyManagerWrapper<FREQ> create(FrequencyType<FREQ> frequencyType, FrequencyManagerWrapper.Type type) {
      return new FrequencyManagerWrapper<>(type, frequencyType);
   }

   public FrequencyManager<FREQ> getPublicManager() {
      if (!this.type.supportsPublic()) {
         Mekanism.logger.error("Attempted to access public frequency manager of type {}. This shouldn't happen!", this.frequencyType.getName());
         return null;
      } else {
         return this.publicManager;
      }
   }

   public FrequencyManager<FREQ> getPrivateManager(UUID ownerUUID) {
      if (!this.type.supportsPrivate()) {
         Mekanism.logger.error("Attempted to access private frequency manager of type {}. This shouldn't happen!", this.frequencyType.getName());
         return null;
      } else if (ownerUUID == null) {
         Mekanism.logger.error("Attempted to access private frequency manager of type {} with no owner. This shouldn't happen!", this.frequencyType.getName());
         return null;
      } else {
         return this.privateManagers.computeIfAbsent(ownerUUID, owner -> {
            FrequencyManager<FREQ> manager = new FrequencyManager<>(this.frequencyType, owner);
            manager.createOrLoad();
            return manager;
         });
      }
   }

   public void clear() {
      if (this.privateManagers != null) {
         this.privateManagers.clear();
      }
   }

   public static enum Type {
      PUBLIC_ONLY,
      PRIVATE_ONLY,
      PUBLIC_PRIVATE;

      boolean supportsPublic() {
         return this == PUBLIC_ONLY || this == PUBLIC_PRIVATE;
      }

      boolean supportsPrivate() {
         return this == PRIVATE_ONLY || this == PUBLIC_PRIVATE;
      }
   }
}
