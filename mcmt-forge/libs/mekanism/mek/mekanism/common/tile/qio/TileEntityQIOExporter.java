package mekanism.common.tile.qio;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import mekanism.api.math.MathUtils;
import mekanism.common.Mekanism;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.content.qio.filter.QIOFilter;
import mekanism.common.content.qio.filter.QIOItemStackFilter;
import mekanism.common.content.qio.filter.QIOModIDFilter;
import mekanism.common.content.qio.filter.QIOTagFilter;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

public class TileEntityQIOExporter extends TileEntityQIOFilterHandler {
   private static final int MAX_DELAY = 10;
   private int delay = 0;
   private boolean exportWithoutFilter;
   private final TileEntityQIOExporter.EfficientEjector<it.unimi.dsi.fastutil.objects.Object2LongMap.Entry<HashedItem>> filterEjector = new TileEntityQIOExporter.EfficientEjector<>(
      java.util.Map.Entry::getKey, e -> MathUtils.clampToInt(e.getLongValue()), freq -> this.getFilterEjectMap(freq).object2LongEntrySet()
   );
   private final TileEntityQIOExporter.EfficientEjector<java.util.Map.Entry<HashedItem, QIOFrequency.QIOItemTypeData>> filterlessEjector = new TileEntityQIOExporter.EfficientEjector<>(
      java.util.Map.Entry::getKey, e -> MathUtils.clampToInt(e.getValue().getCount()), freq -> freq.getItemDataMap().entrySet()
   );

   public TileEntityQIOExporter(BlockPos pos, BlockState state) {
      super(MekanismBlocks.QIO_EXPORTER, pos, state);
   }

   @Override
   protected void onUpdateServer() {
      super.onUpdateServer();
      if (MekanismUtils.canFunction(this)) {
         if (this.delay > 0) {
            this.delay--;
            return;
         }

         this.tryEject();
         this.delay = 10;
      }
   }

   private void tryEject() {
      QIOFrequency freq = this.getQIOFrequency();
      if (freq != null) {
         Direction direction = this.getDirection();
         BlockEntity back = WorldUtils.getTileEntity(this.m_58904_(), this.f_58858_.m_121945_(direction.m_122424_()));
         LazyOptional<IItemHandler> backHandler = CapabilityUtils.getCapability(back, ForgeCapabilities.ITEM_HANDLER, direction);
         if (backHandler.isPresent()) {
            TileEntityQIOExporter.EfficientEjector<?> ejector;
            if (this.getFilterManager().hasEnabledFilters()) {
               ejector = this.filterEjector;
            } else {
               if (!this.exportWithoutFilter) {
                  return;
               }

               ejector = this.filterlessEjector;
            }

            ejector.eject(freq, (IItemHandler)backHandler.orElseThrow(MekanismUtils.MISSING_CAP_ERROR));
         }
      }
   }

   private Object2LongMap<HashedItem> getFilterEjectMap(QIOFrequency freq) {
      Object2LongMap<HashedItem> map = new Object2LongOpenHashMap();

      for (QIOFilter<?> filter : this.getFilterManager().getEnabledFilters()) {
         if (filter instanceof QIOItemStackFilter itemFilter) {
            if (itemFilter.fuzzyMode) {
               map.putAll(freq.getStacksByItem(itemFilter.getItemStack().m_41720_()));
            } else {
               HashedItem type = HashedItem.create(itemFilter.getItemStack());
               map.put(type, freq.getStored(type));
            }
         } else if (filter instanceof QIOTagFilter tagFilter) {
            String tagName = tagFilter.getTagName();
            map.putAll(freq.getStacksByTagWildcard(tagName));
         } else if (filter instanceof QIOModIDFilter modIDFilter) {
            String modID = modIDFilter.getModID();
            map.putAll(freq.getStacksByModIDWildcard(modID));
         }
      }

      return map;
   }

   @ComputerMethod
   public boolean getExportWithoutFilter() {
      return this.exportWithoutFilter;
   }

   public void toggleExportWithoutFilter() {
      this.exportWithoutFilter = !this.exportWithoutFilter;
      this.markForSave();
   }

   @Override
   public void addContainerTrackers(MekanismContainer container) {
      super.addContainerTrackers(container);
      container.track(SyncableBoolean.create(this::getExportWithoutFilter, value -> this.exportWithoutFilter = value));
   }

   @Override
   public void writeSustainedData(CompoundTag dataMap) {
      super.writeSustainedData(dataMap);
      dataMap.m_128379_("auto", this.exportWithoutFilter);
   }

   @Override
   public void readSustainedData(CompoundTag dataMap) {
      super.readSustainedData(dataMap);
      NBTUtils.setBooleanIfPresent(dataMap, "auto", value -> this.exportWithoutFilter = value);
   }

   @Override
   public Map<String, String> getTileDataRemap() {
      Map<String, String> remap = super.getTileDataRemap();
      remap.put("auto", "auto");
      return remap;
   }

   @ComputerMethod(
      requiresPublicSecurity = true
   )
   void setExportsWithoutFilter(boolean value) throws ComputerException {
      this.validateSecurityIsPublic();
      if (this.exportWithoutFilter != value) {
         this.toggleExportWithoutFilter();
      }
   }

   private final class EfficientEjector<T> {
      private static final double MAX_EJECT_ATTEMPTS = 100.0;
      private final Function<QIOFrequency, Collection<T>> ejectMapCalculator;
      private final Function<T, HashedItem> typeSupplier;
      private final ToIntFunction<T> countSupplier;

      private EfficientEjector(Function<T, HashedItem> typeSupplier, ToIntFunction<T> countSupplier, Function<QIOFrequency, Collection<T>> ejectMapCalculator) {
         this.typeSupplier = typeSupplier;
         this.countSupplier = countSupplier;
         this.ejectMapCalculator = ejectMapCalculator;
      }

      private void eject(QIOFrequency freq, IItemHandler inventory) {
         int slots = inventory.getSlots();
         if (slots != 0) {
            Collection<T> ejectMap = this.ejectMapCalculator.apply(freq);
            if (!ejectMap.isEmpty()) {
               RandomSource random = TileEntityQIOExporter.this.m_58904_().m_213780_();
               double ejectChance = Math.min(1.0, 100.0 / ejectMap.size());
               int maxTypes = TileEntityQIOExporter.this.getMaxTransitTypes();
               int maxCount = TileEntityQIOExporter.this.getMaxTransitCount();
               Object2IntMap<HashedItem> removed = new Object2IntOpenHashMap();
               int amountRemoved = 0;

               for (T obj : ejectMap) {
                  if (amountRemoved == maxCount || removed.size() == maxTypes) {
                     break;
                  }

                  if (!(random.m_188500_() > ejectChance)) {
                     HashedItem type = this.typeSupplier.apply(obj);
                     ItemStack origInsert = type.createStack(Math.min(maxCount - amountRemoved, this.countSupplier.applyAsInt(obj)));
                     ItemStack toInsert = origInsert.m_41777_();
                     int i = 0;

                     while (true) {
                        if (i < slots) {
                           toInsert = inventory.insertItem(i, toInsert, false);
                           if (!toInsert.m_41619_()) {
                              i++;
                              continue;
                           }
                        }

                        ItemStack toUse = TransporterManager.getToUse(origInsert, toInsert);
                        if (!toUse.m_41619_()) {
                           amountRemoved += toUse.m_41613_();
                           removed.merge(type, toUse.m_41613_(), Integer::sum);
                        }
                        break;
                     }
                  }
               }

               ObjectIterator var18 = removed.object2IntEntrySet().iterator();

               while (var18.hasNext()) {
                  Entry<HashedItem> entry = (Entry<HashedItem>)var18.next();
                  int amount = entry.getIntValue();
                  ItemStack ret = freq.removeByType((HashedItem)entry.getKey(), amount);
                  if (ret.m_41613_() != amount) {
                     Mekanism.logger.error("QIO ejection item removal didn't line up with prediction: removed {}, expected {}", ret.m_41613_(), amount);
                  }
               }
            }
         }
      }
   }
}
