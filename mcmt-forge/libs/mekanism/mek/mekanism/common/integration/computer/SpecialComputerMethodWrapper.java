package mekanism.common.integration.computer;

import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class SpecialComputerMethodWrapper {
   public static class ComputerChemicalTankWrapper extends SpecialComputerMethodWrapper {
      @WrappingComputerMethod.WrappingComputerMethodIndex(0)
      @WrappingComputerMethod.WrappingComputerMethodHelp("Get the contents of the %s.")
      public static ChemicalStack<?> getStack(IChemicalTank<?, ?> tank) {
         return tank.getStack();
      }

      @WrappingComputerMethod.WrappingComputerMethodIndex(1)
      @WrappingComputerMethod.WrappingComputerMethodHelp("Get the capacity of the %s.")
      public static long getCapacity(IChemicalTank<?, ?> tank) {
         return tank.getCapacity();
      }

      @WrappingComputerMethod.WrappingComputerMethodIndex(2)
      @WrappingComputerMethod.WrappingComputerMethodHelp("Get the amount needed to fill the %s.")
      public static long getNeeded(IChemicalTank<?, ?> tank) {
         return tank.getNeeded();
      }

      @WrappingComputerMethod.WrappingComputerMethodIndex(3)
      @WrappingComputerMethod.WrappingComputerMethodHelp("Get the filled percentage of the %s.")
      public static double getFilledPercentage(IChemicalTank<?, ?> tank) {
         return (double)tank.getStored() / tank.getCapacity();
      }
   }

   public static class ComputerFluidTankWrapper extends SpecialComputerMethodWrapper {
      @WrappingComputerMethod.WrappingComputerMethodIndex(0)
      @WrappingComputerMethod.WrappingComputerMethodHelp("Get the contents of the %s.")
      public static FluidStack getStack(IExtendedFluidTank tank) {
         return tank.getFluid();
      }

      @WrappingComputerMethod.WrappingComputerMethodIndex(1)
      @WrappingComputerMethod.WrappingComputerMethodHelp("Get the capacity of the %s.")
      public static int getCapacity(IExtendedFluidTank tank) {
         return tank.getCapacity();
      }

      @WrappingComputerMethod.WrappingComputerMethodIndex(2)
      @WrappingComputerMethod.WrappingComputerMethodHelp("Get the amount needed to fill the %s.")
      public static int getNeeded(IExtendedFluidTank tank) {
         return tank.getNeeded();
      }

      @WrappingComputerMethod.WrappingComputerMethodIndex(3)
      @WrappingComputerMethod.WrappingComputerMethodHelp("Get the filled percentage of the %s.")
      public static double getFilledPercentage(IExtendedFluidTank tank) {
         return (double)tank.getFluidAmount() / tank.getCapacity();
      }
   }

   public static class ComputerHeatCapacitorWrapper extends SpecialComputerMethodWrapper {
      @WrappingComputerMethod.WrappingComputerMethodHelp("Get the temperature of the %s in Kelvin.")
      public static double getTemperature(IHeatCapacitor capacitor) {
         return capacitor.getTemperature();
      }
   }

   public static class ComputerIInventorySlotWrapper extends SpecialComputerMethodWrapper {
      @WrappingComputerMethod.WrappingComputerMethodHelp("Get the contents of the %s.")
      public static ItemStack getStack(IInventorySlot slot) {
         return slot.getStack();
      }
   }
}
