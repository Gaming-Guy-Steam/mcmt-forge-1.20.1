package mekanism.common.tile.component.config.slot;

import java.util.List;
import java.util.function.Supplier;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.inventory.IInventorySlot;

public interface IProxiedSlotInfo extends ISlotInfo {
   public static class EnergyProxy extends EnergySlotInfo implements IProxiedSlotInfo {
      private final Supplier<List<IEnergyContainer>> containerSupplier;

      public EnergyProxy(boolean canInput, boolean canOutput, Supplier<List<IEnergyContainer>> containerSupplier) {
         super(canInput, canOutput);
         this.containerSupplier = containerSupplier;
      }

      @Override
      public List<IEnergyContainer> getContainers() {
         return this.containerSupplier.get();
      }
   }

   public static class FluidProxy extends FluidSlotInfo implements IProxiedSlotInfo {
      private final Supplier<List<IExtendedFluidTank>> tankSupplier;

      public FluidProxy(boolean canInput, boolean canOutput, Supplier<List<IExtendedFluidTank>> tankSupplier) {
         super(canInput, canOutput);
         this.tankSupplier = tankSupplier;
      }

      @Override
      public List<IExtendedFluidTank> getTanks() {
         return this.tankSupplier.get();
      }
   }

   public static class GasProxy extends ChemicalSlotInfo.GasSlotInfo implements IProxiedSlotInfo {
      private final Supplier<List<IGasTank>> tankSupplier;

      public GasProxy(boolean canInput, boolean canOutput, Supplier<List<IGasTank>> tankSupplier) {
         super(canInput, canOutput);
         this.tankSupplier = tankSupplier;
      }

      @Override
      public List<IGasTank> getTanks() {
         return this.tankSupplier.get();
      }
   }

   public static class HeatProxy extends HeatSlotInfo implements IProxiedSlotInfo {
      private final Supplier<List<IHeatCapacitor>> capacitorSupplier;

      public HeatProxy(boolean canInput, boolean canOutput, Supplier<List<IHeatCapacitor>> capacitorSupplier) {
         super(canInput, canOutput);
         this.capacitorSupplier = capacitorSupplier;
      }

      @Override
      public List<IHeatCapacitor> getHeatCapacitors() {
         return this.capacitorSupplier.get();
      }
   }

   public static class InfusionProxy extends ChemicalSlotInfo.InfusionSlotInfo implements IProxiedSlotInfo {
      private final Supplier<List<IInfusionTank>> tankSupplier;

      public InfusionProxy(boolean canInput, boolean canOutput, Supplier<List<IInfusionTank>> tankSupplier) {
         super(canInput, canOutput);
         this.tankSupplier = tankSupplier;
      }

      @Override
      public List<IInfusionTank> getTanks() {
         return this.tankSupplier.get();
      }
   }

   public static class InventoryProxy extends InventorySlotInfo implements IProxiedSlotInfo {
      private final Supplier<List<IInventorySlot>> slotSupplier;

      public InventoryProxy(boolean canInput, boolean canOutput, Supplier<List<IInventorySlot>> slotSupplier) {
         super(canInput, canOutput);
         this.slotSupplier = slotSupplier;
      }

      @Override
      public List<IInventorySlot> getSlots() {
         return this.slotSupplier.get();
      }
   }

   public static class PigmentProxy extends ChemicalSlotInfo.PigmentSlotInfo implements IProxiedSlotInfo {
      private final Supplier<List<IPigmentTank>> tankSupplier;

      public PigmentProxy(boolean canInput, boolean canOutput, Supplier<List<IPigmentTank>> tankSupplier) {
         super(canInput, canOutput);
         this.tankSupplier = tankSupplier;
      }

      @Override
      public List<IPigmentTank> getTanks() {
         return this.tankSupplier.get();
      }
   }

   @FunctionalInterface
   public interface ProxySlotInfoCreator<T> {
      IProxiedSlotInfo create(boolean canInput, boolean canOutput, Supplier<List<T>> supplier);
   }

   public static class SlurryProxy extends ChemicalSlotInfo.SlurrySlotInfo implements IProxiedSlotInfo {
      private final Supplier<List<ISlurryTank>> tankSupplier;

      public SlurryProxy(boolean canInput, boolean canOutput, Supplier<List<ISlurryTank>> tankSupplier) {
         super(canInput, canOutput);
         this.tankSupplier = tankSupplier;
      }

      @Override
      public List<ISlurryTank> getTanks() {
         return this.tankSupplier.get();
      }
   }
}
