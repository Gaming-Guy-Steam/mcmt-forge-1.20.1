package mekanism.common.tile.qio;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.Mekanism;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

public class TileEntityQIOImporter extends TileEntityQIOFilterHandler {
   private static final int MAX_DELAY = 10;
   private int delay = 0;
   private boolean importWithoutFilter = true;

   public TileEntityQIOImporter(BlockPos pos, BlockState state) {
      super(MekanismBlocks.QIO_IMPORTER, pos, state);
   }

   @Override
   protected void onUpdateServer() {
      super.onUpdateServer();
      if (MekanismUtils.canFunction(this)) {
         if (this.delay > 0) {
            this.delay--;
            return;
         }

         this.tryImport();
         this.delay = 10;
      }
   }

   private void tryImport() {
      QIOFrequency freq = this.getQIOFrequency();
      if (freq != null) {
         Direction direction = this.getDirection();
         BlockEntity back = WorldUtils.getTileEntity(this.m_58904_(), this.f_58858_.m_121945_(direction.m_122424_()));
         LazyOptional<IItemHandler> lazyCapability = CapabilityUtils.getCapability(back, ForgeCapabilities.ITEM_HANDLER, direction);
         if (lazyCapability.isPresent()) {
            Predicate<ItemStack> canFilter;
            if (this.getFilterManager().hasEnabledFilters()) {
               canFilter = stackx -> this.getFilterManager().anyEnabledMatch(filter -> filter.getFinder().modifies(stackx));
            } else {
               if (!this.importWithoutFilter) {
                  return;
               }

               canFilter = ConstantPredicates.alwaysTrue();
            }

            IItemHandler inventory = (IItemHandler)lazyCapability.orElseThrow(MekanismUtils.MISSING_CAP_ERROR);
            int slots = inventory.getSlots();
            if (slots != 0) {
               Set<HashedItem> typesAdded = new HashSet<>();
               int maxTypes = this.getMaxTransitTypes();
               int maxCount = this.getMaxTransitCount();
               int countAdded = 0;

               for (int i = slots - 1; i >= 0; i--) {
                  ItemStack stack = inventory.extractItem(i, maxCount - countAdded, true);
                  if (!stack.m_41619_()) {
                     HashedItem type = HashedItem.create(stack);
                     if ((typesAdded.contains(type) || typesAdded.size() != maxTypes) && canFilter.test(stack)) {
                        ItemStack used = TransporterManager.getToUse(stack, freq.addItem(stack));
                        ItemStack ret = inventory.extractItem(i, used.m_41613_(), false);
                        if (!InventoryUtils.areItemsStackable(used, ret) || used.m_41613_() != ret.m_41613_()) {
                           Mekanism.logger
                              .error(
                                 "QIO insertion error: item handler {} returned {} during simulated extraction, but returned {} during execution. This is wrong!",
                                 new Object[]{back, stack, ret}
                              );
                        }

                        typesAdded.add(type);
                        countAdded += used.m_41613_();
                     }
                  }
               }
            }
         }
      }
   }

   @ComputerMethod
   public boolean getImportWithoutFilter() {
      return this.importWithoutFilter;
   }

   public void toggleImportWithoutFilter() {
      this.importWithoutFilter = !this.importWithoutFilter;
      this.markForSave();
   }

   @Override
   public void addContainerTrackers(MekanismContainer container) {
      super.addContainerTrackers(container);
      container.track(SyncableBoolean.create(this::getImportWithoutFilter, value -> this.importWithoutFilter = value));
   }

   @Override
   public void writeSustainedData(CompoundTag dataMap) {
      super.writeSustainedData(dataMap);
      dataMap.m_128379_("auto", this.importWithoutFilter);
   }

   @Override
   public void readSustainedData(CompoundTag dataMap) {
      super.readSustainedData(dataMap);
      NBTUtils.setBooleanIfPresent(dataMap, "auto", value -> this.importWithoutFilter = value);
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
   void setImportsWithoutFilter(boolean value) throws ComputerException {
      this.validateSecurityIsPublic();
      if (this.importWithoutFilter != value) {
         this.toggleImportWithoutFilter();
      }
   }
}
