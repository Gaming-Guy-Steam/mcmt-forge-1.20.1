package mekanism.common.lib.collection;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HashList<T> extends AbstractList<T> {
   private final List<T> list;

   public HashList(List<T> newList) {
      this.list = newList;
   }

   public HashList() {
      this(new ArrayList<>());
   }

   public HashList(int initialCapacity) {
      this(new ArrayList<>(initialCapacity));
   }

   @Override
   public boolean contains(Object obj) {
      return this.list.contains(obj);
   }

   @Override
   public void clear() {
      this.list.clear();
   }

   @Override
   public T get(int index) {
      return this.list.get(index);
   }

   @Nullable
   public T getOrNull(int index) {
      return index >= 0 && index < this.size() ? this.get(index) : null;
   }

   @Override
   public boolean add(T obj) {
      return !this.list.contains(obj) && this.list.add(obj);
   }

   @Override
   public void add(int index, T obj) {
      if (!this.list.contains(obj)) {
         if (index > this.size()) {
            for (int i = this.size(); i <= index - 1; i++) {
               this.list.add(i, null);
            }
         }

         this.list.add(index, obj);
      }
   }

   @Override
   public boolean isEmpty() {
      return this.list.isEmpty();
   }

   @Override
   public T remove(int index) {
      return this.list.remove(index);
   }

   public void replace(int index, T obj) {
      if (this.getOrNull(index) != null) {
         this.remove(index);
      }

      this.add(index, obj);
   }

   public boolean replace(T existing, @Nullable T replacement) {
      if (existing.equals(replacement)) {
         return false;
      } else {
         int index = this.indexOf(existing);
         if (index == -1) {
            return false;
         } else {
            if (replacement != null && !this.contains(replacement)) {
               this.list.set(index, replacement);
            } else {
               this.remove(index);
            }

            return true;
         }
      }
   }

   @Override
   public boolean remove(Object obj) {
      return this.list.remove(obj);
   }

   @Override
   public int indexOf(Object obj) {
      return this.list.indexOf(obj);
   }

   @Override
   public int size() {
      return this.list.size();
   }

   public HashList<T> clone() {
      return new HashList<>(new ArrayList<>(this.list));
   }

   public void swap(int source, int target, BiConsumer<T, T> postSwap) {
      if (source != target && source >= 0 && target >= 0) {
         int size = this.size();
         if (source < size && target < size) {
            T sourceT = this.list.get(source);
            T targetT = this.list.get(target);
            this.list.set(source, targetT);
            this.list.set(target, sourceT);
            postSwap.accept(sourceT, targetT);
         }
      }
   }

   @Override
   public int hashCode() {
      return this.list.hashCode();
   }

   @Override
   public boolean equals(Object obj) {
      return obj == this || obj instanceof List && this.list.equals(obj);
   }

   @NotNull
   @Override
   public Iterator<T> iterator() {
      return this.list.iterator();
   }

   @Override
   public String toString() {
      return this.list.toString();
   }
}
