package mekanism.common.integration.crafttweaker.chemical;

import java.util.function.Function;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.util.ChemicalUtil;

public abstract class CrTMutableChemicalStack<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, CRT_STACK extends ICrTChemicalStack<CHEMICAL, STACK, CRT_STACK>>
   extends BaseCrTChemicalStack<CHEMICAL, STACK, CRT_STACK> {
   public CrTMutableChemicalStack(STACK stack, Function<STACK, CRT_STACK> stackConverter) {
      super(stack, stackConverter);
   }

   @Override
   public CRT_STACK setAmount(long amount) {
      this.stack.setAmount(amount);
      return this.asMutable();
   }

   @Override
   public CRT_STACK asMutable() {
      return (CRT_STACK)this;
   }

   @Override
   public CRT_STACK asImmutable() {
      return this.stackConverter.apply(this.stack);
   }

   @Override
   public STACK getImmutableInternal() {
      return ChemicalUtil.copy(this.stack);
   }

   @Override
   protected StringBuilder getBracket() {
      return super.getBracket().append(".mutable()");
   }

   public static class CrTMutableGasStack
      extends CrTMutableChemicalStack<Gas, GasStack, ICrTChemicalStack.ICrTGasStack>
      implements ICrTChemicalStack.ICrTGasStack {
      public CrTMutableGasStack(GasStack stack) {
         super(stack, CrTMutableChemicalStack.CrTMutableGasStack::new);
      }
   }

   public static class CrTMutableInfusionStack
      extends CrTMutableChemicalStack<InfuseType, InfusionStack, ICrTChemicalStack.ICrTInfusionStack>
      implements ICrTChemicalStack.ICrTInfusionStack {
      public CrTMutableInfusionStack(InfusionStack stack) {
         super(stack, CrTMutableChemicalStack.CrTMutableInfusionStack::new);
      }
   }

   public static class CrTMutablePigmentStack
      extends CrTMutableChemicalStack<Pigment, PigmentStack, ICrTChemicalStack.ICrTPigmentStack>
      implements ICrTChemicalStack.ICrTPigmentStack {
      public CrTMutablePigmentStack(PigmentStack stack) {
         super(stack, CrTMutableChemicalStack.CrTMutablePigmentStack::new);
      }
   }

   public static class CrTMutableSlurryStack
      extends CrTMutableChemicalStack<Slurry, SlurryStack, ICrTChemicalStack.ICrTSlurryStack>
      implements ICrTChemicalStack.ICrTSlurryStack {
      public CrTMutableSlurryStack(SlurryStack stack) {
         super(stack, CrTMutableChemicalStack.CrTMutableSlurryStack::new);
      }
   }
}
