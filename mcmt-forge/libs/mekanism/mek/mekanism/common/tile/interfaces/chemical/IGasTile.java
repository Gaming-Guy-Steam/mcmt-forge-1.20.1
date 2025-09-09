package mekanism.common.tile.interfaces.chemical;

import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.common.capabilities.chemical.dynamic.DynamicChemicalHandler;
import mekanism.common.capabilities.chemical.dynamic.IGasTracker;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.resolver.manager.ChemicalHandlerManager;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@MethodsReturnNonnullByDefault
public interface IGasTile extends IGasTracker {
   ChemicalHandlerManager.GasHandlerManager getGasManager();

   default ChemicalHandlerManager.GasHandlerManager getInitialGasManager(IContentsListener listener) {
      return new ChemicalHandlerManager.GasHandlerManager(
         this.getInitialGasTanks(listener),
         new DynamicChemicalHandler.DynamicGasHandler(this::getGasTanks, this::extractGasCheck, this::insertGasCheck, listener)
      );
   }

   @Nullable
   default IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks(IContentsListener listener) {
      return null;
   }

   default boolean canHandleGas() {
      return this.getGasManager().canHandle();
   }

   @Override
   default List<IGasTank> getGasTanks(@Nullable Direction side) {
      return this.getGasManager().getContainers(side);
   }

   default boolean extractGasCheck(int tank, @Nullable Direction side) {
      return true;
   }

   default boolean insertGasCheck(int tank, @Nullable Direction side) {
      return true;
   }
}
