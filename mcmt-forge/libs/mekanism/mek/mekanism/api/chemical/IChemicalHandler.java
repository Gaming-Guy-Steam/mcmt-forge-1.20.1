package mekanism.api.chemical;

import mekanism.api.Action;
import mekanism.api.annotations.NothingNullByDefault;

@NothingNullByDefault
public interface IChemicalHandler<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> extends IEmptyStackProvider<CHEMICAL, STACK> {
   int getTanks();

   STACK getChemicalInTank(int var1);

   void setChemicalInTank(int var1, STACK var2);

   long getTankCapacity(int var1);

   boolean isValid(int var1, STACK var2);

   STACK insertChemical(int var1, STACK var2, Action var3);

   STACK extractChemical(int var1, long var2, Action var4);

   default STACK insertChemical(STACK stack, Action action) {
      return ChemicalUtils.insert(stack, action, this.getEmptyStack(), this::getTanks, this::getChemicalInTank, this::insertChemical);
   }

   default STACK extractChemical(long amount, Action action) {
      return ChemicalUtils.extract(amount, action, this.getEmptyStack(), this::getTanks, this::getChemicalInTank, this::extractChemical);
   }

   default STACK extractChemical(STACK stack, Action action) {
      return ChemicalUtils.extract(stack, action, this.getEmptyStack(), this::getTanks, this::getChemicalInTank, this::extractChemical);
   }
}
