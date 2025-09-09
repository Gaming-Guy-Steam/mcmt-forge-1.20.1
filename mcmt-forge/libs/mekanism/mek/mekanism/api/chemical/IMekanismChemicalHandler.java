package mekanism.api.chemical;

import java.util.List;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public interface IMekanismChemicalHandler<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>>
   extends ISidedChemicalHandler<CHEMICAL, STACK>,
   IContentsListener {
   default boolean canHandle() {
      return true;
   }

   List<TANK> getChemicalTanks(@Nullable Direction var1);

   @Nullable
   default TANK getChemicalTank(int tank, @Nullable Direction side) {
      List<TANK> tanks = this.getChemicalTanks(side);
      return tank >= 0 && tank < tanks.size() ? tanks.get(tank) : null;
   }

   @Override
   default int getTanks(@Nullable Direction side) {
      return this.getChemicalTanks(side).size();
   }

   @Override
   default STACK getChemicalInTank(int tank, @Nullable Direction side) {
      TANK chemicalTank = this.getChemicalTank(tank, side);
      return chemicalTank == null ? this.getEmptyStack() : chemicalTank.getStack();
   }

   @Override
   default void setChemicalInTank(int tank, STACK stack, @Nullable Direction side) {
      TANK chemicalTank = this.getChemicalTank(tank, side);
      if (chemicalTank != null) {
         chemicalTank.setStack(stack);
      }
   }

   @Override
   default long getTankCapacity(int tank, @Nullable Direction side) {
      TANK chemicalTank = this.getChemicalTank(tank, side);
      return chemicalTank == null ? 0L : chemicalTank.getCapacity();
   }

   @Override
   default boolean isValid(int tank, STACK stack, @Nullable Direction side) {
      TANK chemicalTank = this.getChemicalTank(tank, side);
      return chemicalTank != null && chemicalTank.isValid(stack);
   }

   @Override
   default STACK insertChemical(int tank, STACK stack, @Nullable Direction side, Action action) {
      TANK chemicalTank = this.getChemicalTank(tank, side);
      return chemicalTank == null ? stack : chemicalTank.insert(stack, action, side == null ? AutomationType.INTERNAL : AutomationType.EXTERNAL);
   }

   @Override
   default STACK extractChemical(int tank, long amount, @Nullable Direction side, Action action) {
      TANK chemicalTank = this.getChemicalTank(tank, side);
      return chemicalTank == null
         ? this.getEmptyStack()
         : chemicalTank.extract(amount, action, side == null ? AutomationType.INTERNAL : AutomationType.EXTERNAL);
   }
}
