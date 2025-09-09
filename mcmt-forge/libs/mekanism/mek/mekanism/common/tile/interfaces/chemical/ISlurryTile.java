package mekanism.common.tile.interfaces.chemical;

import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.capabilities.chemical.dynamic.DynamicChemicalHandler;
import mekanism.common.capabilities.chemical.dynamic.ISlurryTracker;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.resolver.manager.ChemicalHandlerManager;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@MethodsReturnNonnullByDefault
public interface ISlurryTile extends ISlurryTracker {
   ChemicalHandlerManager.SlurryHandlerManager getSlurryManager();

   default ChemicalHandlerManager.SlurryHandlerManager getInitialSlurryManager(IContentsListener listener) {
      return new ChemicalHandlerManager.SlurryHandlerManager(
         this.getInitialSlurryTanks(listener),
         new DynamicChemicalHandler.DynamicSlurryHandler(this::getSlurryTanks, this::extractSlurryCheck, this::insertSlurryCheck, listener)
      );
   }

   @Nullable
   default IChemicalTankHolder<Slurry, SlurryStack, ISlurryTank> getInitialSlurryTanks(IContentsListener listener) {
      return null;
   }

   default boolean canHandleSlurry() {
      return this.getSlurryManager().canHandle();
   }

   @Override
   default List<ISlurryTank> getSlurryTanks(@Nullable Direction side) {
      return this.getSlurryManager().getContainers(side);
   }

   default boolean extractSlurryCheck(int tank, @Nullable Direction side) {
      return true;
   }

   default boolean insertSlurryCheck(int tank, @Nullable Direction side) {
      return true;
   }
}
