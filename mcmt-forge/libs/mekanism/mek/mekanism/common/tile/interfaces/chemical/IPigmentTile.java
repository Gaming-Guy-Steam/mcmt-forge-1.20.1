package mekanism.common.tile.interfaces.chemical;

import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.common.capabilities.chemical.dynamic.DynamicChemicalHandler;
import mekanism.common.capabilities.chemical.dynamic.IPigmentTracker;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.resolver.manager.ChemicalHandlerManager;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@MethodsReturnNonnullByDefault
public interface IPigmentTile extends IPigmentTracker {
   ChemicalHandlerManager.PigmentHandlerManager getPigmentManager();

   default ChemicalHandlerManager.PigmentHandlerManager getInitialPigmentManager(IContentsListener listener) {
      return new ChemicalHandlerManager.PigmentHandlerManager(
         this.getInitialPigmentTanks(listener),
         new DynamicChemicalHandler.DynamicPigmentHandler(this::getPigmentTanks, this::extractPigmentCheck, this::insertPigmentCheck, listener)
      );
   }

   @Nullable
   default IChemicalTankHolder<Pigment, PigmentStack, IPigmentTank> getInitialPigmentTanks(IContentsListener listener) {
      return null;
   }

   default boolean canHandlePigment() {
      return this.getPigmentManager().canHandle();
   }

   @Override
   default List<IPigmentTank> getPigmentTanks(@Nullable Direction side) {
      return this.getPigmentManager().getContainers(side);
   }

   default boolean extractPigmentCheck(int tank, @Nullable Direction side) {
      return true;
   }

   default boolean insertPigmentCheck(int tank, @Nullable Direction side) {
      return true;
   }
}
