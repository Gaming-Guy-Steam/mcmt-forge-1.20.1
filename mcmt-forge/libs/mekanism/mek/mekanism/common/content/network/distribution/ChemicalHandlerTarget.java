package mekanism.common.content.network.distribution;

import mekanism.api.Action;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.common.lib.distribution.SplitInfo;
import mekanism.common.lib.distribution.Target;
import mekanism.common.util.ChemicalUtil;
import org.jetbrains.annotations.NotNull;

public class ChemicalHandlerTarget<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, HANDLER extends IChemicalHandler<CHEMICAL, STACK>>
   extends Target<HANDLER, Long, STACK> {
   public ChemicalHandlerTarget(@NotNull STACK type) {
      this.extra = type;
   }

   public ChemicalHandlerTarget(@NotNull STACK type, int expectedSize) {
      super(expectedSize);
      this.extra = type;
   }

   protected void acceptAmount(HANDLER handler, SplitInfo<Long> splitInfo, Long amount) {
      splitInfo.send(amount - handler.insertChemical(ChemicalUtil.copyWithAmount(this.extra, amount), Action.EXECUTE).getAmount());
   }

   protected Long simulate(HANDLER handler, @NotNull STACK stack) {
      return stack.getAmount() - handler.insertChemical(stack, Action.SIMULATE).getAmount();
   }
}
