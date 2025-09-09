package mekanism.common.content.matrix;

import mekanism.api.math.FloatingLong;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.sync.dynamic.ContainerSync;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.lib.multiblock.MultiblockCache;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.tile.multiblock.TileEntityInductionCasing;
import mekanism.common.tile.multiblock.TileEntityInductionCell;
import mekanism.common.tile.multiblock.TileEntityInductionProvider;
import mekanism.common.util.MekanismUtils;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class MatrixMultiblockData extends MultiblockData {
   public static final String STATS_TAB = "stats";
   @NotNull
   private final MatrixEnergyContainer energyContainer;
   @ContainerSync(
      getter = "getLastOutput"
   )
   private FloatingLong clientLastOutput = FloatingLong.ZERO;
   @ContainerSync(
      getter = "getLastInput"
   )
   private FloatingLong clientLastInput = FloatingLong.ZERO;
   @ContainerSync(
      getter = "getEnergy"
   )
   private FloatingLong clientEnergy = FloatingLong.ZERO;
   @ContainerSync(
      tags = {"stats"},
      getter = "getTransferCap"
   )
   private FloatingLong clientMaxTransfer = FloatingLong.ZERO;
   @ContainerSync(
      getter = "getStorageCap"
   )
   private FloatingLong clientMaxEnergy = FloatingLong.ZERO;
   @ContainerSync(
      tags = {"stats"},
      getter = "getProviderCount"
   )
   private int clientProviders;
   @ContainerSync(
      tags = {"stats"},
      getter = "getCellCount"
   )
   private int clientCells;
   @NotNull
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getInputItem"},
      docPlaceholder = "input slot"
   )
   final EnergyInventorySlot energyInputSlot;
   @NotNull
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getOutputItem"},
      docPlaceholder = "output slot"
   )
   final EnergyInventorySlot energyOutputSlot;

   public MatrixMultiblockData(TileEntityInductionCasing tile) {
      super(tile);
      this.energyContainers.add(this.energyContainer = new MatrixEnergyContainer(this));
      this.inventorySlots.add(this.energyInputSlot = EnergyInventorySlot.drain(this.energyContainer, this, 146, 21));
      this.inventorySlots.add(this.energyOutputSlot = EnergyInventorySlot.fillOrConvert(this.energyContainer, tile::m_58904_, this, 146, 51));
      this.energyInputSlot.setSlotOverlay(SlotOverlay.PLUS);
      this.energyOutputSlot.setSlotOverlay(SlotOverlay.MINUS);
   }

   @Override
   protected int getMultiblockRedstoneLevel() {
      return MekanismUtils.redstoneLevelFromContents(this.getEnergy(), this.getStorageCap());
   }

   @Override
   protected boolean shouldCap(MultiblockCache.CacheSubstance<?, ?> type) {
      return type != MultiblockCache.CacheSubstance.ENERGY;
   }

   public void addCell(TileEntityInductionCell cell) {
      this.energyContainer.addCell(cell.m_58899_(), cell);
   }

   public void addProvider(TileEntityInductionProvider provider) {
      this.energyContainer.addProvider(provider.m_58899_(), provider);
   }

   @NotNull
   public MatrixEnergyContainer getEnergyContainer() {
      return this.energyContainer;
   }

   public FloatingLong getEnergy() {
      return this.isRemote() ? this.clientEnergy : this.energyContainer.getEnergy();
   }

   @Override
   public boolean tick(Level world) {
      boolean ret = super.tick(world);
      this.energyContainer.tick();
      this.energyInputSlot.drainContainer();
      this.energyOutputSlot.fillContainerOrConvert();
      if (!this.getLastInput().isZero() || !this.getLastOutput().isZero()) {
         this.markDirtyComparator(world);
      }

      return ret;
   }

   @Override
   public void remove(Level world) {
      this.energyContainer.invalidate();
      super.remove(world);
   }

   public FloatingLong getStorageCap() {
      return this.isRemote() ? this.clientMaxEnergy : this.energyContainer.getMaxEnergy();
   }

   @ComputerMethod
   public FloatingLong getTransferCap() {
      return this.isRemote() ? this.clientMaxTransfer : this.energyContainer.getMaxTransfer();
   }

   @ComputerMethod
   public FloatingLong getLastInput() {
      return this.isRemote() ? this.clientLastInput : this.energyContainer.getLastInput();
   }

   @ComputerMethod
   public FloatingLong getLastOutput() {
      return this.isRemote() ? this.clientLastOutput : this.energyContainer.getLastOutput();
   }

   @ComputerMethod(
      nameOverride = "getInstalledCells"
   )
   public int getCellCount() {
      return this.isRemote() ? this.clientCells : this.energyContainer.getCells();
   }

   @ComputerMethod(
      nameOverride = "getInstalledProviders"
   )
   public int getProviderCount() {
      return this.isRemote() ? this.clientProviders : this.energyContainer.getProviders();
   }
}
