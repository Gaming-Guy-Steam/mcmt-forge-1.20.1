package mekanism.common.content.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.list.SyncableFilterList;
import mekanism.common.lib.collection.HashList;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

public class FilterManager<FILTER extends IFilter<?>> {
   private final Class<? extends FILTER> filterClass;
   protected final Runnable markForSave;
   protected HashList<FILTER> filters = new HashList<>();
   @Nullable
   protected List<FILTER> enabledFilters = null;

   public FilterManager(Class<? extends FILTER> filterClass, Runnable markForSave) {
      this.filterClass = filterClass;
      this.markForSave = markForSave;
   }

   public final List<FILTER> getFilters() {
      return this.filters;
   }

   public final List<FILTER> getEnabledFilters() {
      if (this.enabledFilters == null) {
         this.enabledFilters = this.filters.stream().filter(IFilter::isEnabled).collect(Collectors.toList());
      }

      return this.enabledFilters;
   }

   public final int count() {
      return this.filters.size();
   }

   public boolean anyEnabledMatch(Predicate<FILTER> validator) {
      return this.getEnabledFilters().stream().anyMatch(validator);
   }

   public boolean hasEnabledFilters() {
      return !this.getEnabledFilters().isEmpty();
   }

   public void toggleState(int index) {
      FILTER filter = this.filters.getOrNull(index);
      if (filter != null) {
         filter.setEnabled(!filter.isEnabled());
         this.markForSave.run();
         this.enabledFilters = null;
      }
   }

   public void tryAddFilter(IFilter<?> toAdd, boolean save) {
      if (this.filterClass.isInstance(toAdd)) {
         this.addFilter((FILTER)this.filterClass.cast(toAdd), save);
      }
   }

   public boolean addFilter(FILTER filter) {
      return this.addFilter(filter, true);
   }

   private boolean addFilter(FILTER filter, boolean save) {
      boolean result = this.filters.add(filter);
      if (save) {
         this.markForSave.run();
      }

      if (this.enabledFilters != null && filter.isEnabled()) {
         this.enabledFilters.add(filter);
      }

      return result;
   }

   public boolean removeFilter(FILTER filter) {
      boolean result = this.filters.remove(filter);
      this.markForSave.run();
      if (filter.isEnabled()) {
         this.enabledFilters = null;
      }

      return result;
   }

   public <F extends IFilter<F>> void tryEditFilter(F currentFilter, @Nullable F newFilter) {
      if (this.filterClass.isInstance(currentFilter)) {
         if (newFilter == null) {
            this.removeFilter((FILTER)this.filterClass.cast(currentFilter));
         } else {
            this.editFilter((FILTER)this.filterClass.cast(currentFilter), (FILTER)this.filterClass.cast(newFilter));
         }
      }
   }

   private void editFilter(FILTER currentFilter, FILTER newFilter) {
      if (this.filters.replace(currentFilter, newFilter)) {
         this.markForSave.run();
         if (currentFilter.isEnabled() || newFilter.isEnabled()) {
            this.enabledFilters = null;
         }
      }
   }

   public void addContainerTrackers(MekanismContainer container) {
      container.track(SyncableFilterList.create(this::getFilters, value -> {
         if (value instanceof HashList<FILTER> filterList) {
            this.filters = filterList;
         } else {
            this.filters = new HashList<>(value);
         }

         this.enabledFilters = null;
      }));
   }

   public void writeToNBT(CompoundTag nbt) {
      if (!this.filters.isEmpty()) {
         ListTag filterTags = new ListTag();

         for (FILTER filter : this.filters) {
            filterTags.add(filter.write(new CompoundTag()));
         }

         nbt.m_128365_("filters", filterTags);
      }
   }

   public void readFromNBT(CompoundTag nbt) {
      this.filters.clear();
      this.enabledFilters = new ArrayList<>();
      NBTUtils.setListIfPresent(nbt, "filters", 10, tagList -> {
         int i = 0;

         for (int size = tagList.size(); i < size; i++) {
            IFilter<?> filter = BaseFilter.readFromNBT(tagList.m_128728_(i));
            this.tryAddFilter(filter, false);
         }
      });
   }
}
