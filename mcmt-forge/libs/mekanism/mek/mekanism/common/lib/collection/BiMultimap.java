package mekanism.common.lib.collection;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import java.util.Collection;
import java.util.Set;
import java.util.Map.Entry;

public class BiMultimap<K, V> {
   private final SetMultimap<K, V> map = HashMultimap.create();
   private final SetMultimap<V, K> reverseMap = HashMultimap.create();

   public boolean put(K key, V value) {
      return this.map.put(key, value) && this.reverseMap.put(value, key);
   }

   public boolean putAll(Collection<K> keys, V value) {
      boolean changed = false;

      for (K key : keys) {
         changed |= this.put(key, value);
      }

      return changed;
   }

   public boolean remove(K key, V value) {
      return this.map.remove(key, value) && this.reverseMap.remove(value, key);
   }

   public boolean removeKey(K key) {
      boolean changed = false;

      for (V value : this.getValues(key)) {
         changed |= this.reverseMap.remove(value, key);
      }

      this.map.removeAll(key);
      return changed;
   }

   public boolean removeValue(V value) {
      boolean changed = false;

      for (K key : this.getKeys(value)) {
         changed |= this.map.remove(key, value);
      }

      this.reverseMap.removeAll(value);
      return changed;
   }

   public Set<K> getAllKeys() {
      return this.map.keySet();
   }

   public Set<V> getValues(K key) {
      return this.map.get(key);
   }

   public Set<K> getKeys(V value) {
      return this.reverseMap.get(value);
   }

   public Set<Entry<K, V>> getEntries() {
      return this.map.entries();
   }

   public Set<Entry<V, K>> getReverseEntries() {
      return this.reverseMap.entries();
   }

   public boolean hasAllKeys(Collection<K> keys) {
      return this.getAllKeys().containsAll(keys);
   }

   public void clear() {
      this.map.clear();
      this.reverseMap.clear();
   }
}
