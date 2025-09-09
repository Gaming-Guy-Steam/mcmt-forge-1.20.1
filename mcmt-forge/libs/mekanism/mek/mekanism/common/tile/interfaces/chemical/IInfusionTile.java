package mekanism.common.tile.interfaces.chemical;

import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.common.capabilities.chemical.dynamic.DynamicChemicalHandler;
import mekanism.common.capabilities.chemical.dynamic.IInfusionTracker;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.resolver.manager.ChemicalHandlerManager;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@MethodsReturnNonnullByDefault
public interface IInfusionTile extends IInfusionTracker {
   ChemicalHandlerManager.InfusionHandlerManager getInfusionManager();

   default ChemicalHandlerManager.InfusionHandlerManager getInitialInfusionManager(IContentsListener listener) {
      return new ChemicalHandlerManager.InfusionHandlerManager(
         this.getInitialInfusionTanks(listener),
         new DynamicChemicalHandler.DynamicInfusionHandler(this::getInfusionTanks, this::extractInfusionCheck, this::insertInfusionCheck, listener)
      );
   }

   @Nullable
   default IChemicalTankHolder<InfuseType, InfusionStack, IInfusionTank> getInitialInfusionTanks(IContentsListener listener) {
      return null;
   }

   default boolean canHandleInfusion() {
      return this.getInfusionManager().canHandle();
   }

   @Override
   default List<IInfusionTank> getInfusionTanks(@Nullable Direction side) {
      return this.getInfusionManager().getContainers(side);
   }

   default boolean extractInfusionCheck(int tank, @Nullable Direction side) {
      return true;
   }

   default boolean insertInfusionCheck(int tank, @Nullable Direction side) {
      return true;
   }
}
