package mekanism.common.tile.qio;

import java.util.List;
import java.util.Map;
import mekanism.api.Upgrade;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.content.filter.SortableFilterManager;
import mekanism.common.content.qio.filter.QIOFilter;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityQIOFilterHandler extends TileEntityQIOComponent implements ITileFilterHolder<QIOFilter<?>> {
   private final SortableFilterManager<QIOFilter<?>> filterManager = new SortableFilterManager<>(QIOFilter.class, this::markForSave);

   public TileEntityQIOFilterHandler(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
      super(blockProvider, pos, state);
   }

   public SortableFilterManager<QIOFilter<?>> getFilterManager() {
      return this.filterManager;
   }

   @Override
   public void writeSustainedData(CompoundTag dataMap) {
      super.writeSustainedData(dataMap);
      this.filterManager.writeToNBT(dataMap);
   }

   @Override
   public void readSustainedData(CompoundTag dataMap) {
      super.readSustainedData(dataMap);
      this.filterManager.readFromNBT(dataMap);
   }

   @Override
   public Map<String, String> getTileDataRemap() {
      Map<String, String> remap = super.getTileDataRemap();
      remap.put("filters", "filters");
      return remap;
   }

   @Override
   public void addContainerTrackers(MekanismContainer container) {
      super.addContainerTrackers(container);
      this.filterManager.addContainerTrackers(container);
   }

   protected int getMaxTransitCount() {
      return 64 + 32 * this.upgradeComponent.getUpgrades(Upgrade.SPEED);
   }

   protected int getMaxTransitTypes() {
      return Math.round(1.0F + this.upgradeComponent.getUpgrades(Upgrade.SPEED) / 2.0F);
   }

   @ComputerMethod
   List<QIOFilter<?>> getFilters() {
      return this.filterManager.getFilters();
   }

   @ComputerMethod(
      requiresPublicSecurity = true
   )
   boolean addFilter(QIOFilter<?> filter) throws ComputerException {
      this.validateSecurityIsPublic();
      return this.filterManager.addFilter(filter);
   }

   @ComputerMethod(
      requiresPublicSecurity = true
   )
   boolean removeFilter(QIOFilter<?> filter) throws ComputerException {
      this.validateSecurityIsPublic();
      return this.filterManager.removeFilter(filter);
   }
}
