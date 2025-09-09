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

public abstract class CrTChemicalStack<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, CRT_STACK extends ICrTChemicalStack<CHEMICAL, STACK, CRT_STACK>>
   extends BaseCrTChemicalStack<CHEMICAL, STACK, CRT_STACK> {
   private final Function<STACK, CRT_STACK> mutableStackConverter;

   public CrTChemicalStack(STACK stack, Function<STACK, CRT_STACK> stackConverter, Function<STACK, CRT_STACK> mutableStackConverter) {
      super(stack, stackConverter);
      this.mutableStackConverter = mutableStackConverter;
   }

   @Override
   public CRT_STACK setAmount(long amount) {
      return this.stackConverter.apply(ChemicalUtil.copyWithAmount(this.stack, amount));
   }

   @Override
   public CRT_STACK asMutable() {
      return this.mutableStackConverter.apply(this.stack);
   }

   @Override
   public CRT_STACK asImmutable() {
      return (CRT_STACK)this;
   }

   @Override
   public STACK getImmutableInternal() {
      return this.getInternal();
   }

   public static class CrTGasStack extends CrTChemicalStack<Gas, GasStack, ICrTChemicalStack.ICrTGasStack> implements ICrTChemicalStack.ICrTGasStack {
      public CrTGasStack(GasStack stack) {
         super(stack, CrTChemicalStack.CrTGasStack::new, CrTMutableChemicalStack.CrTMutableGasStack::new);
      }
   }

   public static class CrTInfusionStack
      extends CrTChemicalStack<InfuseType, InfusionStack, ICrTChemicalStack.ICrTInfusionStack>
      implements ICrTChemicalStack.ICrTInfusionStack {
      public CrTInfusionStack(InfusionStack stack) {
         super(stack, CrTChemicalStack.CrTInfusionStack::new, CrTMutableChemicalStack.CrTMutableInfusionStack::new);
      }
   }

   public static class CrTPigmentStack
      extends CrTChemicalStack<Pigment, PigmentStack, ICrTChemicalStack.ICrTPigmentStack>
      implements ICrTChemicalStack.ICrTPigmentStack {
      public CrTPigmentStack(PigmentStack stack) {
         super(stack, CrTChemicalStack.CrTPigmentStack::new, CrTMutableChemicalStack.CrTMutablePigmentStack::new);
      }
   }

   public static class CrTSlurryStack
      extends CrTChemicalStack<Slurry, SlurryStack, ICrTChemicalStack.ICrTSlurryStack>
      implements ICrTChemicalStack.ICrTSlurryStack {
      public CrTSlurryStack(SlurryStack stack) {
         super(stack, CrTChemicalStack.CrTSlurryStack::new, CrTMutableChemicalStack.CrTMutableSlurryStack::new);
      }
   }
}
