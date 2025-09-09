package mekanism.common.item.interfaces;

import java.util.Optional;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface IGasItem {
   @NotNull
   default GasStack useGas(ItemStack stack, long amount) {
      Optional<IGasHandler> capability = stack.getCapability(Capabilities.GAS_HANDLER).resolve();
      if (capability.isPresent()) {
         IGasHandler gasHandlerItem = capability.get();
         if (gasHandlerItem instanceof IGasHandler.IMekanismGasHandler gasHandler) {
            IGasTank gasTank = gasHandler.getChemicalTank(0, null);
            if (gasTank != null) {
               return gasTank.extract(amount, Action.EXECUTE, AutomationType.MANUAL);
            }
         }

         return gasHandlerItem.extractChemical(amount, Action.EXECUTE);
      } else {
         return GasStack.EMPTY;
      }
   }

   default boolean hasGas(ItemStack stack) {
      return stack.getCapability(Capabilities.GAS_HANDLER).map(handler -> {
         int tank = 0;

         for (int tanks = handler.getTanks(); tank < tanks; tank++) {
            if (!handler.getChemicalInTank(tank).isEmpty()) {
               return true;
            }
         }

         return false;
      }).orElse(false);
   }
}
