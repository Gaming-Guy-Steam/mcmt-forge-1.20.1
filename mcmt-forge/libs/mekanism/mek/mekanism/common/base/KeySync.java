package mekanism.common.base;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.UUID;

public class KeySync {
   public static final int ASCEND = 0;
   public static final int BOOST = 1;
   public final Map<UUID, KeySync.KeySet> keys = new Object2ObjectOpenHashMap();

   public KeySync.KeySet getPlayerKeys(UUID playerUUID) {
      return this.keys.get(playerUUID);
   }

   public void add(UUID playerUUID, int key) {
      if (this.keys.containsKey(playerUUID)) {
         this.keys.get(playerUUID).keysActive.add(key);
      } else {
         this.keys.put(playerUUID, new KeySync.KeySet(key));
      }
   }

   public void remove(UUID playerUUID, int key) {
      if (this.keys.containsKey(playerUUID)) {
         this.keys.get(playerUUID).keysActive.remove(key);
      }
   }

   public boolean has(UUID playerUUID, int key) {
      return this.keys.containsKey(playerUUID) && this.keys.get(playerUUID).keysActive.contains(key);
   }

   public void update(UUID playerUUID, int key, boolean add) {
      if (add) {
         this.add(playerUUID, key);
      } else {
         this.remove(playerUUID, key);
      }
   }

   public static class KeySet {
      public final IntSet keysActive = new IntOpenHashSet();

      public KeySet(int key) {
         this.keysActive.add(key);
      }
   }
}
