package mekanism.common.content.filter;

import java.util.function.BiConsumer;

public class SortableFilterManager<FILTER extends IFilter<?>> extends FilterManager<FILTER> {
   private final BiConsumer<FILTER, FILTER> postSwap = (sourceFilter, targetFilter) -> {
      this.markForSave.run();
      if (sourceFilter.isEnabled() && targetFilter.isEnabled()) {
         this.enabledFilters = null;
      }
   };

   public SortableFilterManager(Class<? extends FILTER> filterClass, Runnable markForSave) {
      super(filterClass, markForSave);
   }

   public void moveUp(int filterIndex) {
      this.filters.swap(filterIndex, filterIndex - 1, this.postSwap);
   }

   public void moveDown(int filterIndex) {
      this.filters.swap(filterIndex, filterIndex + 1, this.postSwap);
   }

   public void moveToTop(int filterIndex) {
      this.moveTo(filterIndex, 0);
   }

   public void moveToBottom(int filterIndex) {
      this.moveTo(filterIndex, this.count() - 1);
   }

   private void moveTo(int source, int target) {
      if (source != target && source >= 0 && target >= 0) {
         int size = this.count();
         if (source < size && target < size) {
            FILTER sourceFilter = this.filters.remove(source);
            this.filters.add(target, sourceFilter);
            this.markForSave.run();
            if (sourceFilter.isEnabled()) {
               this.enabledFilters = null;
            }
         }
      }
   }
}
