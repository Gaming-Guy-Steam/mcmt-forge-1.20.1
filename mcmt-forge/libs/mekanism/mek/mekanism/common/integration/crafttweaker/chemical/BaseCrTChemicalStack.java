package mekanism.common.integration.crafttweaker.chemical;

import java.util.function.Function;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.common.util.ChemicalUtil;

public abstract class BaseCrTChemicalStack<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, CRT_STACK extends ICrTChemicalStack<CHEMICAL, STACK, CRT_STACK>>
   implements ICrTChemicalStack<CHEMICAL, STACK, CRT_STACK> {
   protected final STACK stack;
   protected final Function<STACK, CRT_STACK> stackConverter;

   public BaseCrTChemicalStack(STACK stack, Function<STACK, CRT_STACK> stackConverter) {
      this.stack = stack;
      this.stackConverter = stackConverter;
   }

   protected StringBuilder getBracket() {
      return new StringBuilder().append('<').append(this.getBracketName()).append(':').append(this.stack.getTypeRegistryName()).append('>');
   }

   public String getCommandString() {
      StringBuilder builder = this.getBracket();
      if (!this.stack.isEmpty() && this.stack.getAmount() != 1L) {
         builder.append(" * ").append(this.stack.getAmount());
      }

      return builder.toString();
   }

   @Override
   public CRT_STACK copy() {
      return this.stackConverter.apply(ChemicalUtil.copy(this.stack));
   }

   @Override
   public STACK getInternal() {
      return this.stack;
   }

   @Override
   public String toString() {
      return this.getCommandString();
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         return o != null && this.getClass() == o.getClass() ? this.stack.equals(((BaseCrTChemicalStack)o).stack) : false;
      }
   }

   @Override
   public int hashCode() {
      return this.stack.hashCode();
   }
}
