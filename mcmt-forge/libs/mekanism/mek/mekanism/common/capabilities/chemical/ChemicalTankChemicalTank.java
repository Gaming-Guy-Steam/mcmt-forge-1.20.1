package mekanism.common.capabilities.chemical;

import java.util.Objects;
import java.util.function.LongSupplier;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.BasicChemicalTank;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.merged.MergedChemicalTank;
import mekanism.api.chemical.pigment.IPigmentHandler;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.ISlurryHandler;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.tier.ChemicalTankTier;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public abstract class ChemicalTankChemicalTank<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>>
   extends BasicChemicalTank<CHEMICAL, STACK> {
   private final boolean isCreative;
   private final LongSupplier rate;

   public static MergedChemicalTank create(ChemicalTankTier tier, @Nullable IContentsListener listener) {
      Objects.requireNonNull(tier, "Chemical tank tier cannot be null");
      return MergedChemicalTank.create(
         new ChemicalTankChemicalTank.GasTankChemicalTank(tier, listener),
         new ChemicalTankChemicalTank.InfusionTankChemicalTank(tier, listener),
         new ChemicalTankChemicalTank.PigmentTankChemicalTank(tier, listener),
         new ChemicalTankChemicalTank.SlurryTankChemicalTank(tier, listener)
      );
   }

   private ChemicalTankChemicalTank(ChemicalTankTier tier, ChemicalTankBuilder<CHEMICAL, STACK, ?> tankBuilder, @Nullable IContentsListener listener) {
      super(
         tier.getStorage(),
         tankBuilder.alwaysTrueBi,
         tankBuilder.alwaysTrueBi,
         tankBuilder.alwaysTrue,
         tier == ChemicalTankTier.CREATIVE ? ChemicalAttributeValidator.ALWAYS_ALLOW : null,
         listener
      );
      this.isCreative = tier == ChemicalTankTier.CREATIVE;
      this.rate = tier::getOutput;
   }

   @Override
   protected long getRate(@Nullable AutomationType automationType) {
      return automationType == AutomationType.INTERNAL ? this.rate.getAsLong() : super.getRate(automationType);
   }

   @Override
   public STACK insert(STACK stack, Action action, AutomationType automationType) {
      if (this.isCreative && this.isEmpty() && action.execute() && automationType != AutomationType.EXTERNAL) {
         STACK simulatedRemainder = super.insert(stack, Action.SIMULATE, automationType);
         if (simulatedRemainder.isEmpty()) {
            this.setStackUnchecked(this.createStack(stack, this.getCapacity()));
         }

         return simulatedRemainder;
      } else {
         return super.insert(stack, action.combine(!this.isCreative), automationType);
      }
   }

   @Override
   public STACK extract(long amount, Action action, AutomationType automationType) {
      return super.extract(amount, action.combine(!this.isCreative), automationType);
   }

   @Override
   public long setStackSize(long amount, Action action) {
      return super.setStackSize(amount, action.combine(!this.isCreative));
   }

   private static class GasTankChemicalTank extends ChemicalTankChemicalTank<Gas, GasStack> implements IGasHandler, IGasTank {
      private GasTankChemicalTank(ChemicalTankTier tier, @Nullable IContentsListener listener) {
         super(tier, ChemicalTankBuilder.GAS, listener);
      }
   }

   private static class InfusionTankChemicalTank extends ChemicalTankChemicalTank<InfuseType, InfusionStack> implements IInfusionHandler, IInfusionTank {
      private InfusionTankChemicalTank(ChemicalTankTier tier, @Nullable IContentsListener listener) {
         super(tier, ChemicalTankBuilder.INFUSION, listener);
      }
   }

   private static class PigmentTankChemicalTank extends ChemicalTankChemicalTank<Pigment, PigmentStack> implements IPigmentHandler, IPigmentTank {
      private PigmentTankChemicalTank(ChemicalTankTier tier, @Nullable IContentsListener listener) {
         super(tier, ChemicalTankBuilder.PIGMENT, listener);
      }
   }

   private static class SlurryTankChemicalTank extends ChemicalTankChemicalTank<Slurry, SlurryStack> implements ISlurryHandler, ISlurryTank {
      private SlurryTankChemicalTank(ChemicalTankTier tier, @Nullable IContentsListener listener) {
         super(tier, ChemicalTankBuilder.SLURRY, listener);
      }
   }
}
