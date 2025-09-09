package mekanism.api.chemical;

import mekanism.api.Action;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public interface ISidedChemicalHandler<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> extends IChemicalHandler<CHEMICAL, STACK> {
   @Nullable
   default Direction getSideFor() {
      return null;
   }

   int getTanks(@Nullable Direction var1);

   @Override
   default int getTanks() {
      return this.getTanks(this.getSideFor());
   }

   STACK getChemicalInTank(int var1, @Nullable Direction var2);

   @Override
   default STACK getChemicalInTank(int tank) {
      return this.getChemicalInTank(tank, this.getSideFor());
   }

   void setChemicalInTank(int var1, STACK var2, @Nullable Direction var3);

   @Override
   default void setChemicalInTank(int tank, STACK stack) {
      this.setChemicalInTank(tank, stack, this.getSideFor());
   }

   long getTankCapacity(int var1, @Nullable Direction var2);

   @Override
   default long getTankCapacity(int tank) {
      return this.getTankCapacity(tank, this.getSideFor());
   }

   boolean isValid(int var1, STACK var2, @Nullable Direction var3);

   @Override
   default boolean isValid(int tank, STACK stack) {
      return this.isValid(tank, stack, this.getSideFor());
   }

   STACK insertChemical(int var1, STACK var2, @Nullable Direction var3, Action var4);

   @Override
   default STACK insertChemical(int tank, STACK stack, Action action) {
      return this.insertChemical(tank, stack, this.getSideFor(), action);
   }

   STACK extractChemical(int var1, long var2, @Nullable Direction var4, Action var5);

   @Override
   default STACK extractChemical(int tank, long amount, Action action) {
      return this.extractChemical(tank, amount, this.getSideFor(), action);
   }

   default STACK insertChemical(STACK stack, @Nullable Direction side, Action action) {
      return ChemicalUtils.insert(
         stack,
         action,
         this.getEmptyStack(),
         () -> this.getTanks(side),
         tank -> this.getChemicalInTank(tank, side),
         (tank, s, a) -> this.insertChemical(tank, s, side, a)
      );
   }

   default STACK extractChemical(long amount, @Nullable Direction side, Action action) {
      return ChemicalUtils.extract(
         amount,
         action,
         this.getEmptyStack(),
         () -> this.getTanks(side),
         tank -> this.getChemicalInTank(tank, side),
         (tank, a, act) -> this.extractChemical(tank, a, side, act)
      );
   }

   default STACK extractChemical(STACK stack, @Nullable Direction side, Action action) {
      return ChemicalUtils.extract(
         stack,
         action,
         this.getEmptyStack(),
         () -> this.getTanks(side),
         tank -> this.getChemicalInTank(tank, side),
         (tank, a, act) -> this.extractChemical(tank, a, side, act)
      );
   }
}
